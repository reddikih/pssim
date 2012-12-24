package sim.storage.cache;

import org.junit.Test;

import sim.datalayout.managed.DataEntry;
import sim.storage.cache.CacheEntry;
import sim.util.DataType;
import junit.framework.TestCase;

public class CacheEntryTest extends TestCase {

	@Test
	public void testHashCode() {
		CacheEntry data1 = new CacheEntry(new DataEntry(1, 1, DataType.NORMAL), 0.1);
		CacheEntry data2 = new CacheEntry(new DataEntry(2, 2, DataType.NORMAL), 0.2);
		CacheEntry data3 = new CacheEntry(new DataEntry(1, 3, DataType.NORMAL), 0.3);
		CacheEntry data4 = new CacheEntry(new DataEntry(2, 2, DataType.INITIAL), 0.4);

		assertFalse("hash codes should not to be the same!!.",
				data1.hashCode() == data2.hashCode());
		assertFalse("hash codes should not to be the same!!.",
				data1.hashCode() == data3.hashCode());
		assertTrue("hash codes should be the same!!.",
				data2.hashCode() == data4.hashCode());

		for (int i = 0; i < 10; i++) {
			assertTrue("hash codes should be the same!!.",
					data2.hashCode() == data4.hashCode());
		}
	}

	@Test
	public void testEqualsObject() {
		CacheEntry data1 = new CacheEntry(new DataEntry(1, 1, DataType.NORMAL), 0.1);
		CacheEntry data2 = new CacheEntry(new DataEntry(2, 2, DataType.NORMAL), 0.2);
		CacheEntry data3 = new CacheEntry(new DataEntry(1, 3, DataType.NORMAL), 0.3);
		CacheEntry data4 = new CacheEntry(new DataEntry(2, 2, DataType.INITIAL), 0.4);
		CacheEntry data5 = new CacheEntry(new DataEntry(2, 2, DataType.NORMAL), 0.5);

		// verify reflexive property
		assertTrue("reflexive is invalid.", data1.equals(data1));
		assertTrue("reflexive is invalid.", data2.equals(data2));

		// verify symmetric property
		assertTrue("symmetric is invalid", data2.equals(data4) && data4.equals(data2));
		assertFalse("symmetric is invalid", data2.equals(data3) && data3.equals(data2));
		assertFalse("symmetric is invalid", data1.equals(data3) && data3.equals(data1));

		// verify transitive property
		assertTrue("transitive is invalid",
				data2.equals(data4) && data4.equals(data5) && data2.equals(data5));

		// verify consistent property
		for (int i = 0; i < 10; i++) {
			assertTrue("consistent is invalid", data2.equals(data4));
			assertFalse("consistent is invalid", data1.equals(data2));
		}

		// verify non-null property
		assertFalse("non null is invalid", data1.equals(null));
	}

}
