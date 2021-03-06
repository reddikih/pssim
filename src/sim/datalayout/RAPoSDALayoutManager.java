package sim.datalayout;

import java.util.ArrayList;
import java.util.List;

import sim.Environment;
import sim.SimParameter;
import sim.datalayout.info.RAPoSDALayoutInfo;
import sim.datalayout.managed.CacheDisk;
import sim.datalayout.managed.CacheMemory;
import sim.datalayout.managed.CacheUnit;
import sim.datalayout.managed.DataDisk;
import sim.datalayout.managed.DataEntry;
import sim.datalayout.partitioning.IPartitioning;
import sim.datalayout.partitioning.RoundRobinPartitioning;
import sim.output.LogCollector;
import sim.output.LogCollector.OutputType;
import sim.stat.RAPoSDAStats;
import sim.stat.Statistics;
import sim.stat.Statistics.READ_COUNTER_TYPE;
import sim.stat.Statistics.RESPONSE_TYPE;
import sim.stat.Statistics.WRITE_COUNTER_TYPE;
import sim.storage.StorageManager;
import sim.util.AccessType;
import sim.util.ReplicaType;

public class RAPoSDALayoutManager extends LayoutManager {

	private int numberOfCacheDisk;
	private int numberOfCacheMemory;
	private int diskPerCache;
	private int numberOfDataDisk;

	private List<CacheMemory> cacheMemoryList;
	private List<CacheDisk> cacheDiskList;
	private List<DataDisk> dataDiskList;

	private RAPoSDALayoutInfo layoutInfo;
	private IPartitioning cacheDiskPartitioning;

	@Override
	public void init() {
		this.numberOfCacheDisk = SimParameter.getNumberOfCacheDisks();
		this.numberOfCacheMemory = SimParameter.getNumberOfCacheMemory();
		this.diskPerCache = SimParameter.getDisksPerCacheMemory();
		this.numberOfDataDisk = diskPerCache * numberOfCacheMemory;

		this.partitioning = new RoundRobinPartitioning(numberOfDataDisk);
		this.layoutInfo = new RAPoSDALayoutInfo();

		this.cacheDiskPartitioning = new RoundRobinPartitioning(numberOfCacheDisk);

		createManagedDevices();
		deviceMapping();
	}

	@Override
	public void decideDestinationDisk(DataEntry entry) {
		if (!layoutInfo.exist(entry.getId())) {
			int nextAssignedDiskId = this.partitioning.partition(entry);
			this.layoutInfo.putDiskMap(entry.getId(), nextAssignedDiskId);
		}
	}

