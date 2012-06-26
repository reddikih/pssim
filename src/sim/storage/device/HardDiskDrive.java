package sim.storage.device;

import sim.SimParameter;
import sim.datalayout.managed.DataEntry;
import sim.output.LogCollector;
import sim.storage.cache.Cache;
import sim.storage.cache.CacheSource;
import sim.storage.device.model.DiskModel;
import sim.storage.device.state.DiskStateManager;
import sim.util.AccessType;
import sim.util.DiskState;

public class HardDiskDrive implements CacheSource {

	private static final int BLOCK_SIZE = 512;

	private int id;
	private DiskModel model;
	private DiskStateManager stateManager;

	private double lastResponseTime;
	private double lastArrivalTimestamp;
	private double delay;

	private long lastAccessDataId;
	private int lastAccessDataSize;

	private double threshold = SimParameter.getThresholdOfToSpindown();

	private double totalEnergy;

	private Cache diskCache;

	private boolean useCache;

	public HardDiskDrive(int id, DiskModel model, DiskStateManager stateManager) {
		this.id = id;
		this.model = model;
		this.stateManager = stateManager;
		this.diskCache = new DiskCache(this, model.getCacheSize());
		this.useCache = SimParameter.isUseCache();
	}

	/**
	 * READ処理をシミュレートします．
	 *
	 * @param entry 読み出すデータです．
	 * @param arrivalTime リクエストの到着時間
	 * @return HDD上でREAD処理時間
	 */
	public double read(DataEntry entry, double arrivalTime) {
		double responseTime = 0.0;
		if (useCache) {
			DataEntry result = (DataEntry)diskCache.read(entry, arrivalTime);
			responseTime = result.getResponseTime();
		} else {
			responseTime = readDisk(entry.getId(), entry.getSize(), arrivalTime);
		}

		return responseTime;
	}

	/**
	 * WRITE処理をシミュレートします．処理内容はいまのところread()とまったく同じです．
	 *
	 * @param data 書き込むデータのidです．
	 * @param arrivalTime リクエストの到着時間
	 * @return HDD上でのWRITE処理時間
	 */
	public double write(DataEntry data, double arrivalTime) {
		double responseTime = 0.0;
		if (useCache) {
			DataEntry entry = (DataEntry)diskCache.write(data, arrivalTime);
			responseTime = entry.getResponseTime();
		} else {
			responseTime = writeDisk(data.getId(), data.getSize(), arrivalTime);
		}

		return responseTime;
	}

	private double readDisk(long dataId, int size, double arrivalTime) {
//		if (arrivalTime < lastArraivalTimestamp) arrivalTime = lastArraivalTimestamp;

		double serviceTime = calculateServiceTime(size);
		double queueingTime = calculateQueueingTime(arrivalTime);
		double responseTime = serviceTime + queueingTime;

		// 状態時間の更新
		updateAccessParameter(dataId, size, arrivalTime, responseTime);

		return responseTime;
	}

	public double writeDisk(long dataId, int size, double arrivalTime) {
//		if (arrivalTime < lastArraivalTimestamp) arrivalTime = lastArraivalTimestamp;

		double serviceTime = calculateServiceTime(size);
		double queueingTime = calculateQueueingTime(arrivalTime);
		double responseTime = serviceTime + queueingTime;

		// 状態時間の更新
		updateAccessParameter(dataId, size, arrivalTime, responseTime);

		return responseTime;
	}

	public double calculateServiceTime(int size) {
		int block_len = (int)Math.ceil(size / BLOCK_SIZE);
		double fsst = model.getFullStrokeSeekTime();         // full stroke seek time.
		double fdrt = 60.0 / model.getRpm();                 // full disk rotation time
		int sec_per_track = model.getSectorsPerTrack();      // sectors per track
		double overhead = model.getHeadSwitchTime() + model.getCommandOverhead(); // overhead time
		double transfer_rate = 1.0 / ((double)model.getTransferRate() / BLOCK_SIZE); // blocks/s

		return (fsst/2) + (fdrt/2) + fdrt*(block_len/sec_per_track) + overhead + transfer_rate;
	}

