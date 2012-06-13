package sim.datalayout.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sim.Environment;

public class CacheDisk {

	private int id;
	private long capacity;
	private long usage;

	/**
	 * Key: dataId, Value: CacheDiskEntry
	 */
	private HashMap<Long, CacheDiskEntry> cacheLine;
//	private List<CacheDiskEntry> cacheLine;
	private HashMap<Long, CacheDiskEntry> removedList;

	public CacheDisk(int id) {
		this.id = id;
		this.cacheLine = new HashMap<Long, CacheDiskEntry>();
//		this.cacheLine = new ArrayList<CacheDiskEntry>();
		this.removedList = new HashMap<Long, CacheDiskEntry>();

		this.capacity = Environment.getCacheDiskModel().getCapacity();
	}

	public int getId() {
		return this.id;
	}

	public void writeEntry(DataEntry entry, double accessTime, boolean dirtyFlag) {
		CacheDiskEntry value = new CacheDiskEntry(entry, accessTime);
		value.setDirtyFlag(dirtyFlag);

		// キャッシュサイズがデータサイズより大きくなければそもそも
		// キャッシュディスクへの書き込み処理はしない
		if (capacity >= entry.getSize()) {
			// LRUでキャッシュの書き込み処理を行う
			while (capacity - (usage + entry.getSize()) < 0) {
				CacheDiskEntry least = null;

				for (CacheDiskEntry e : cacheLine.values()) {
					if (least == null) {
						least = e;
					}
					double leastLeastTime = accessTime - least.getLastAccessTime();
					double eLeastTime = accessTime - e.getLastAccessTime();
					if (leastLeastTime <= eLeastTime) least = e;
				}
				this.usage -= least.getEntry().getSize();
				this.cacheLine.remove(least.getId());

				// 削除したデータは削除用のリストに一時的に格納する．
				removedList.put(least.getId(), least);
			}

			if (!cacheLine.containsKey(value.getId())) {
				this.usage += value.getEntry().getSize();
			}
			cacheLine.put(value.getId(), value);
		}
	}

	public DataEntry readEntry(long dataId, double accessTime) {
		DataEntry result = null;

		if (cacheLine.containsKey(dataId)) {
			CacheDiskEntry entry = cacheLine.get(dataId);
			if (accessTime >= entry.getCreatedTime()) {
				result = entry.getEntry();

				entry.setLastAccessTime(accessTime);
//				cacheLine.put(entry.getId(), entry);
			}
		}
		return result;
	}

	public List<DataEntry> getDirtyDataFromCacheLine(double accessTime) {
		return getDirtyData(accessTime, this.cacheLine);
	}

	public List<DataEntry> getDirtyDataFromRemovedList(double accessTime) {
		return getDirtyData(accessTime, this.removedList);
	}

	private List<DataEntry> getDirtyData(double accessTime, HashMap<Long, CacheDiskEntry> cacheList) {
		List<DataEntry> result = new ArrayList<DataEntry>();

		for (CacheDiskEntry cEntry : cacheList.values()) {
			if (cEntry.getDirtyFlag() && accessTime >= cEntry.getCreatedTime())
				result.add(cEntry.getEntry());
		}
		return result;
	}

	public void updateDirtyFlag(long dataId, boolean flag) {
		CacheDiskEntry entry = this.cacheLine.get(dataId);
		entry.setDirtyFlag(flag);
	}

	public void clearRemovedList() {
		this.removedList.clear();
	}

	public long getCapacity() {
		return capacity;
	}

	public long getUsage() {
		return usage;
	}

}