	private void createManagedDevices() {

		// CacheMemory
		long memoryCapacity = Environment.getMemoryModel().getCapacity();
		long threshold = SimParameter.getThresholdOfMemoryBuffer();
		double readAreaRatio = Environment.getMemoryModel().getReadAreaRatio();
		int numOfReplica = Environment.getMemoryModel().getNumberOfReplica();

		this.cacheMemoryList = new ArrayList<CacheMemory>();

		for (int i = 0; i < numberOfCacheMemory; i++) {
			CacheMemory memory = new CacheMemory(i, threshold, memoryCapacity, readAreaRatio, numOfReplica);
			int sourceBackupId = (i + (numberOfCacheMemory - 1)) % numberOfCacheMemory;
			int destinationBackupId = (i + 1) % numberOfCacheMemory;
			memory.setSourceBackupId(sourceBackupId);
			memory.setDestinationBackupId(destinationBackupId);
			this.cacheMemoryList.add(memory);
		}

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

	/**
	 * キャッシュメモリとデータディスクの対応関係を割り当てます．
	 */
	private void deviceMapping() {
		// CacheUnitを生成して，LayoutInfoのputCacheUnit()へ渡す
		// 最初のループでCacheUnitのPrimary領域に対応するディスクIDの情報を格納し，
		// 次のループでバックアップ領域に対応するディスクIDの情報を格納する

		// Primary領域ディスクID情報の書き込みループ
		int diskId = 0;
		for (int cacheMemoryId = 0; cacheMemoryId < this.numberOfCacheMemory; cacheMemoryId++) {
			CacheUnit cacheUnit = new CacheUnit();
			for (int i = 0; i < this.diskPerCache; i++) {
				cacheUnit.addToPrimaryDiskIdList(diskId);

				DataDisk dataDisk = this.dataDiskList.get(diskId);
				dataDisk.setCacheMemoryId(cacheMemoryId);
				diskId++;
			}
			this.layoutInfo.putCacheUnitMap(cacheMemoryId, cacheUnit);
		}

		// Backup領域ディスクID情報の書き込みループ
		for (int cacheMemoryId = 0; cacheMemoryId < this.numberOfCacheMemory; cacheMemoryId++) {
			CacheUnit cacheUnit = this.layoutInfo.getCacheUnit(cacheMemoryId);
			CacheUnit destinationBackupCacheUnit = this.layoutInfo.getCacheUnit((cacheMemoryId + 1) % numberOfCacheMemory);

			List<Integer> primaryDiskIds = cacheUnit.getPrimaryDiskIdList();
			for (Integer dataDiskId : primaryDiskIds) {
				destinationBackupCacheUnit.addToBackupDiskIdList(dataDiskId);
			}
		}
	}

	@Override
	public double readProcess(DataEntry entry, double arrivalTime) {
		double result = -1.0;

		StorageManager sm = Environment.getStorageManager();
		boolean isHit = false;

		Statistics stats = Environment.getStats();

		// Primary Cache Memoryを確認
		int pCacheMemId = getCacheMemoryId(entry.getId(), ReplicaType.PRIMARY);
		CacheMemory pCacheMemory = this.cacheMemoryList.get(pCacheMemId);
		isHit = pCacheMemory.isHit(entry.getId(), ReplicaType.PRIMARY);
		if (isHit) {
			result = sm.accessToCacheMemory(pCacheMemId, entry, arrivalTime, AccessType.READ);

			stats.incrementReadCounter(RAPoSDAStats.READ_COUNTER_TYPE.WRITE_BUFF);
			stats.addingResponseTime(Statistics.RESPONSE_TYPE.MEMORY, result);

			// Primary Cache Memroy Hitのログ出力
			String logStr = LogCollector.createCacheMemoryHitRatioRecord(entry.getId(), pCacheMemId, arrivalTime, ReplicaType.PRIMARY, true);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
		}

		// Buckup Cache Memoryを確認
		int bCacheMemId = -1;
		CacheMemory bCacheMemory = null;
		if (!isHit) {
			bCacheMemId = getCacheMemoryId(entry.getId(), ReplicaType.BACKUP);
			bCacheMemory = this.cacheMemoryList.get(bCacheMemId);
			isHit = bCacheMemory.isHit(entry.getId(), ReplicaType.BACKUP);
			if (isHit) {
				result = sm.accessToCacheMemory(bCacheMemId, entry, arrivalTime, AccessType.READ);

				stats.incrementReadCounter(RAPoSDAStats.READ_COUNTER_TYPE.WRITE_BUFF);
				stats.addingResponseTime(Statistics.RESPONSE_TYPE.MEMORY, result);

				// Backup Cache Memroy Hitのログ出力
				String logStr = LogCollector.createCacheMemoryHitRatioRecord(entry.getId(), bCacheMemId, arrivalTime, ReplicaType.BACKUP, true);
				LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
			}
		}

		// read用領域を確認
		if (!isHit) {
			DataEntry temp;
			temp = pCacheMemory.readFromReadArea(entry.getId(), arrivalTime);
			if (temp != null) {
				isHit = true;
				result = sm.accessToCacheMemory(pCacheMemId, entry, arrivalTime, AccessType.READ);
				// Primary Cache Memroy Hitのログ出力
				String logStr = LogCollector.createCacheMemoryHitRatioRecord(entry.getId(), pCacheMemId, arrivalTime, ReplicaType.PRIMARY, true);
				LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
			} else {
				temp = bCacheMemory.readFromReadArea(entry.getId(), arrivalTime);
				if (temp != null) {
					isHit = true;
					result = sm.accessToCacheMemory(bCacheMemId, entry, arrivalTime, AccessType.READ);
					// Backup Cache Memroy Hitのログ出力
					String logStr = LogCollector.createCacheMemoryHitRatioRecord(entry.getId(), bCacheMemId, arrivalTime, ReplicaType.BACKUP, true);
					LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
				}
			}

			if (isHit) {
				stats.incrementReadCounter(RAPoSDAStats.READ_COUNTER_TYPE.READ_AREA);
				stats.addingResponseTime(Statistics.RESPONSE_TYPE.MEMORY, result);
			}
		}

		if (!isHit) {
			// CacheMemoryのhitmiss のログ出力
			String logStr = LogCollector.createCacheMemoryHitRatioRecord(entry.getId(), -1, arrivalTime, null, false);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
		}

		// Cache Diskを確認
		if (!isHit) {
			// 実際にキャッシュデータがキャッシュディスクに存在するか確認
			int cacheDiskId = this.layoutInfo.getCacheDiskId(entry.getId());
			if (cacheDiskId != -1) {
				// 少なくとも一度はキャッシュディスクに割り当てられたことがあるデータ
				CacheDisk cacheDisk = this.cacheDiskList.get(cacheDiskId);
				DataEntry cacheEntry = cacheDisk.readEntry(entry.getId(), arrivalTime);
				if (cacheEntry != null) { // キャッシュヒット
					result = sm.accessToCacheDisk(cacheDiskId, cacheEntry, arrivalTime, AccessType.READ);
					isHit = true;

					// キャッシュメモリのread用領域にデータをコピー
					pCacheMemory.writeToReadArea(entry, result);
					bCacheMemory.writeToReadArea(entry, result);

					stats.incrementReadCounter(RAPoSDAStats.READ_COUNTER_TYPE.CACHE_DISK);
					stats.addingResponseTime(Statistics.RESPONSE_TYPE.CACHE_DISK, result);

					// CacheDisk Hitのログ出力
					String logStr = LogCollector.createCacheDiskHitRatioRecord(entry.getId(), cacheDiskId, arrivalTime, true);
					LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_DISK_HIT_RATIO);
				} else {
					// TODO このようなキャッシュディスクのデータ管理方法も良く検討した方がよさそう
					// layoutInfoから割り当て情報は消す方がよいか，消さない方がよいか？

					// キャッシュ上からは追い出されているので，layoutInfo.cacheDiskMapからは削除しておく
					// 暫定処理．この処理はやるべきかやらないべきかを後で検討する
//					this.layoutInfo.removeCacheDiskMap(entry.getId());
				}
			}
		}

		if (!isHit) {
			// CacheDiskのHitmissのログ出力
			String logStr = LogCollector.createCacheDiskHitRatioRecord(entry.getId(), -1, arrivalTime, false);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.CACHE_DISK_HIT_RATIO);
		}

