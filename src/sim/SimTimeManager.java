package sim;

public class SimTimeManager {

	private double cacheMemoryLastAccessTime;
	private double cacheDiskLastAccessTime;
	private double dataDiskLastAccessTime;

	public double getLatestAccessTime() {
		double result = cacheMemoryLastAccessTime;
		if (result < cacheDiskLastAccessTime) result = cacheDiskLastAccessTime;
		if (result < dataDiskLastAccessTime) result = dataDiskLastAccessTime;
		return result;
	}

	public double getCacheMemoryLastAccessTime() {
		return cacheMemoryLastAccessTime;
	}
	public void setCacheMemoryLastAccessTime(double cacheMemoryLastAccessTime) {
		this.cacheMemoryLastAccessTime = cacheMemoryLastAccessTime;
	}
	public double getCacheDiskLastAccessTime() {
		return cacheDiskLastAccessTime;
	}
	public void setCacheDiskLastAccessTime(double cacheDiskLastAccessTime) {
		this.cacheDiskLastAccessTime = cacheDiskLastAccessTime;
	}
	public double getDataDiskLastAccessTime() {
		return dataDiskLastAccessTime;
	}
	public void setDataDiskLastAccessTime(double dataDiskLastAccessTime) {
		this.dataDiskLastAccessTime = dataDiskLastAccessTime;
	}



}
