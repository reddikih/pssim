package sim.storage.device;

import org.junit.Test;

import sim.datalayout.MAIDLayoutManager;
import sim.datalayout.managed.DataEntry;
import sim.storage.cache.CacheSource;
import sim.storage.device.MAIDCacheMemory;
import sim.util.DataType;
import junit.framework.TestCase;

public class MAIDCacheMemoryTest extends TestCase {

	private MAIDCacheMemory maidCache;

	public void init(long capacity, int divid) {
		MocSource source = new MocSource();
		this.maidCache = new MAIDCacheMemory(source, capacity, divid);
	}

	@Test
	public void testConstruct() {
		long capacity = 5;
		init(5, 0);

		assertEquals(capacity, maidCache.getMaxCacheSize());
		assertEquals(0, maidCache.getUsingSize());
	}

	@Test
	public void testReadAndWrite() {
//		DataEntry data1 = new DataEntry(1, 1, DataType.N);
//		DataEntry data2 = new DataEntry(2, 1, DataType.N);
//
//		maidCache.write(data1, 1);
//		currentSize = this.cache.getCurrentSize();
//		assertEquals("current size is invalid.", 1, currentSize);
//		this.cache.write(data2, 2);
//		currentSize = this.cache.getCurrentSize();
//		assertEquals("current size is invalid.", 2, currentSize);
//
//		DataEntry read1 = (DataEntry)this.cache.read(data1, 3);
//		assertTrue("read data is not correct.", read1.equals(data1));
//		DataEntry data3 = new DataEntry(3, 1, DataType.N);
//		assertNull("read data is not correct.", this.cache.read(data3, 4));
	}

	private class MocSource extends MAIDLayoutManager {
		public MocSource() {
			super();
		}

		@Override
		public Object readFromSource(DataEntry entry, double accessTime) {
			return entry;
		}
		@Override
		public Object writeToSource(DataEntry entry, double arrivalTime) {
			return entry;
		}
	}
}