		// キャッシュミスならData diskから取得
		if (!isHit) {
			result = readFromDataDisk(entry, arrivalTime);
		}

		return result;
	}

	private double readFromDataDisk(DataEntry entry, double arrivalTime) {
		double result = 0.0;

		// 場合分け処理
		// a) 片方回転中：回転している方のディスクから読む
		// b) 両方回転中：バッファのキューが長い方から読む
		// c) 両方停止中：停止期間が長い方から読む

		StorageManager sm = Environment.getStorageManager();

		int pDiskId = getDataDiskId(entry.getId(), ReplicaType.PRIMARY);
		int bDiskId = getDataDiskId(entry.getId(), ReplicaType.BACKUP);

		boolean isDone = false;

		//// a) 片方回転中：回転している方のディスクから読む
		if (sm.isSpinning(pDiskId, arrivalTime) && !sm.isSpinning(bDiskId, arrivalTime)) {
			result = sm.accessToDataDisk(pDiskId, entry, arrivalTime, AccessType.READ);
			isDone = true;

			// ディスク回転中アクセスのログ出力(primary)
			String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), pDiskId, arrivalTime, ReplicaType.PRIMARY, true);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
			// ディスク停止中アクセスのログ出力(backup)
			// ここは停止中でもスピンアップには関係ないので，ディスクアクセスログには出さない
//			logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), bDiskId, arrivalTime, ReplicaType.BACKUP, false);
//			LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
		} else if (sm.isSpinning(bDiskId, arrivalTime) && !sm.isSpinning(pDiskId, arrivalTime)) {
			result = sm.accessToDataDisk(bDiskId, entry, arrivalTime, AccessType.READ);
			isDone = true;

			// ディスク回転中アクセスのログ出力(backup)
			String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), bDiskId, arrivalTime, ReplicaType.BACKUP, true);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
			// ディスク停止中アクセスのログ出力(primary)
			// ここは停止中でもスピンアップには関係ないので，ディスクアクセスログには出さない
