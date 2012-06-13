package sim.datalayout.managed;

public class DataDisk {

	private long capacity;
	private long primaryCapacity;
	private long backupCapacity;

	private int id;
	private int backupSourceId;
	private int backupDestinationId;
	private int cacheMemoryId;

	public DataDisk(int id, long capacity, int divid) {
		this.id = id;

		this.capacity = capacity;
		this.primaryCapacity = capacity / divid;
		this.backupCapacity = capacity / divid;
	}

	public int getId() {
		return id;
	}

	public int getBackupSourceId() {
		return backupSourceId;
	}

	public int getBackupDestinationId() {
		return backupDestinationId;
	}

	public int getCacheMemoryId() {
		return cacheMemoryId;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setBackupSourceId(int backupSourceId) {
		this.backupSourceId = backupSourceId;
	}

	public void setBackupDestinationId(int backupDestinationId) {
		this.backupDestinationId = backupDestinationId;
	}

	public void setCacheMemoryId(int cacheMemoryId) {
		this.cacheMemoryId = cacheMemoryId;
	}

}
