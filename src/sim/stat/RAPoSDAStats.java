package sim.stat;

import sim.output.LogCollector;

public class RAPoSDAStats implements IStatistics {

	private long writeBufferReadCount;
	private long readAreaCacheReadCount;
	private long cacheDiskReadCount;
	private long totalReadCount;

	public double calcWriteBufferCacheHit() {
		return (double)writeBufferReadCount / totalReadCount;
	}

	public double calcReadAreaCacheHit() {
		return (double)readAreaCacheReadCount / totalReadCount;
	}

	public double calcCacheDiskHit() {
		return (double)cacheDiskReadCount / totalReadCount;
	}

	public void incrementCounter(COUNTER_TYPE type) {
		if (type.equals(COUNTER_TYPE.WRITE_BUFF)) {
			writeBufferReadCount++;
		} else if (type.equals(COUNTER_TYPE.READ_AREA)) {
			readAreaCacheReadCount++;
		} else if (type.equals(COUNTER_TYPE.CACHE_DISK)) {
			cacheDiskReadCount++;
		}
	}

	public void incrementReadCounter() {
		totalReadCount++;
	}

	public void outputStats() {
		LogCollector.outputRecord("RAPoSDA statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("Total read accesses = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("WriteBuffer read accesses = " + writeBufferReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("WriteBuffer read hit ratio = " + calcWriteBufferCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("ReadAreaCache read accesses = " + readAreaCacheReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("ReadAreaCache read hit ratio = " + calcReadAreaCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheDisk read accesses = " + cacheDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheDIsk read hit ratio = " + calcCacheDiskHit(), LogCollector.OutputType.STATS);
	}

}
