package sim.storage.cache;

import sim.datalayout.managed.DataEntry;
import sim.storage.cache.CacheSource;
import sim.storage.cache.LRUCache;
import sim.util.DataType;
import junit.framework.TestCase;

public class LRUCacheTest extends TestCase implements CacheSource {

	private LRUCache cache;

	private void init(long capacity) {
		this.cache = new LRUCache(this, capacity);
	}

	public void testReadAndWrite() {
		long capacity = 5;
		long currentSize = 0;

		init(capacity);

		DataEntry data1 = new DataEntry(1, 1, DataType.NORMAL);
		DataEntry data2 = new DataEntry(2, 1, DataType.NORMAL);

		this.cache.write(data1, 1);
		currentSize = this.cache.getCurrentSize();
		assertEquals("current size is invalid.", 1, currentSize);
		this.cache.write(data2, 2);
		currentSize = this.cache.getCurrentSize();
		assertEquals("current size is invalid.", 2, currentSize);

		DataEntry read1 = (DataEntry)this.cache.read(data1, 3);
		assertTrue("read data is not correct.", read1.equals(data1));
		DataEntry data3 = new DataEntry(3, 1, DataType.NORMAL);
		assertNull("read data is not correct.", this.cache.read(data3, 4));
	}

	public void testLRUStack() {
		long capacity = 5;
		long currentSize = 0;
		init(capacity);

		DataEntry data1 = new DataEntry(1, 1, DataType.NORMAL);
		DataEntry data2 = new DataEntry(2, 1, DataType.NORMAL);
		DataEntry data3 = new DataEntry(3, 1, DataType.NORMAL);
		DataEntry data4 = new DataEntry(4, 1, DataType.NORMAL);
		DataEntry data5 = new DataEntry(5, 1, DataType.NORMAL);
		DataEntry data6 = new DataEntry(6, 1, DataType.NORMAL);

		cache.write(data1, 0.1);
		cache.write(data2, 0.2);
		cache.write(data3, 0.3);
		cache.write(data4, 0.4);
		cache.write(data5, 0.5);
		currentSize = this.cache.getCurrentSize();
		assertEquals("current size is invalid.", capacity, currentSize);
		cache.write(data6, 0.6);
		currentSize = this.cache.getCurrentSize();
		assertEquals("current size is invalid.", capacity, currentSize);
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
