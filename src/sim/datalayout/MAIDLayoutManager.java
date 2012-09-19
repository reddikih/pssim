package sim.datalayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sim.Environment;
import sim.SimParameter;
import sim.datalayout.info.MAIDLayoutInfo;
import sim.datalayout.managed.CacheDisk;
import sim.datalayout.managed.DataDisk;
import sim.datalayout.managed.DataEntry;
import sim.datalayout.partitioning.IPartitioning;
import sim.datalayout.partitioning.RoundRobinPartitioning;
import sim.output.LogCollector;
import sim.stat.Statistics;
import sim.stat.MAIDStats;
import sim.stat.RAPoSDAStats;
import sim.storage.StorageManager;
import sim.storage.device.MAIDCacheMemory;
import sim.util.AccessType;
import sim.util.ReplicaType;

public class MAIDLayoutManager extends LayoutManager {

	private int numberOfCacheDisk;
	private int numberOfDataDisk;

	private List<CacheDisk> cacheDiskList;
	private List<DataDisk> dataDiskList;

	private MAIDLayoutInfo layoutInfo;
	private IPartitioning cacheDiskPartitioning;

	@Override
	public void decideDestinationDisk(DataEntry entry) {
		if (!layoutInfo.exist(entry.getId())) {
			int nextAssignedDiskId = this.partitioning.partition(entry);
			this.layoutInfo.putDiskMap(entry.getId(), nextAssignedDiskId);
		}
	}

	private MAIDCacheMemory cache = null;

	@Override
	public void init() {
		this.numberOfCacheDisk = SimParameter.getNumberOfCacheDisks();
		this.numberOfDataDisk = SimParameter.getNumberOfDataDisk();

		this.partitioning = new RoundRobinPartitioning(numberOfDataDisk);
		this.layoutInfo = new MAIDLayoutInfo();

		this.cacheDiskPartitioning = new RoundRobinPartitioning(numberOfCacheDisk);

		this.cache = new MAIDCacheMemory(this);

		createManagedDevices();
	}

	private void createManagedDevices() {
		// CacheDisk
		this.cacheDiskList = new ArrayList<CacheDisk>();
		for (int i = 0; i < numberOfCacheDisk; i++) {
			CacheDisk cDisk = new CacheDisk(i);
			this.cacheDiskList.add(cDisk);
		}

		// DataDisk
		long diskCapacity = Environment.getDiskModel().getCapacity();
		int divid = 2; // PrimaryとBackupの二分割
		this.dataDiskList = new ArrayList<DataDisk>();

		for (int i = 0; i < this.numberOfDataDisk; i++) {
			DataDisk dDisk = new DataDisk(i, diskCapacity, divid);
			int sourceBackupId = (i + (numberOfDataDisk - 1)) % numberOfDataDisk;
			int destinationBackupId = (i + 1) % numberOfDataDisk;
			dDisk.setBackupSourceId(sourceBackupId);
			dDisk.setBackupDestinationId(destinationBackupId);
			this.dataDiskList.add(dDisk);
		}
	}

	@Override
	public double readProcess(DataEntry entry, double arrivalTime) {
		double result = -1.0;

		// Check the cache memory at first
		DataEntry value = (DataEntry)cache.read(entry, arrivalTime);
		result = value.getResponseTime();

		return result;
	}

