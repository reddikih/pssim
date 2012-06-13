package sim;

import sim.Environment;
import sim.SimMain;
import sim.datalayout.RAPoSDALayoutManager;
import junit.framework.TestCase;

public class SimMainTest extends TestCase {

	public void testMain() {
		// Usage が標準出力に表示されること
		//main.main(null);

		String[] args = {"simulator.xml", "memoryModel.xml", "dataDiskModel.xml", "workload"};
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

	public void testSetUp() {
		fail("まだ実装されていません");
	}

	public void testExecute() {
		fail("まだ実装されていません");
	}

	public void testPostProcess() {
		fail("まだ実装されていません");
	}

}
