package sim.storage.cache;

import sim.datalayout.managed.DataEntry;

public class CacheEntry {

	private DataEntry entry;
	private double accessedTime;

	public CacheEntry(DataEntry entry, double accessedTime) {
		this.entry = entry;
		this.accessedTime = accessedTime;
	}

	public DataEntry getEntry() {
		return entry;
	}

	public void setEntry(DataEntry entry) {
		this.entry = entry;
	}

	public double getAccessedTime() {
		return accessedTime;
	}

	public void setAccessedTime(double accessedTime) {
		this.accessedTime = accessedTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof CacheEntry)) return false;
		CacheEntry target = (CacheEntry)obj;
		return  (target.getEntry().getId() == this.getEntry().getId()) &&
				(target.getEntry().getSize() == this.getEntry().getSize());
	}

	@Override
	public int hashCode() {
		// follows Effective Java pp35-39
		int result = 17;
		result = 37 * result + (int)(this.entry.getId() ^ (this.entry.getId() >>> 32));
		result = 37 * result + this.entry.getSize();
		return result;
	}
}