//			logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), pDiskId, arrivalTime, ReplicaType.PRIMARY, false);
//			LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
		}


		//// b) 両方回転中：バッファのキューが長い方から読む
		int pCacheMemoryId = getCacheMemoryId(entry.getId(), ReplicaType.PRIMARY);
		int bCacheMemoryId = getCacheMemoryId(entry.getId(), ReplicaType.BACKUP);

		if (!isDone) {
			if (sm.isSpinning(pDiskId, arrivalTime) && sm.isSpinning(bDiskId, arrivalTime)) {

				long pBufferSize = getCacheBufferSize(pCacheMemoryId, ReplicaType.PRIMARY);
				long bBufferSize = getCacheBufferSize(bCacheMemoryId, ReplicaType.BACKUP);

				if (pBufferSize >= bBufferSize) {
					result = sm.accessToDataDisk(pDiskId, entry, arrivalTime, AccessType.READ);
					isDone = true;

					// ディスク回転中アクセスのログ出力
					String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), pDiskId, arrivalTime, ReplicaType.PRIMARY, true);
					LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
				} else {
					result = sm.accessToDataDisk(bDiskId, entry, arrivalTime, AccessType.READ);
					isDone = true;

					// ディスク回転中アクセスのログ出力
					String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), bDiskId, arrivalTime, ReplicaType.BACKUP, true);
					LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
				}
			}
		}


		//// c) 両方停止中：停止期間が長い方から読む
		if (!isDone) {
			double pLastAccessTimestamp = sm.getLastAccessTimestamp(pDiskId);
			double bLastAccessTimeStamp = sm.getLastAccessTimestamp(bDiskId);

			if (pLastAccessTimestamp <= bLastAccessTimeStamp) {
				double delay = sm.spinUp(pDiskId, arrivalTime);
				result = sm.accessToDataDisk(pDiskId, entry, arrivalTime + delay, AccessType.READ) + delay;

				// ディスク停止中アクセスのログ出力
				String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), pDiskId, arrivalTime, ReplicaType.PRIMARY, false);
				LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
			} else {
				double delay = sm.spinUp(bDiskId, arrivalTime);
				result = sm.accessToDataDisk(bDiskId, entry, arrivalTime + delay, AccessType.READ) + delay;

				// ディスク停止中アクセスのログ出力
				String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), bDiskId, arrivalTime, ReplicaType.BACKUP, false);
				LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
			}
		}

		// 読み出したデータはキャッシュディスクへ書き込む
		writeToCacheDisk(entry, arrivalTime + result, AccessType.BG_WRITE);

		// 更にキャッシュメモリのread用領域にもコピーする．
		// Primary と Backup それぞれのキャッシュメモリにコピーする．
		CacheMemory cacheMem = this.cacheMemoryList.get(pCacheMemoryId);
		cacheMem.writeToReadArea(entry, arrivalTime + result);
		cacheMem = this.cacheMemoryList.get(bCacheMemoryId);
		cacheMem.writeToReadArea(entry, arrivalTime + result);

		Statistics stats = Environment.getStats();
		stats.incrementReadCounter(READ_COUNTER_TYPE.DATA_DISK);
		stats.addingResponseTime(RESPONSE_TYPE.DATA_DISK, result);

		return result;
	}

	private int getDataDiskId(long dataId, ReplicaType type) {
		int result = -1;

		int pDiskId = this.layoutInfo.getDataDiskId(dataId);
		DataDisk dataDisk = this.dataDiskList.get(pDiskId);
		int bDiskId = dataDisk.getBackupDestinationId();

		if (type.equals(ReplicaType.PRIMARY)) result = pDiskId;
		else result = bDiskId;

		return result;
	}

	private long getCacheBufferSize(int cacheMemoryId, ReplicaType type) {
		long result = -1;

		CacheMemory cacheMemory = this.cacheMemoryList.get(cacheMemoryId);
		List<DataEntry> entries = cacheMemory.getCacheLines(type);

		for (DataEntry entry : entries) {
			result += entry.getSize();
		}
		return result;
	}

	@Override
	public double writeProcess(DataEntry entry, double arrivalTime) {
		double result = -1.0;

		// PrimaryとBackupのキャッシュメモリIDを取得
		int primaryDiskId = layoutInfo.getDataDiskId(entry.getId());
		DataDisk dataDisk = this.dataDiskList.get(primaryDiskId);
		int pCacheMemId = dataDisk.getCacheMemoryId();

		CacheMemory cacheMemory = cacheMemoryList.get(pCacheMemId);
		int bCacheMemId = cacheMemory.getDestinationBackupId();


		// 各データ毎に書き込み処理を実施する
		double resPrimary = writeSequence(pCacheMemId, entry, arrivalTime, ReplicaType.PRIMARY);
		double resBackup = writeSequence(bCacheMemId, entry, arrivalTime, ReplicaType.BACKUP);

		result = resPrimary < resBackup ? resBackup : resPrimary;

		return result;
	}

	private double writeSequence(int cacheMemoryId, DataEntry entry, double arrivalTime, ReplicaType type) {
		double responseTime = -1.0;

		CacheMemory cacheMemory = cacheMemoryList.get(cacheMemoryId);
		boolean shouldDiskAccess = true;
		double delay = 0;

		int targetDiskId = -1;
		boolean canWrite = cacheMemory.writeCacheData(entry, type);
		if (canWrite) {
			boolean isUnderThreshold = cacheMemory.isUnderThreshold(type); // Cacheに書き込めて，かつ閾値を超えていない
			if (isUnderThreshold) {
				// メモリアクセス遅延時間だけがかかる
				StorageManager sm = Environment.getStorageManager();
				responseTime = sm.accessToCacheMemory(cacheMemoryId, entry, arrivalTime, AccessType.WRITE);
				shouldDiskAccess = false;

				Statistics stats = Environment.getStats();
				stats.incrementWriteCounter(WRITE_COUNTER_TYPE.CACHE_MEMORY);
				stats.addingResponseTime(RESPONSE_TYPE.MEMORY, responseTime);

				// バッファ書き込み可能率のログ出力(hit)
				String logStr = LogCollector.createBufferWritableRatioRecord(entry.getId(), cacheMemoryId, arrivalTime, type, true);
				LogCollector.outputRecord(logStr, OutputType.BUFFER_WRITABLE_RATIO);
			} else {
				// バッファ書き込み可能率のログ出力(overflow)
				String logStr = LogCollector.createBufferWritableRatioRecord(entry.getId(), cacheMemoryId, arrivalTime, type, false);
				LogCollector.outputRecord(logStr, OutputType.BUFFER_WRITABLE_RATIO);
			}
		} else {
			if (type.equals(ReplicaType.PRIMARY)) {
				targetDiskId = layoutInfo.getDataDiskId(entry.getId());
			} else {
				int pDiskId = layoutInfo.getDataDiskId(entry.getId());
				DataDisk dataDisk = this.dataDiskList.get(pDiskId);
				targetDiskId = dataDisk.getBackupDestinationId();
			}

			StorageManager sm = Environment.getStorageManager();

			// 対象ディスクをスピンアップ
			boolean isSpinning = sm.isSpinning(targetDiskId, arrivalTime);
			if (!isSpinning) {
				delay = sm.spinUp(targetDiskId, arrivalTime);
				arrivalTime += delay;
			}
			String logStr = LogCollector.createDiskRotationRatioRecord(
					entry.getId(), targetDiskId, arrivalTime, type, isSpinning);
			LogCollector.outputRecord(logStr, OutputType.DISK_ROTATION_RATIO);

			// バッファ書き込み可能率のログ出力(overflow)
			logStr = LogCollector.createBufferWritableRatioRecord(entry.getId(), cacheMemoryId, arrivalTime, type, false);
			LogCollector.outputRecord(logStr, OutputType.BUFFER_WRITABLE_RATIO);
		}

		if (shouldDiskAccess) {
			// Diskアクセス処理

			// 対象キャッシュラインの取得
			List<DataEntry> mainEntries = cacheMemory.getCacheLines(type);
			// 複製データのキャッシュライン取得
			List<DataEntry> repEntries = getReplicaCacheLines(cacheMemory, type);

			List<DataEntry> entries = new ArrayList<DataEntry>();
			entries.addAll(mainEntries);
			entries.addAll(repEntries);
			entries.add(entry);

			responseTime = writeToDataDisk(entries.toArray(new DataEntry[0]), arrivalTime, type, targetDiskId) + delay;

			// 取りだしたキャッシュラインのデータはキャッシュメモリから削除する
			removeCacheLine(mainEntries, cacheMemory, type);
			if (type.equals(ReplicaType.PRIMARY))
				removeReplicaDataCacheLine(repEntries, ReplicaType.BACKUP);
			else
				removeReplicaDataCacheLine(repEntries, ReplicaType.PRIMARY);
		}

		return responseTime;
	}

	private List<DataEntry> getReplicaCacheLines(CacheMemory cacheMemory, ReplicaType type) {
		List<DataEntry> result = new ArrayList<DataEntry>();

		CacheMemory repCacheMemory;

		if (type.equals(ReplicaType.PRIMARY)) {
//			 プライマリのキャッシュラインを取得
//			result.addAll(cacheMemory.getCacheLines(ReplicaType.PRIMARY));

			// ディスクのバックアップ領域に対応するキャッシュラインを取得
			CacheUnit cacheUnit = this.layoutInfo.getCacheUnit(cacheMemory.getId());
			List<Integer> primaryDiskIds = cacheUnit.getPrimaryDiskIdList();

			for (int primaryDiskId : primaryDiskIds) {
				DataDisk pDisk = this.dataDiskList.get(primaryDiskId);
				int backupDiskId = pDisk.getBackupSourceId();
				int cacheMemoryId = layoutInfo.searchCacheMemoryIdRelatedToDiskId(backupDiskId, ReplicaType.BACKUP);
				repCacheMemory = cacheMemoryList.get(cacheMemoryId);
				result.addAll(repCacheMemory.getCacheLines(ReplicaType.BACKUP));
			}
		} else {
//			 バックアップのキャッシュラインを取得
//			result.addAll(cacheMemory.getCacheLines(ReplicaType.BACKUP));

			// ディスクのプライマリ領域に対応するキャッシュラインを取得
			CacheUnit cacheUnit = this.layoutInfo.getCacheUnit(cacheMemory.getId());
			List<Integer> backupDiskIds = cacheUnit.getBackupDiskIdList();

			for (int backupDiskId : backupDiskIds) {
				DataDisk bDisk = this.dataDiskList.get(backupDiskId);
				int primaryDiskId = bDisk.getId();
				int cacheMemoryId = layoutInfo.searchCacheMemoryIdRelatedToDiskId(primaryDiskId, ReplicaType.PRIMARY);
				repCacheMemory = cacheMemoryList.get(cacheMemoryId);
				result.addAll(repCacheMemory.getCacheLines(ReplicaType.PRIMARY));
			}
		}

		return result;
	}

	public void removeCacheLine(List<DataEntry> entries, CacheMemory cacheMemory, ReplicaType type) {
		for (DataEntry entry : entries) {
			cacheMemory.removeCacheData(entry.getId(), type);
		}
	}

	public void removeReplicaDataCacheLine(List<DataEntry> repEntries, ReplicaType type) {

		if (type.equals(ReplicaType.PRIMARY)) {
			for (DataEntry entry : repEntries) {
				int diskId = layoutInfo.getDataDiskId(entry.getId());
				DataDisk disk = dataDiskList.get(diskId);
				int cacheMemoryId = disk.getCacheMemoryId();
				CacheMemory cacheMemory = cacheMemoryList.get(cacheMemoryId);
				cacheMemory.removeCacheData(entry.getId(), ReplicaType.PRIMARY);
			}
		} else {
			for (DataEntry entry : repEntries) {
				int diskId = layoutInfo.getDataDiskId(entry.getId());
				DataDisk disk = dataDiskList.get(diskId);
				int cacheMemoryId = disk.getCacheMemoryId();
				CacheMemory cacheMemory = cacheMemoryList.get(cacheMemoryId);

				// バックアップ側のキャッシュメモリを取得する
				cacheMemoryId = cacheMemory.getDestinationBackupId();
				cacheMemory = cacheMemoryList.get(cacheMemoryId);
				cacheMemory.removeCacheData(entry.getId(), ReplicaType.BACKUP);
			}
		}
	}

	private double writeToDataDisk(DataEntry[] entries, double arrivalTime,
			ReplicaType type, int ignoreDiskId) {
		double responseTime = -1.0;

		StorageManager sm = Environment.getStorageManager();

		if (type.equals(ReplicaType.PRIMARY)) {
			for (DataEntry entry : entries) {
				int diskId = layoutInfo.getDataDiskId(entry.getId());
				if (sm.isSpinning(diskId, arrivalTime)) {
					responseTime = sm.accessToDataDisk(diskId, entry, arrivalTime, AccessType.WRITE);

					Statistics stats = Environment.getStats();
					stats.incrementWriteCounter(WRITE_COUNTER_TYPE.DATA_DISK);
					stats.addingResponseTime(RESPONSE_TYPE.DATA_DISK, responseTime);

					writeToCacheDisk(entry, arrivalTime + responseTime, AccessType.BG_WRITE);

					// キャッシュメモリのread用領域にもコピーする
					int cacheMemId = getCacheMemoryId(entry.getId(), ReplicaType.PRIMARY);
					CacheMemory cacheMem = this.cacheMemoryList.get(cacheMemId);
					if (cacheMem != null)
						//　read用領域へのコピーはデータディスクへの書き込みとは非同期で行うので
						// データディスクの書き込み待ちはしない
						cacheMem.writeToReadArea(entry, arrivalTime);

					// ディスク回転中アクセスのログ出力
					if (diskId != ignoreDiskId) {
						String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskId, arrivalTime, type, true);
						LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
					}
				} else {
					// ディスク停止中アクセスのログ出力
					// ここは停止中でもスピンアップには関係ないので，ディスクアクセスログには出さない
//					String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskId, arrivalTime, type, false);
//					LogCollector.outputRecord(logStr, OutputType.DISK_ROTATION_RATIO);
				}
			}
		} else {
			for (DataEntry entry : entries) {
				int tempDiskId = layoutInfo.getDataDiskId(entry.getId());
				DataDisk dDisk = this.dataDiskList.get(tempDiskId);
				int diskId = dDisk.getBackupDestinationId();
				if (sm.isSpinning(diskId, arrivalTime)) {
					responseTime = sm.accessToDataDisk(diskId, entry, arrivalTime, AccessType.WRITE);

					Statistics stats = Environment.getStats();
					stats.incrementWriteCounter(WRITE_COUNTER_TYPE.DATA_DISK);
					stats.addingResponseTime(RESPONSE_TYPE.DATA_DISK, responseTime);

					writeToCacheDisk(entry, arrivalTime + responseTime, AccessType.BG_WRITE);

					// キャッシュメモリのread用領域にもコピーする
					int cacheMemId = getCacheMemoryId(entry.getId(), ReplicaType.BACKUP);
					CacheMemory cacheMem = this.cacheMemoryList.get(cacheMemId);
					if (cacheMem != null)
						//　read用領域へのコピーはデータディスクへの書き込みとは非同期で行うので
						// データディスクの書き込み待ちはしない
						cacheMem.writeToReadArea(entry, arrivalTime);

					// ディスク回転中アクセスのログ出力
					String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskId, arrivalTime, type, true);
					LogCollector.outputRecord(logStr, LogCollector.OutputType.DISK_ROTATION_RATIO);
				} else {
					// ディスク停止中アクセスのログ出力
					// ここは停止中でもスピンアップには関係ないので，ディスクアクセスログには出さない
//					String logStr = LogCollector.createDiskRotationRatioRecord(entry.getId(), diskId, arrivalTime, type, false);
//					LogCollector.outputRecord(logStr, OutputType.DISK_ROTATION_RATIO);
				}
			}
		}

		return responseTime;
	}

	private double writeToCacheDisk(
			DataEntry entry, double accessTime, AccessType accessType) {
		double responseTime = -1.0;
		// 対象CacheDiskのdisk IDを決定する
		decideDestinationCacheDisk(entry);

		// CacheDiskの管理情報をLRUで更新
		int cacheDiskId = layoutInfo.getCacheDiskId(entry.getId());
		CacheDisk cacheDisk = this.cacheDiskList.get(cacheDiskId);
		cacheDisk.writeEntry(entry, accessTime, false);

		// CacheDiskへの書き込み処理の応答時間を返す
		StorageManager sm = Environment.getStorageManager();
		responseTime = sm.accessToCacheDisk(cacheDiskId, entry, accessTime, accessType);

		Statistics stats = Environment.getStats();
		stats.incrementWriteCounter(WRITE_COUNTER_TYPE.CACHE_DISK);
		stats.addingResponseTime(RESPONSE_TYPE.CACHE_DISK, responseTime);

		return responseTime;
	}

	private void decideDestinationCacheDisk(DataEntry entry) {
		int nextCacheDiskId = -1;
		if (!layoutInfo.existOnCacheDisk(entry.getId())) {
			nextCacheDiskId = this.cacheDiskPartitioning.partition(entry);
			layoutInfo.putCacheDiskMap(entry.getId(), nextCacheDiskId);
		}
	}

	private int getCacheMemoryId(long dataId, ReplicaType type) {
		int result = -1;

		// PrimaryとBackupのキャッシュメモリIDを取得
		int primaryDiskId = layoutInfo.getDataDiskId(dataId);
		DataDisk dataDisk = this.dataDiskList.get(primaryDiskId);
		int pCacheMemId = dataDisk.getCacheMemoryId();

		if (type.equals(ReplicaType.PRIMARY)) {
			result = pCacheMemId;
		} else {
			CacheMemory cacheMemory = cacheMemoryList.get(pCacheMemId);
			int bCacheMemId = cacheMemory.getDestinationBackupId();
			result = bCacheMemId;
		}
		return result;
	}

	@Override
	public void showConfiguration() {
		System.out.println("<< This is RAPoSDA Configuration >>");
		debugCreateManagedDevices();
		debugDeviceMapping();
	}

	public void debugCreateManagedDevices() {
		System.out.println("---createManagedDevices-------");
		System.out.println("Number of CacheMemory : " + this.numberOfCacheMemory);
		System.out.println("Size of CacheMemory   : " + Environment.getMemoryModel().getCapacity());
		System.out.println("Number of CacheDisk   : " + this.numberOfCacheDisk);
		System.out.println("Disks per CacheMemory : " + this.diskPerCache);
		System.out.println("Number of DataDisk    : " + this.numberOfDataDisk);
		System.out.println("------------------------------");
//		for (int i = 0; i < this.numberOfCacheMemory; i++) {
//			System.out.println("CacheMemory[" + i + "], id = " + this.cacheMemoryList.get(i).getId()
//								+ " source backup id = " + this.cacheMemoryList.get(i).getSourceBackupId()
//								+ " destination backup id = " + this.cacheMemoryList.get(i).getDestinationBackupId());
//		}
//		for (int i = 0; i < this.numberOfCacheDisk; i++) {
//			System.out.println("CacheDisk[" + i + "], id = " + this.cacheDiskList.get(i).getId());
//		}
//		for (int i = 0; i < this.numberOfDataDisk; i++) {
//			System.out.println("DataDisk[" + i + "], id = " + this.dataDiskList.get(i).getId()
//								+ " source backup id = " + this.dataDiskList.get(i).getBackupSourceId()
//								+ " destination backup id = " + this.dataDiskList.get(i).getBackupDestinationId());
//		}
	}

	public void debugDeviceMapping() {
		System.out.println("---deviceMapping--------------");
//		for (CacheMemory memory : cacheMemoryList) {
//			System.out.println("CacheMemory[" + memory.getId() + "]");
//			CacheUnit cacheUnit = this.layoutInfo.getCacheUnit(memory.getId());
//			System.out.print("  Primary Disk Id :");
//			for (int pDiskId : cacheUnit.getPrimaryDiskIdList()) {
//				System.out.print(" " + pDiskId + " ");
//			}
//			System.out.println("");
//
//			System.out.print("  Backup Disk Id :");
//			for (int bDiskId : cacheUnit.getBackupDiskIdList()) {
//				System.out.print(" " + bDiskId + " ");
//			}
//			System.out.println("");
//		}
	}

	public void showDataAndDiskMappingInfo() {
		layoutInfo.showDataAndDiskMappingInfo();
	}

	@Override
	public String getStorageType() {
		return 	"RAPoSDA";
	}
}
