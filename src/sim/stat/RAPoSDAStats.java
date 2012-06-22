package sim.stat;

import sim.output.LogCollector;

public class RAPoSDAStats extends Statistics {

	public void outputStats() {
		LogCollector.outputRecord("RAPoSDA statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total read accesses          = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	WriteBuffer read accesses    = " + writeBufferReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	WriteBuffer read hit ratio   = " + calcWriteBufferCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	ReadAreaCache read accesses  = " + readAreaCacheReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	ReadAreaCache read hit ratio = " + calcReadAreaCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDisk read accesses      = " + cacheDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	CacheDIsk read hit ratio     = " + calcCacheDiskHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	DataDisk read accesses       = " + dataDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg memory respnose time     = " + calcAverageMemoryResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg cache disk respnose time = " + calcAverageCacheDiskResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg data disk respnose time  = " + calcAverageDataDiskResponseTime(), LogCollector.OutputType.STATS);
	}

}
