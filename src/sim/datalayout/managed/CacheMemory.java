package sim.datalayout.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sim.util.ReplicaType;

public class CacheMemory {

	private int id;
	private int sourceBackupId;
	private int destinationBackupId;

	private long primaryCapacity;
	private long backupCapacity;
	private long primaryUsage;
	private long backupUsage;
	private long threshold;
	private long readArea;
	private long usageReadArea;

	private HashMap<Long, DataEntry> primaryLines;
	private HashMap<Long, DataEntry> backupLines;

	private TreeMap<Double, Long> usedKeys;
	private HashMap<Long, DataEntry> readCaches;

	public CacheMemory(int id, long threshold, long totalCapacity) {
		this(id, threshold, totalCapacity, 0.0, 2);
	}

	public CacheMemory(int id, long threshold, long totalCapacity, double ratioReadArea, int numReplica) {
		this.id = id;
		this.threshold = threshold;
		/* 注意:double -> long へのキャストでは小数点部分は切り捨てられる */
		this.readArea = (long)(totalCapacity * ratioReadArea);

		long forBufferArea = totalCapacity - readArea;
		this.primaryCapacity = (long)(forBufferArea / numReplica);
		this.backupCapacity = forBufferArea - primaryCapacity;

		this.primaryLines = new HashMap<Long, DataEntry>();
		this.backupLines = new HashMap<Long, DataEntry>();

		this.usedKeys = new TreeMap<Double, Long>();
		this.readCaches = new HashMap<Long, DataEntry>();
	}

