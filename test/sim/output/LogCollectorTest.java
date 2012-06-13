package sim.output;

import sim.output.LogCollector;
import sim.util.AccessType;
import sim.util.DiskState;
import junit.framework.TestCase;

public class LogCollectorTest extends TestCase {

	public void testOutputRecord() {
		String outputStr = LogCollector.createDataDiskStateRecord(0, 0, 0, DiskState.ACTIVE, 10.0, 10.5, 30.0, AccessType.POSTPROCESS);
		LogCollector.outputRecord(outputStr, LogCollector.OutputType.DATA_DISK);

		outputStr = LogCollector.createDataDiskStateRecord(1, 0, 0, DiskState.ACTIVE, 10.0, 10.5, 30.0, AccessType.POSTPROCESS);
		LogCollector.outputRecord(outputStr, LogCollector.OutputType.DATA_DISK);
	}

}
