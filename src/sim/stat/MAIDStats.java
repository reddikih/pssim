package sim.stat;

import sim.output.LogCollector;

public class MAIDStats extends Statistics {

	@Override
	public void outputStats() {
		LogCollector.outputRecord("MAID statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total read accesses          = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheMemory read accesses    = " + memoryReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheMemory read hit ratio   = " + calcMemoryCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDisk read accesses      = " + cacheDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDIsk read hit ratio     = " + calcCacheDiskHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	DataDisk read accesses       = " + dataDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg memory respnose time     = " + calcAverageMemoryResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg cache disk respnose time = " + calcAverageCacheDiskResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg data disk respnose time  = " + calcAverageDataDiskResponseTime(), LogCollector.OutputType.STATS);
	}
}
