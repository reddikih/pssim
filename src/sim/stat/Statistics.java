package sim.stat;

public abstract class Statistics {

	public static enum READ_COUNTER_TYPE {
		WRITE_BUFF,
		READ_AREA,
		CACHE_MEMORY,
		CACHE_DISK,
		DATA_DISK,
	};

	public static enum WRITE_COUNTER_TYPE {
		CACHE_MEMORY,
		CACHE_DISK,
		DATA_DISK,
	};

	public static enum RESPONSE_TYPE {
		MEMORY,
		CACHE_DISK,
		DATA_DISK,
	};

	protected long memoryReadCount;
	protected long cacheDiskReadCount;
	protected long dataDiskReadCount;
	protected long totalReadCount;

	protected long writeBufferReadCount;
	protected long readAreaCacheReadCount;

	protected long memoryWriteCount;
	protected long cacheDiskWriteCount;
	protected long dataDiskWriteCount;
	protected long totalWriteCount;

	protected double totalMemoryResponseTime;
	protected double totalCacheDiskResponseTime;
	protected double totalDataDiskResponseTime;


	public double calcMemoryCacheHit() {
		return (double)memoryReadCount / totalReadCount;
	}

	public double calcCacheDiskHit() {
		return (double)cacheDiskReadCount / totalReadCount;
	}

	public double calcWriteBufferCacheHit() {
		return (double)writeBufferReadCount / totalReadCount;
	}

	public double calcReadAreaCacheHit() {
		return (double)readAreaCacheReadCount / totalReadCount;
	}

	protected double calcAverageMemoryResponseTime() {
		return totalMemoryResponseTime / (memoryReadCount + memoryWriteCount);
	}

	protected double calcAverageCacheDiskResponseTime() {
		return totalCacheDiskResponseTime / (cacheDiskReadCount + cacheDiskWriteCount);
	}

	protected double calcAverageDataDiskResponseTime() {
		return totalDataDiskResponseTime / (dataDiskReadCount + dataDiskWriteCount);
	}

	public void incrementReadCounter(READ_COUNTER_TYPE type) {
		if (type.equals(READ_COUNTER_TYPE.CACHE_MEMORY)) {
			memoryReadCount++;
		} else if (type.equals(READ_COUNTER_TYPE.WRITE_BUFF)) {
			writeBufferReadCount++;
			memoryReadCount++;
		} else if (type.equals(READ_COUNTER_TYPE.READ_AREA)) {
			readAreaCacheReadCount++;
			memoryReadCount++;
		} else if (type.equals(READ_COUNTER_TYPE.CACHE_DISK)) {
			cacheDiskReadCount++;
		} else if (type.equals(READ_COUNTER_TYPE.DATA_DISK)) {
			dataDiskReadCount++;
		}
	}

	public void incrementWriteCounter(WRITE_COUNTER_TYPE type) {
		if (type.equals(WRITE_COUNTER_TYPE.CACHE_MEMORY)) {
			memoryWriteCount++;
		} else if (type.equals(WRITE_COUNTER_TYPE.CACHE_DISK)) {
			cacheDiskWriteCount++;
		} else if (type.equals(WRITE_COUNTER_TYPE.DATA_DISK)) {
			dataDiskWriteCount++;
		}
	}

	public void addingResponseTime(RESPONSE_TYPE type, double responseTime) {
		if (type.equals(RESPONSE_TYPE.MEMORY)) {
			totalMemoryResponseTime += responseTime;
		} else if (type.equals(RESPONSE_TYPE.CACHE_DISK)) {
			totalCacheDiskResponseTime += responseTime;
		} else if (type.equals(RESPONSE_TYPE.DATA_DISK)) {
			totalDataDiskResponseTime += responseTime;
		}
	}

	public void incrementRead() {
		totalReadCount++;
	}

	public void incrementWrite() {
		totalWriteCount++;
	}

	public abstract void outputStats();

}