	public Object readFromSource(DataEntry entry, double arrivalTime) {
		double responseTime = 0.0;
		boolean isHit = false;

		StorageManager sm = Environment.getStorageManager();

		MAIDStats stats = (MAIDStats)Environment.getStats();

		// 最初にキャッシュディスクから読み出しを試みる
		// 実際にキャッシュデータがキャッシュディスクに存在するか確認

		int cacheDiskId = this.layoutInfo.getCacheDiskId(entry.getId());
		if (cacheDiskId != -1) {
			// 少なくとも一度はキャッシュディスクに割り当てられたことがあるデータ
			CacheDisk cacheDisk = this.cacheDiskList.get(cacheDiskId);
			DataEntry cacheEntry = cacheDisk.readEntry(entry.getId(), arrivalTime);
			if (cacheEntry != null) {
				responseTime = sm.accessToCacheDisk(cacheDiskId, cacheEntry, arrivalTime, AccessType.READ);
				isHit = true;

				stats.incrementReadCounter(Statistics.READ_COUNTER_TYPE.CACHE_DISK);
				stats.addingResponseTime(Statistics.RESPONSE_TYPE.CACHE_DISK, responseTime);

				// CacheDisk Hitのログ出力
				String logStr = LogCollector.createCacheDiskHitRatioRecord(entry.getId(), cacheDiskId, arrivalTime, true);
				LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_DISK_HIT_RATIO);
			} else {
				// TODO このようなキャッシュディスクのデータ管理方法も良く検討した方がよさそう
				// layoutInfoから割り当て情報は消す方がよいか，消さない方がよいか？

				// キャッシュ上からは追い出されているので，layoutInfo.cacheDiskMapからは削除しておく
				// 暫定処理．この処理はやるべきかやらないべきかを後で検討する
//				 this.layoutInfo.removeCacheDiskMap(entry.getId());
			}
		}

		if (!isHit) {
			// CacheDiskのHitmissのログ出力
			String logStr = LogCollector.createCacheDiskHitRatioRecord(entry.getId(), -1, arrivalTime, false);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_DISK_HIT_RATIO);

			// キャッシュミスなので，データディスクから読み出す
			double tempTime = readFromDataDisk(entry, arrivalTime);

			// 読み出したデータはキャッシュディスクへコピーを書き込む
			responseTime = writeToCacheDisk(entry, arrivalTime + tempTime, false);
		}
		entry.setResponseTime(responseTime);

		return entry;
	}



	private double readFromDataDisk(DataEntry entry, double arrivalTime) {
		double responseTime = 0.0;
		double delay = 0.0;

		int diskId = 0;
		try {
//TODO ここの処理をランダムに選ぶようにする（Primary or Backup）
			diskId = this.layoutInfo.getDataDiskId(entry.getId());

			Random rand = new Random();
			if(rand.nextInt(2) == 1) {
				diskId = this.dataDiskList.get(diskId).getBackupDestinationId();
			}
// ここの処理をランダムに選ぶようにする（Primary or Backup）
		} catch (Exception e) {
			e.printStackTrace();
		}

		StorageManager sm = Environment.getStorageManager();

		boolean isSpinning = true;
		if(!sm.isSpinning(diskId, arrivalTime)) {
			delay = sm.spinUp(diskId, arrivalTime);
			isSpinning = false;
		}

		// ディスクアクセス時の回転確率ログ出力
		String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskId, arrivalTime, ReplicaType.PRIMARY, isSpinning);
		LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);

		responseTime = sm.accessToDataDisk(diskId, entry, arrivalTime + delay, entry.getAccessType()) + delay;

		MAIDStats stats = (MAIDStats)Environment.getStats();
		stats.incrementReadCounter(Statistics.READ_COUNTER_TYPE.DATA_DISK);
		stats.addingResponseTime(Statistics.RESPONSE_TYPE.DATA_DISK, responseTime);

//		MAIDのキャッシュポリシーは Write through なのでキャッシュディスクのデータがダーティデータになることはありえない．
//		なので，ここの処理は不要．
//		さらに，RAPoSDAに関してもキャッシュディスクはデータディスクのただのコピーなのでダーティデータは存在しない
//		したがってRAPoSDAにおいてもここの処理は不要．
		// ディスクは回転中なので，キャッシュディスク中のダーティーデータをデータディスクへ書き込む
