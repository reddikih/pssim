package sim.datalayout.managed;

public class CacheDiskEntry {

	private long id;
	private DataEntry entry;
	private double createdTime;
	private double lastAccessTime;
	private boolean dirtyFlag;

	public CacheDiskEntry(DataEntry entry, double createdTime) {
		this.id = entry.getId();
		this.entry = entry;
		this.createdTime = createdTime;
		this.lastAccessTime = createdTime;
		this.dirtyFlag = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DataEntry getEntry() {
		return entry;
	}

	public void setEntry(DataEntry entry) {
		this.entry = entry;
	}

	public double getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(double lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public boolean getDirtyFlag() {
		return dirtyFlag;
	}

	public void setDirtyFlag(boolean flag) {
		this.dirtyFlag = flag;
	}

	public double getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(double createdTime) {
		this.createdTime = createdTime;
	}
}
