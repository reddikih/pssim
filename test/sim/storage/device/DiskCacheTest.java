package sim.storage.device;

import org.junit.Test;

import junit.framework.TestCase;
import sim.Environment;
import sim.datalayout.managed.DataEntry;
import sim.storage.cache.Cache;
import sim.storage.cache.CacheSource;
import sim.storage.device.DiskCache;
import sim.storage.device.HardDiskDrive;
import sim.storage.device.model.DiskModel;
import sim.storage.device.model.MemoryModel;
import sim.storage.device.state.DiskStateManager;
import sim.util.DataType;

public class DiskCacheTest extends TestCase implements CacheSource {

	private static int CACHE_SIZE = 3;

	private Cache diskCache;
	private DiskModel diskModel;
	private DiskStateManager diskStateManager;
	private HardDiskDrive hdd;

	private void init() {
//		diskModel = new DiskModel("src/test/sim/storage/device/STTestDiskModel.xml");
//		assertNotNull("DiskModel is null!", diskModel);
//		diskStateManager = new DefaultDiskStateManager();
//		hdd = new HardDiskDrive(0, diskModel, diskStateManager);
//		assertNotNull("HardDiskDrive is null!", hdd);

//		diskCache = new DiskCache(hdd, CACHE_SIZE);

		MemoryModel mmodel = new MemoryModel("test/sim/storage/device/STTestMemoryModel.xml");
		Environment.setMemoryModel(mmodel);
		diskCache = new DiskCache(this, CACHE_SIZE);
	}

	@Test
	public void testDiskCacheCacheSourceInt() {
		init();

		assertNotNull("diskCache is null", diskCache);
	}

	@Test
	public void testReadAndWrite() {
		init();

		DataEntry entry;
		entry = (DataEntry)diskCache.write(new DataEntry(1, 1, DataType.NORMAL), 0.001);
		assertTrue("", entry.getResponseTime() > 0.001); // write to the Cache source.
		System.out.println("Response Time(write to disk) : " + entry.getResponseTime());

		// read write test.
		entry = (DataEntry)diskCache.read(new DataEntry(1, 1, DataType.NORMAL), 0.002);
		assertTrue("", entry.getResponseTime() < 0.001); // read from Cache source.
		System.out.println("Response Time(read from cache) : " + entry.getResponseTime());

		entry = (DataEntry)diskCache.write(new DataEntry(2, 1, DataType.NORMAL), 0.003);
		assertTrue("", entry.getResponseTime() > 0.001); // write to the Cache source.
		entry = (DataEntry)diskCache.write(new DataEntry(3, 1, DataType.NORMAL), 0.004);
		assertTrue("", entry.getResponseTime() > 0.001); // write to the Cache source.

		entry = (DataEntry)diskCache.read(new DataEntry(1, 1, DataType.NORMAL), 0.005);
		assertTrue("", entry.getResponseTime() < 0.001); // read from Cache source.
		entry = (DataEntry)diskCache.read(new DataEntry(2, 1, DataType.NORMAL), 0.006);
		assertTrue("", entry.getResponseTime() < 0.001); // read from Cache source.
		entry = (DataEntry)diskCache.read(new DataEntry(3, 1, DataType.NORMAL), 0.007);
		assertTrue("", entry.getResponseTime() < 0.001); // read from Cache source.


		// replace test.
		entry = (DataEntry)diskCache.write(new DataEntry(4, 1, DataType.NORMAL), 0.008); // expect that replaced id 4 to id 1.
		assertTrue("", entry.getResponseTime() > 0.001); // write to the Cache source.
		entry = (DataEntry)diskCache.read(new DataEntry(1, 1, DataType.NORMAL), 0.009);
		assertFalse("", entry.getResponseTime() < 0.001); // read from Cache source because required data was replaced.

	}

	public Object readFromSource(DataEntry data, double accessTime) {
		data.setResponseTime(0.05);
		return data;
	}

	public Object writeToSource(DataEntry data, double arrivalTime) {
		data.setResponseTime(0.05);
		return data;
	}

}
