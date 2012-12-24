package sim;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import sim.Environment;
import sim.SimMain;
import sim.datalayout.RAPoSDALayoutManager;
import junit.framework.TestCase;

public class SimMainTest extends TestCase {

	@Test
	public void testMain() {
		// Usage が標準出力に表示されること
		//main.main(null);

		String[] args = {
				"test/sim/simulator_raposda_2.xml",
				"test/sim/memoryModel.xml",
				"test/sim/dataDiskModel.xml",
				"test/sim/cacheDiskModel.xml",
				"test/sim/workload",
				"on",
				"on",
				"test/sim/out"
				};
		try {
			SimMain.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exceptionが発生しました．");
		}

		RAPoSDALayoutManager layoutManager = (RAPoSDALayoutManager)Environment.getLayoutManager();
		layoutManager.debugCreateManagedDevices();
		layoutManager.debugDeviceMapping();
	}

}
