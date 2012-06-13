package sim.datalayout.managed;

import sim.Environment;
import sim.datalayout.managed.CacheDisk;
import sim.datalayout.managed.DataEntry;
import sim.storage.device.model.DiskModel;
import sim.util.DataType;
import junit.framework.TestCase;

public class CacheDiskTest extends TestCase {

//	DiskModel model = new DiskModel("src/test/sim/datalayout/managed/cacheDiskTestModel.xml");
	// capacity = 5

	public void testWriteEntry() {
//		Environment.setCacheDiskModel(model);
//		CacheDisk cDisk = new CacheDisk(1);
//
//		DataEntry entry1 = new DataEntry(1, 1, DataType.N);
//		cDisk.writeEntry(entry1, 1);
//		assertEquals(1, cDisk.getUsage());
//
//		DataEntry entry2 = new DataEntry(2, 1, DataType.N);
//		cDisk.writeEntry(entry2, 2);
//		assertEquals(2, cDisk.getUsage());
//
//		DataEntry entry3 = new DataEntry(3, 3, DataType.N);
//		cDisk.writeEntry(entry3, 3);
//		assertEquals(5, cDisk.getUsage());
//
//		DataEntry entry4 = new DataEntry(4, 1, DataType.N);
//		cDisk.writeEntry(entry4, 4);
//		assertEquals(5, cDisk.getUsage());
//
//		DataEntry entry5 = new DataEntry(5, 1, DataType.N);
//		cDisk.writeEntry(entry5, 5);
//		assertEquals(5, cDisk.getUsage());
//
//		DataEntry entry6 = new DataEntry(6, 1, DataType.N);
//		cDisk.writeEntry(entry6, 6);
//		assertEquals(3, cDisk.getUsage());

	}

	public void testReadEntry() {
//		Environment.setCacheDiskModel(model);
//		CacheDisk cDisk = new CacheDisk(1);
//		DataEntry result = null;
//
//		DataEntry entry1 = new DataEntry(1, 1, DataType.N);
//		cDisk.writeEntry(entry1, 1);
//		result = cDisk.readEntry(entry1.getId(), 2);
//		assertNotNull("キャッシュにあるはずのデータがヒットしませんでした．", result);
//
//		result = cDisk.readEntry(entry1.getId(), 0.5);
//		assertNull("キャッシュ書き込み時間より以前のアクセスでヒットしてしまいました．", result);
//
//		result = cDisk.readEntry(2, 3);
//		assertNull("存在しないはずのキャッシュデータがヒットしてしまいました．", result);
//
//		result = cDisk.readEntry(entry1.getId(), 100);
//		assertNotNull("キャッシュにあるはずのデータがヒットしませんでした．", result);
	}

}
