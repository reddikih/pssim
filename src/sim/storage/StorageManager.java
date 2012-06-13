package sim.storage;

import java.util.ArrayList;
import java.util.List;

import sim.Environment;
import sim.SimParameter;
import sim.WorkloadRecord;
import sim.datalayout.DefaultLayoutManager;
import sim.datalayout.LayoutManager;
import sim.datalayout.MAIDLayoutManager;
import sim.datalayout.RAPoSDALayoutManager;
import sim.datalayout.managed.DataEntry;
import sim.output.LogCollector;
import sim.storage.device.HardDiskDrive;
import sim.storage.device.Memory;
import sim.storage.device.state.DefaultDiskStateManager;
import sim.storage.device.state.DiskStateManager;
import sim.storage.device.state.MAIDDiskStateManager;
import sim.storage.device.state.RAPoSDADiskStateManager;
import sim.util.DataType;
import sim.util.AccessType;

public class StorageManager {

	private int numberOfCacheMemory;
	private int numberOfCacheDisk;
	private int numberOfDataDisk;

	private List<Memory> cacheMemories;
	private List<HardDiskDrive> cacheDisks;
	private List<HardDiskDrive> dataDisks;

	public StorageManager() {
		this.numberOfCacheMemory = SimParameter.getNumberOfCacheMemory();
		this.numberOfCacheDisk = SimParameter.getNumberOfCacheDisks();

		this.numberOfDataDisk = SimParameter.getNumberOfDataDisk();

		this.cacheMemories = new ArrayList<Memory>();
		this.cacheDisks = new ArrayList<HardDiskDrive>();
		this.dataDisks = new ArrayList<HardDiskDrive>();

		createDevices();
	}

	private void createDevices() {
		DiskStateManager dataDiskStateManager = createDataDiskStateManager();
		DiskStateManager cacheDiskStateManager = createCacheDiskStateManager();


		for (int i = 0; i < this.numberOfCacheMemory; i++) {
			Memory memory = new Memory(i, Environment.getMemoryModel());
			this.cacheMemories.add(memory);
		}

		for (int i = 0; i < this.numberOfCacheDisk; i++) {
			HardDiskDrive hdd = new HardDiskDrive(i, Environment.getDiskModel(), cacheDiskStateManager);
			this.cacheDisks.add(hdd);
		}

		for (int i = 0; i < this.numberOfDataDisk; i++) {
			HardDiskDrive hdd = new HardDiskDrive(i, Environment.getDiskModel(), dataDiskStateManager);
			this.dataDisks.add(hdd);
		}
	}

	private DiskStateManager createDataDiskStateManager() {
		// TODO FactoryMethodを適用すること
		DiskStateManager stateManager = null;
		LayoutManager layoutManager = Environment.getLayoutManager();
		if (layoutManager instanceof DefaultLayoutManager) {
			stateManager = new DefaultDiskStateManager();
		} else if (layoutManager instanceof RAPoSDALayoutManager) {
			stateManager = new RAPoSDADiskStateManager();
		} else if (layoutManager instanceof MAIDLayoutManager) {
			stateManager = new MAIDDiskStateManager();
		}
		return stateManager;
	}

	private DiskStateManager createCacheDiskStateManager() {
		//TODO FactoryMethodを適用すること
		return new DefaultDiskStateManager();
	}

	public double execute(WorkloadRecord request) {
		double responseTime = -1.0;

		LayoutManager layoutManager = Environment.getLayoutManager();

		AccessType type = request.getRequestType();
		DataType dataType = request.getDataType();
		AccessType accessType = request.getRequestType();
		double arrivalTime = request.getArrivalTime();

		DataEntry data = new DataEntry(request.getDataId(), request.getDataSize(), dataType, accessType, arrivalTime);

		if (type.equals(AccessType.READ)) {
			if (dataType.equals(DataType.NORMAL))
				responseTime = layoutManager.readProcess(data, arrivalTime);
		} else if (type.equals(AccessType.WRITE)) {
			layoutManager.decideDestinationDisk(data);
			// 初期データであればrequestデータとデータディスクの対応付けのみを行い，実際の書き込み処理は行わない
			if (dataType.equals(DataType.NORMAL))
				responseTime = layoutManager.writeProcess(data, arrivalTime);
		}

		return responseTime;
	}

	/**
	 * This method accommodate the requirement that needs block level access.
	 *
	 * @param dataIds
	 * @param request
	 * @return
	 */
	public double execute(long[] dataIds, WorkloadRecord request) {
		double responseTime = -1.0;

		LayoutManager layoutManager = Environment.getLayoutManager();

		// modified: convert workload record id to internal data id
		// for considering block access.

		int size = request.getDataSize();
		int blockSize = SimParameter.getBlockSize();

		for (int i=0; i<dataIds.length; i++) {
			double tempResponseTime = 0.0;

			AccessType type = request.getRequestType();
			DataType dataType = request.getDataType();
			AccessType accessType = request.getRequestType();
			double accessTime = request.getArrivalTime();

			DataEntry data = null;
			if (size >= blockSize) {
				data = new DataEntry(dataIds[i], blockSize, dataType, accessType, accessTime);
				size = size - blockSize;
			} else if (size > 0) {
				data = new DataEntry(dataIds[i], size, dataType, accessType, accessTime);
			}
//			int mod = size % blockSize;
//			if (mod == 0 && size != 0) size = blockSize; else size = mod;
//			DataEntry data = new DataEntry(dataIds[i], size, dataType, accessType);
//			size = size - blockSize;

			double arrivalTime = request.getArrivalTime();

			if (type.equals(AccessType.READ)) {
				if (dataType.equals(DataType.NORMAL))
					tempResponseTime = layoutManager.readProcess(data, arrivalTime);
			} else if (type.equals(AccessType.WRITE)) {
				layoutManager.decideDestinationDisk(data);
				// 初期データであればrequestデータとデータディスクの対応付けのみを行い，実際の書き込み処理は行わない
				if (dataType.equals(DataType.NORMAL))
					tempResponseTime = layoutManager.writeProcess(data, arrivalTime);
			}

			responseTime = tempResponseTime > responseTime ? tempResponseTime : responseTime;
		}

		return responseTime;
	}