//		List<DataEntry> dirtyList = getDirtyDataList(diskId, arrivalTime + delay);
//		for (DataEntry dirtyData : dirtyList) {
//			delay = sm.accessToDataDisk(diskId, dirtyData, arrivalTime + delay, dirtyData.getAccessType());
//
//			// ダーティーデータのダーティーフラグをfalseに変更
//			int cacheDiskId = this.layoutInfo.getCacheDiskId(dirtyData.getId());
//
//			try {
//				CacheDisk cacheDisk = this.cacheDiskList.get(cacheDiskId);
//				cacheDisk.updateDirtyFlag(dirtyData.getId(), false);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		return responseTime;
	}

	private double writeToCacheDisk(DataEntry entry, double accessTime, boolean dirtyFlag) {
		double responseTime = 0.0;
		// 対象CacheDiskのdisk IDを決定する
		decideDestinationCacheDisk(entry);

		// CacheDiskの管理情報をLRUで更新
		int cacheDiskId = layoutInfo.getCacheDiskId(entry.getId());
		CacheDisk cacheDisk = this.cacheDiskList.get(cacheDiskId);
		cacheDisk.writeEntry(entry, accessTime, dirtyFlag);

		// CacheDiskへの書き込み処理の応答時間を返す
		StorageManager sm = Environment.getStorageManager();
		responseTime = sm.accessToCacheDisk(cacheDiskId, entry, accessTime, AccessType.WRITE);

		MAIDStats stats = (MAIDStats)Environment.getStats();
		stats.incrementWriteCounter(Statistics.WRITE_COUNTER_TYPE.CACHE_DISK);
		stats.addingResponseTime(Statistics.RESPONSE_TYPE.CACHE_DISK, responseTime);

//		MAIDのキャッシュポリシーは Write through なのでキャッシュディスクのデータがダーティデータになることはありえない．
//		なので，ここの処理は不要．
//		さらに，RAPoSDAに関してもキャッシュディスクはデータディスクのただのコピーなのでダーティデータは存在しない
//		したがってRAPoSDAにおいてもここの処理は不要．
		// Writeして追い出されたダーティデータをデータディスクへ書き込む
		// この処理は非同期でされると想定するので，resultの結果以上に応答時間の遅延は発生しない．
//		List<DataEntry> dirtyList = cacheDisk.getDirtyDataFromRemovedList(result);
//		if (dirtyList.size() > 0) {
//
//			// 個々の書き込みは別に発行されるので逐次処理的な遅延は考えないことにする
//			double spinDelay = 0.0;
//			double diskAccessTime = result;
//
//			for (DataEntry dirtyEntry : dirtyList) {
//				int primaryDiskId = this.layoutInfo.getDataDiskId(dirtyEntry.getId());
//
//				// バックアップディスクにも書き込む
//				int backupDiskId = this.dataDiskList.get(primaryDiskId).getBackupDestinationId();
//
//				// 個々の書き込みは別に発行されるので逐次処理的な遅延は考えないことにする
////				diskAccessTime += delay;
//
//				int[] diskIds = {primaryDiskId, backupDiskId};
//
//				for (int i = 0; i < diskIds.length; i++) {
//					boolean isSpinning = true;
//					if (!sm.isSpinning(diskIds[i], diskAccessTime)) {
//						spinDelay = sm.spinUp(diskIds[i], diskAccessTime);
//						isSpinning = false;
//					}
//
//					// ディスクアクセス時の回転確率ログ出力
//					if (i == 0) {
//						String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskIds[i], diskAccessTime, ReplicaType.PRIMARY, isSpinning);
//						LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
//					} else {
//						String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskIds[i], diskAccessTime, ReplicaType.BACKUP, isSpinning);
//						LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
//					}
//
//					// 個々の書き込みは別に発行されるので逐次処理的な遅延は考えないことにする
////					delay = sm.accessToDataDisk(diskId, dirtyEntry, diskAccessTime + spinDelay, dirtyEntry.getAccessType());
//					sm.accessToDataDisk(diskIds[i], dirtyEntry, diskAccessTime + spinDelay, dirtyEntry.getAccessType());
//				}
//			}
//			cacheDisk.clearRemovedList();
//		}

		return responseTime;
	}

	private List<DataEntry> getDirtyDataList(int diskId, double arrivalTime) {
		List<DataEntry> result = new ArrayList<DataEntry>();

		for (CacheDisk cDisk : cacheDiskList) {
			List<DataEntry> tempList = cDisk.getDirtyDataFromCacheLine(arrivalTime);
			for (DataEntry tempEntry : tempList) {
				long dataId = tempEntry.getId();
				if (diskId == this.layoutInfo.getDataDiskId(dataId)) result.add(tempEntry);
			}
		}

		return result;
	}

	private void decideDestinationCacheDisk(DataEntry entry) {
		int nextCacheDiskId = -1;
		if (!layoutInfo.existOnCacheDisk(entry.getId())) {
			nextCacheDiskId = this.cacheDiskPartitioning.partition(entry);
			layoutInfo.putCacheDiskMap(entry.getId(), nextCacheDiskId);
		}
	}

	@Override
	public void showConfiguration() {
		System.out.println("<< This is MAID Configuration >>");
		System.out.println("---createManagedDevices-------");
		System.out.println("Size of CacheMemory : " + Environment.getMemoryModel().getCapacity());
		System.out.println("Number of CacheDisk : " + this.numberOfCacheDisk);
		System.out.println("Number of DataDisk  : " + this.numberOfDataDisk);
		System.out.println("------------------------------");
	}

	@Override
	public void showDataAndDiskMappingInfo() {
		layoutInfo.showDataAndDiskMappingInfo();
	}

	@Override
	public double writeProcess(DataEntry entry, double arrivalTime) {

		// Write to the cache memory at first
		DataEntry value = (DataEntry)cache.write(entry, arrivalTime);

		return value.getResponseTime();
	}

	public Object writeToSource(DataEntry entry, double arrivalTime) {
		double responseTime = 0.0;

		double cdResponseTime = writeToCacheDisk(entry, arrivalTime, true);

		// write throghでデータディスクへも書き込む
		double ddResponse = writeToDataDisk(entry, arrivalTime);

		responseTime = cdResponseTime >= ddResponse ? cdResponseTime : ddResponse;

		entry.setResponseTime(responseTime);

		return entry;
	}

	public double writeToDataDisk(DataEntry entry, double arrivalTime) {
		double responseTime = -1.0;

		int pDiskId = this.layoutInfo.getDataDiskId(entry.getId());
		int bDiskId = this.dataDiskList.get(pDiskId).getBackupDestinationId();

		StorageManager sm = Environment.getStorageManager();

		boolean isSpinning;
		double pDelay = 0.0;
		double bDelay = 0.0;

		isSpinning = sm.isSpinning(pDiskId, arrivalTime);
		if (!isSpinning) pDelay = sm.spinUp(pDiskId, arrivalTime);
		loggingDiskRotationRatio(
				pDiskId, entry, arrivalTime, isSpinning, ReplicaType.PRIMARY);

		isSpinning = sm.isSpinning(bDiskId, arrivalTime);
		if (!isSpinning) bDelay = sm.spinUp(bDiskId, arrivalTime);
		loggingDiskRotationRatio(
				bDiskId, entry, arrivalTime, isSpinning, ReplicaType.BACKUP);

		double pResponse = sm.accessToDataDisk(pDiskId, entry, arrivalTime + pDelay, entry.getAccessType()) + pDelay;
		double bResponse = sm.accessToDataDisk(bDiskId, entry, arrivalTime + bDelay, entry.getAccessType()) + bDelay;

		responseTime = pResponse >= bResponse ? pResponse : bResponse;

		Statistics stats = Environment.getStats();
		stats.incrementWriteCounter(Statistics.WRITE_COUNTER_TYPE.DATA_DISK);
		stats.addingResponseTime(Statistics.RESPONSE_TYPE.DATA_DISK, responseTime);

		return responseTime;
	}

	private void loggingDiskRotationRatio(int diskId, DataEntry entry,
			double arrivalTime, boolean isSpinning, ReplicaType type) {

		String logStr = LogCollector.createDiskRotationRatioRecord(
				entry.getId(), diskId, arrivalTime, type, isSpinning);
		LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
	}

	@Override
	public String getStorageType() {
		return "MAID";
	}

}