	public long getUsageVolume(ReplicaType type) {
		long result = -1;
		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.primaryUsage;
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.backupUsage;
		}
		return result;
	}

	/**
	 * プライマリ or バックアップ領域へデータを書き込みます．書き込みデータが領域の容量を 超えない限りtrueを
	 * 返します．判定は領域の容量しか見ていないため，閾値を超えているかの判断は別途処理が必要になります．
	 *
	 * @param entry バッファへ書き込むデータ
	 * @param type プライマリ or バックアップ領域のどちらが対象かを示す.
	 * @return バッファに書き込めればtrue，書き込みデータによって領域が容量オーバーしてしまう場合はfalse
	 */
	public boolean writeCacheData(DataEntry entry, ReplicaType type) {
		boolean result = false;
		int size = entry.getSize();

		if (type.equals(ReplicaType.PRIMARY)) {
			if (this.primaryCapacity - (this.primaryUsage + size) >= 0) {
				if (!primaryLines.containsKey(entry.getId())) {
					this.primaryUsage += size;
				}
				this.primaryLines.put(entry.getId(), entry);
				result = true;
			}
		} else if (type.equals(ReplicaType.BACKUP)) {
			if (this.backupCapacity - (this.backupUsage + size) >= 0) {
				if (!backupLines.containsKey(entry.getId())) {
					this.backupUsage += size;
				}
				this.backupLines.put(entry.getId(), entry);
				result = true;
			}
		}
		return result;
	}

	/**
	 * read用領域にデータを書き込みます．read用領域は本来キャッシュディスクまでしかキャッシュされなかった
	 * データもメモリでキャッシュ出来るようにするための領域として使用します．
	 *
	 * @param entry
	 * @return read用領域に書き込めればtrue，書き込みによって領域がオーバーフローしてしまう場合はfalse
	 */
	public void writeToReadArea(DataEntry entry, double accessTime) {
		entry.setAccessTime(accessTime);
		if ((usageReadArea + entry.getSize()) <= readArea) {
			addEntry(entry, accessTime);
		} else {
			replaceEntry(entry, accessTime);
		}
	}

	public DataEntry readFromReadArea(long id, double accessTime) {
		return getEntry(id, accessTime);
	}

	private void addEntry(DataEntry entry, double arrivalTime) {
		// 同一データの更新の場合，使用容量は変化しない
		if (!readCaches.containsKey(entry.getId()))
			usageReadArea += entry.getSize();
		DataEntry old = readCaches.put(entry.getId(), entry);
		if (old != null)
			usedKeys.remove(old.getAccessTime());
		usedKeys.put(arrivalTime, entry.getId());
	}

	private DataEntry getEntry(Long id, double accessTime) {
		DataEntry entry = null;
		if (readCaches.containsKey(id)) {
			entry = readCaches.get(id);
			if (entry.getAccessTime() >= accessTime)
				return null;
			usedKeys.remove(entry.getAccessTime());
			usedKeys.put(accessTime, id);
			entry.setAccessTime(accessTime);
		}
		return entry;
	}

	private void replaceEntry(DataEntry entry, double arrivalTime) {
		while (readArea < (usageReadArea + entry.getSize())) {
			Map.Entry<Double, Long> lruEntry = usedKeys.pollFirstEntry();
			DataEntry tempEntry = readCaches.remove(lruEntry.getValue());
			usageReadArea -= tempEntry.getSize();
		}
		addEntry(entry, arrivalTime);
	}

	/**
	 * 指定したidを持つデータをキャッシュメモリから読み出します．もし該当データが
	 * キャッシュ中に存在しない場合はnullが返ります．キャッシュにデータが存在するかどうかの
	 * 判定にはisHitメソッドを使用してください．
	 *
	 * @param id 読み出し対象データのid
	 * @param type プライマリ領域かバックアップ領域かのどちらが対象かを表す．
	 * @return idに対応するDataEntryのオブジェクトを返す．該当データが存在しなかった場合はnullを返す．
	 */
	public DataEntry readCacheData(long id, ReplicaType type) {
		DataEntry result = null;

		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.primaryLines.get(id);
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.backupLines.get(id);
		}
		return result;
	}

	public boolean isHit(long id, ReplicaType type) {
		boolean result = false;

		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.primaryLines.containsKey(id);
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.backupLines.containsKey(id);
		}
		return result;
	}

	/**
	 * 指定したidに対応するエントリを削除します．該当データが削除出来た場合はそのDataEntryオブジェクトを
	 * 返します．もし該当データがキャッシュ中に存在しなかった場合はnullを返します．
	 *
	 * @param id 削除対象データのid
	 * @param type プライマリ領域かバックアップ領域かのどちらが対象かを表す．
	 * @return
	 */
	public DataEntry removeCacheData(long id, ReplicaType type) {
		DataEntry result = null;
		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.primaryLines.remove(id);
			if (result != null) this.primaryUsage -= result.getSize();
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.backupLines.remove(id);
			if (result != null) this.backupUsage -= result.getSize();
		}
		return result;
	}

	public boolean isUnderThreshold(ReplicaType type) {
		boolean result = false;

		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.threshold - this.primaryUsage >= 0 ? true : false;
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.threshold - this.backupUsage >= 0 ? true : false;
		}
		return result;
	}

	/**
	 * 指定した複製種別のバッファ領域上のキャッシュデータを取り出します．
	 *
	 * @param type
	 * @return
	 */
	public List<DataEntry> getCacheLines(ReplicaType type) {
		List<DataEntry> result = null;
		if (type.equals(ReplicaType.PRIMARY)) {
			result = new ArrayList<DataEntry>(this.primaryLines.values());
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = new ArrayList<DataEntry>(this.backupLines.values());
		}
		return result;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSourceBackupId() {
		return sourceBackupId;
	}

	public void setSourceBackupId(int sourceBackupId) {
		this.sourceBackupId = sourceBackupId;
	}

	public int getDestinationBackupId() {
		return destinationBackupId;
	}

	public void setDestinationBackupId(int destinationBackupId) {
		this.destinationBackupId = destinationBackupId;
	}

	public long getPrimaryCapacity() {
		return primaryCapacity;
	}

	public long getBackupCapacity() {
		return backupCapacity;
	}

	public long getPrimaryUsage() {
		return primaryUsage;
	}

	public long getBackupUsage() {
		return backupUsage;
	}

	public long getReadArea() {
		return readArea;
	}

}