	private double calculateQueueingTime(double arrivalTime) {
		double lastAccessTimestamp = lastArrivalTimestamp + lastResponseTime;
		double queueingTime = arrivalTime < lastAccessTimestamp ? lastAccessTimestamp - arrivalTime : 0.0;
		return queueingTime;
	}

	/**
	 * time時刻におけるこのディスクの状態をチェックします．
	 * @param time 状態確認時刻
	 * @return チェック時刻におけるこのディスクの状態
	 */
	public DiskState stateCheck(double time) {
		return this.stateManager.stateCheck(time, this);
	}

	/**
	 * 引数で指定した時刻にアクセスがあったとして，ディスクの状態を更新します．
	 * 状態が変更される場合は，各状態期間のログがログファイルに出力されます．
	 *
	 * @param time 状態更新対象時刻
	 * @param ltype どのログ出力ファイルに書き出すかの種別
	 * @param atype ディスクアクセスの種類
	 * @return wait時間．timeにアクセスがあったリクエストは，time + wait 時間後がarrival timeとなります．
	 */
	public double stateUpdate(double timestamp, LogCollector.OutputType ltype, AccessType atype) {
		return this.stateManager.stateUpdate(timestamp, this, ltype, atype);
	}

	public boolean isSpinning(double time) {
		boolean result = false;
		DiskState state = stateCheck(time);
		if (state.equals(DiskState.ACTIVE) || state.equals(DiskState.IDLE))
			result = true;
		return result;
	}

	/**
	 * ディスクをスピンアップします．
	 *
	 * @param time スピンアップ開始時刻
	 * @return スピンアップ完了時刻
	 */
	public double spinUp(double time) {
		double result = -1.0;

		DiskState state = stateCheck(time);

		double wait = stateUpdate(time, LogCollector.OutputType.DATA_DISK, AccessType.SPINUP);

		double start = -1;
		double end = -1;

		switch (state) {
		case SPINDOWN :
			start = time + wait;
			end = start + this.model.getSpinupTime();
			break;
		case STANDBY :
			start = time;
			end = start + this.model.getSpinupTime();
			break;
		}

		// Spinup状態のログ出力
		if (start != -1 && end != -1) {
			double energy = stateManager.calculateEnergyConsumption(this, DiskState.SPINUP, end - start);
			String logStr = LogCollector.createDataDiskStateRecord(id, -1, -1, DiskState.SPINUP, start, end, energy, AccessType.SPINUP);
			LogCollector.outputRecord(logStr, LogCollector.OutputType.DATA_DISK);

			totalEnergy += energy;

			// ディスクアクセス処理に備えて，フィールド値を更新します．
			// Spin up のときは前回アクセス情報をそのまま使用する
			updateAccessParameter(lastAccessDataId, lastAccessDataSize, end, 0);
		}

		result = end - start;
		return result;
	}

	private void updateAccessParameter(long dataId, int size, double lat, double lrt) {
		lastAccessDataId = dataId;
		lastAccessDataSize = size;
		lastArrivalTimestamp = lat;
		lastResponseTime = lrt;
	}

	public int getId() {
		return id;
	}

	public DiskModel getModel() {
		return model;
	}

	public double getLastResponseTime() {
		return lastResponseTime;
	}

	public double getLastArrivalTimestamp() {
		return lastArrivalTimestamp;
	}

	public double getThreshold() {
		return threshold;
	}

	public double getTotalEnergy() {
		return totalEnergy;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public long getLastAccessDataId() {
		return lastAccessDataId;
	}

	public int getLastAccessDataSize() {
		return lastAccessDataSize;
	}

	public Object readFromSource(DataEntry entry, double accessTime) {
		double responseTime = readDisk(entry.getId(), entry.getSize(), accessTime);
		entry.setResponseTime(responseTime);
		return entry;
	}

	public Object writeToSource(DataEntry entry, double arrivalTime) {
		double responseTime = writeDisk(entry.getId(), entry.getSize(), arrivalTime);
		entry.setResponseTime(responseTime);
		return entry;
	}

}