	public double accessToDataDisk(int diskId, DataEntry data, double arrivalTime, AccessType type) {
		double result = -1.0;

		HardDiskDrive dataDisk = this.dataDisks.get(diskId);

		// 初期データ（DataType.I）だった場合，状態更新処理は行わない．
		DataType dataType = data.getDataType();
		double delay = 0.0;
		if (dataType.equals(DataType.NORMAL)) {
			delay = dataDisk.stateUpdate(arrivalTime, LogCollector.OutputType.DATA_DISK, type);

			if (type.equals(AccessType.READ)) {
				result = dataDisk.read(data, arrivalTime + delay);
			} else if (type.equals(AccessType.WRITE)) {
				result = dataDisk.write(data, arrivalTime + delay);
			}
		}

		// SimTimeManagerの時間を更新する
		Environment.getSimTimeManager().setDataDiskLastAccessTime(result + arrivalTime + delay);

		return result + delay;
	}

	public double accessToDataDiskNormal(int diskId, DataEntry data, double arrivalTime, AccessType type) {
		double result = -1.0;

		HardDiskDrive dataDisk = this.dataDisks.get(diskId);

		// 初期データ（DataType.I）だった場合，状態更新処理は行わない．
		DataType dataType = data.getDataType();
		double delay = 0.0;
		if (dataType.equals(DataType.NORMAL)) {
			delay = dataDisk.stateUpdate(arrivalTime, LogCollector.OutputType.DATA_DISK, type);

			if (type.equals(AccessType.READ)) {
				result = dataDisk.read(data, arrivalTime + delay);
			} else if (type.equals(AccessType.WRITE)) {
				result = dataDisk.write(data, arrivalTime + delay);
			}
		}

		// SimTimeManagerの時間を更新する
		Environment.getSimTimeManager().setDataDiskLastAccessTime(result + arrivalTime + delay);

		return result + delay;
	}

	public double accessToCacheMemory(int memoryId, DataEntry data, double arrivalTime, AccessType type) {
		double result = -1.0;

		Memory memory = this.cacheMemories.get(memoryId);
		result = memory.getLatency();

		// SimTimeManagerの時間を更新する
		Environment.getSimTimeManager().setCacheMemoryLastAccessTime(result + arrivalTime);

		return result;
	}

	public double accessToCacheDisk(int diskId, DataEntry data, double arrivalTime, AccessType type) {
		double responseTime = -1.0;

		HardDiskDrive cacheDisk = this.cacheDisks.get(diskId);

		// 初期データ（DataType.I）だった場合，状態更新処理は行わない．
		DataType dataType = data.getDataType();
		double delay = 0.0;
		if (dataType.equals(DataType.NORMAL))
			delay = cacheDisk.stateUpdate(arrivalTime, LogCollector.OutputType.CACHE_DISK, type);

		if (type.equals(AccessType.READ)) {
			responseTime = cacheDisk.read(data, arrivalTime + delay);
		} else if (type.equals(AccessType.WRITE)) {
			responseTime = cacheDisk.write(data, arrivalTime + delay);
		}

		// SimTimeManagerの時間を更新する
		Environment.getSimTimeManager().setCacheDiskLastAccessTime(responseTime + arrivalTime + delay);

		return responseTime + delay;
	}

	/**
	 * 引数idで指定されたデータディスクが回転中かどうかを判定します．
	 * @param diskId 対象データディスクID
	 * @param time 確認時の時刻
	 * @return ディスクが回転中だった場合true，そうでない場合はfalse
	 */
	public boolean isSpinning(int diskId, double time) {
		boolean result = false;
		HardDiskDrive hdd = this.dataDisks.get(diskId);
		result = hdd.isSpinning(time);
		return result;
	}

	/**
	 * 引数で指定したデータディスクをスピンアップさせます．
	 *
	 * @param diskId 対象データディスクID
	 * @param time スピンアップ操作時の時刻
	 * @retrun スピンアップに掛った時間（単位：秒）
	 */
	public double spinUp(int diskId, double time) {
		double result;
		HardDiskDrive hdd = this.dataDisks.get(diskId);
		result = hdd.spinUp(time);
		return result;
	}

	public double getLastAccessTimestamp(int diskId) {
		HardDiskDrive hdd = this.dataDisks.get(diskId);
		return hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
	}

	public void postProcess(double lastAccessedTimestamp) {
		// CacheDiskの最終状態更新
		for (HardDiskDrive cdisk : cacheDisks) {
			cdisk.stateUpdate(lastAccessedTimestamp, LogCollector.OutputType.CACHE_DISK, AccessType.POSTPROCESS);
		}

		// DataDiskの最終状態更新
		for (HardDiskDrive ddisk : dataDisks) {
			ddisk.stateUpdate(lastAccessedTimestamp, LogCollector.OutputType.DATA_DISK, AccessType.POSTPROCESS);
		}
	}

}
