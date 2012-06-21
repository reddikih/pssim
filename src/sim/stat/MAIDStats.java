package sim.stat;

import sim.output.LogCollector;

public class MAIDStats implements IStatistics {

	private long memoryReadCount;
	private long cacheDiskReadCount;
	private long totalReadCount;


	public double calcMemoryCacheHit() {
		return (double)memoryReadCount / totalReadCount;
	}

	public double calcCacheDiskHit() {
		return (double)cacheDiskReadCount / totalReadCount;
	}

	public void incrementCounter(COUNTER_TYPE type) {
		if (type.equals(COUNTER_TYPE.CACHE_MEMORY)) {
			memoryReadCount++;
		} else if (type.equals(COUNTER_TYPE.CACHE_DISK)) {
			cacheDiskReadCount++;
		}
	}

	public void incrementReadCounter() {
		totalReadCount++;
	}

	public void outputStats() {
		LogCollector.outputRecord("MAID statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("Total read accesses = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheMemory read accesses = " + memoryReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheMemory read hit ratio = " + calcMemoryCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheDisk read accesses = " + cacheDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("CacheDIsk read hit ratio = " + calcCacheDiskHit(), LogCollector.OutputType.STATS);
	}
}
