package sim.stat;

import sim.output.LogCollector;

public class NormalStats extends Statistics {

	@Override
	public void outputStats() {
		LogCollector.outputRecord("Normal statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total read I/O accesses      = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total write I/O accesses     = " + totalWriteCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	WriteBuffer read accesses    = " + writeBufferReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	WriteBuffer read hit ratio   = " + calcWriteBufferCacheHit(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	ReadAreaCache read accesses  = " + readAreaCacheReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	ReadAreaCache read hit ratio = " + calcReadAreaCacheHit(), LogCollector.OutputType.STATS);
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
