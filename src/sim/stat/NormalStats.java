package sim.stat;

import sim.output.LogCollector;

public class NormalStats extends Statistics {

	@Override
	public void outputStats() {
		LogCollector.outputRecord("Normal statistics", LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Total read accesses          = " + totalReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	DataDisk read accesses       = " + dataDiskReadCount, LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg memory respnose time     = " + calcAverageMemoryResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg cache disk respnose time = " + calcAverageCacheDiskResponseTime(), LogCollector.OutputType.STATS);
		LogCollector.outputRecord("	Avg data disk respnose time  = " + calcAverageDataDiskResponseTime(), LogCollector.OutputType.STATS);
	}

}
