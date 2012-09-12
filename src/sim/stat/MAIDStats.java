package sim.stat;

import sim.output.LogCollector;

public class MAIDStats extends Statistics {

	@Override
	public void outputStats() {
		LogCollector.outputRecord("MAID statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total read I/O accesses      = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total write I/O accesses     = " + totalWriteCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheMemory read accesses    = " + memoryReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheMemory read hit ratio   = " + calcMemoryCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDisk read accesses      = " + cacheDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDisk read hit ratio     = " + calcCacheDiskHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDisk write accesses     = " + cacheDiskWriteCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	DataDisk read accesses       = " + dataDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	DataDisk write accesses      = " + dataDiskWriteCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg memory response time     = " + calcAverageMemoryResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg cache disk response time = " + calcAverageCacheDiskResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg data disk response time  = " + calcAverageDataDiskResponseTime(), LogCollector.OutputType.STATS);
	}
}
