package sim.storage.device;

import java.lang.reflect.Method;

import sim.storage.device.HardDiskDrive;
import sim.storage.device.model.DiskModel;
import sim.storage.device.state.DefaultDiskStateManager;
import sim.util.DiskState;
import junit.framework.TestCase;

public class HardDiskDriveTest extends TestCase {

//	public void testConstruction() {
//		DiskModel model = new DiskModel("src/sim/resources/dataDiskModel.xml");
//		HardDiskDrive hdd = new HardDiskDrive(0, model, new DefaultDiskStateManager());
//		assertNotNull("HardDiskDrive の生成に失敗しました．", hdd);
//	}
//
//	public void testCalculateServiceTime() {
//		try {
//			Method m = HardDiskDrive.class.getDeclaredMethod("calculateServiceTime", int.class);
//			m.setAccessible(true);
//
//			DiskModel model = new DiskModel("src/sim/resources/dataDiskModel.xml");
//			HardDiskDrive hdd = new HardDiskDrive(0, model, new DefaultDiskStateManager());
//			model = hdd.getModel();
//
//			double fsst = model.getFullStrokeSeekTime();
//			double fdrt = 1.0 / (model.getRpm() / 60.0);
//			int sec_per_track = model.getSectorsPerTrack();
//			double overhead = model.getHeadSwitchTime() + model.getCommandOverhead();
//			int reqSize = 64000;
//			double trans_rate = (double)reqSize / model.getTransferRate();
//
//			double serviceTime = (fsst / 2) + (fdrt / 2) + (fdrt / sec_per_track) + overhead + trans_rate;
//			assertEquals(serviceTime, m.invoke(hdd, reqSize));
//
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			if (e != null && e.getCause() instanceof RuntimeException) return;
//			e.printStackTrace();
//			fail("Exceptionが発生しました．");
//		}
//	}
//
//	public void testCalculateQueueingTime() {
//		DiskModel model = new DiskModel("src/sim/resources/dataDiskModel.xml");
//		HardDiskDrive hdd = new HardDiskDrive(0, model, new DefaultDiskStateManager());
//
//		double initTime = 3.0;
//
//		try {
////			Method mAddLastAccessTime = HardDiskDrive.class.getDeclaredMethod("addLastAccessTime", double.class);
////			mAddLastAccessTime.setAccessible(true);
////			mAddLastAccessTime.invoke(hdd, initTime);
//
//
//			Method mCalculateQueueingTime = HardDiskDrive.class.getDeclaredMethod("calculateQueueingTime", double.class);
//			mCalculateQueueingTime.setAccessible(true);
//
//			double accessTime = 1.0;
//			assertEquals(initTime - accessTime, mCalculateQueueingTime.invoke(hdd, accessTime));
//
//			accessTime = initTime;
//			assertEquals(0.0, mCalculateQueueingTime.invoke(hdd, accessTime));
//
//			accessTime += 0.1;
//			assertEquals(0.0, mCalculateQueueingTime.invoke(hdd, accessTime));
//
//		} catch (RuntimeException e) {
//			return ;
//		} catch (Exception e) {
//			if (e != null && e.getCause() instanceof RuntimeException) return;
//			e.printStackTrace();
//			fail("Exceptionが発生しました．");
//		}
//	}

}
