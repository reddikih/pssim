import sim.SimMainTest;
import sim.datalayout.RAPoSDALayoutManagerTest;
import sim.datalayout.managed.CacheDiskTest;
import sim.datalayout.managed.CacheMemoryTest;
import sim.datalayout.managed.DataDiskTest;
import sim.datalayout.partitioning.RoundRobinPartitioningTest;
import sim.output.LogCollectorTest;
import sim.storage.cache.CacheEntryTest;
import sim.storage.cache.LRUCacheTest;
import sim.storage.device.DiskCacheTest;
import sim.storage.device.HardDiskDriveTest;
import sim.storage.device.MAIDCacheMemoryTest;
import sim.storage.device.MemoryTest;
import sim.storage.device.model.DiskModelTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SimMainTest.class,
	RAPoSDALayoutManagerTest.class,
	CacheDiskTest.class,
	CacheMemoryTest.class,
	DataDiskTest.class,
	RoundRobinPartitioningTest.class,
	LogCollectorTest.class,
	CacheEntryTest.class,
	LRUCacheTest.class,
	DiskCacheTest.class,
	HardDiskDriveTest.class,
	MAIDCacheMemoryTest.class,
	MemoryTest.class,
	DiskModelTest.class
})

public class AllTests {}
