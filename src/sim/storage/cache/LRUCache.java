package sim.storage.cache;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import sim.datalayout.managed.DataEntry;

public class LRUCache implements Cache {

	private long maxCapacity;
	private long currentSize;
	private CacheSource source;
	private TreeSet<LRUEntry> lruStack;
	private HashMap<Long, DataEntry> dataMap;

	public LRUCache(CacheSource source, long capacity) {
		this.source = source;
		this.maxCapacity = capacity;
		this.lruStack = new TreeSet<LRUEntry>(new LRUComparator());
		this.dataMap = new HashMap<Long, DataEntry>();
	}

	@Override
	public Object read(DataEntry entry, double createdTime) {
		if (entry == null) return null;

		DataEntry value = dataMap.get(entry.getId());

		return value;
	}

	@Override
	public Object write(DataEntry entry, double accessTime) {
		if (entry == null)
			throw new NullPointerException();

		Object result = null;

		LRUEntry cEntry = new LRUEntry(accessTime, entry.getId());

		if (maxCapacity >= currentSize + entry.getSize()) {
			currentSize += entry.getSize();
			lruStack.add(cEntry);
			result = dataMap.put(entry.getId(), entry);
		} else {
			LRUEntry lru = lruStack.pollFirst();

		}

		return result;
	}

	public long getCurrentSize() {
		return this.currentSize;
	}

	private class LRUEntry {
		private double accessTime;
		private long dataId;

		public LRUEntry(double accessTime, long dataId) {
			this.accessTime = accessTime;
			this.dataId = dataId;
		}

		public double getAccessTime() {
			return accessTime;
		}

		public void setAccessTime(double accessTime) {
			this.accessTime = accessTime;
		}

		public long getDataId() {
			return dataId;
		}

		public void setDataId(long dataId) {
			this.dataId = dataId;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof LRUEntry)) return false;
			LRUEntry target = (LRUEntry)obj;
			return target.getDataId() == this.getDataId();
		}

		@Override
		public int hashCode() {
			// follows Effective Java pp35-39
			int result = 17;
			result = 37 * result + (int)(this.getDataId() ^ (this.getDataId() >>> 32));
			return result;
		}

	}

	private class LRUComparator implements Comparator<LRUEntry> {
		@Override
		public int compare(LRUEntry o1, LRUEntry o2) {
			return Double.compare(o1.getAccessTime(), o2.getAccessTime());
		}
	}
}
