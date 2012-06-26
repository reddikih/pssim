package scenario.maid;

import sim.Environment;
import sim.SimParameter;
import sim.SimTimeManager;
import sim.datalayout.LayoutManager;
import sim.datalayout.RAPoSDALayoutManager;
import sim.datalayout.factory.LayoutManagerFactory;
import sim.datalayout.managed.DataEntry;
import sim.output.LogCollector;
import sim.storage.StorageManager;
import sim.storage.device.model.DiskModel;
import sim.storage.device.model.MemoryModel;
import sim.util.DataType;
import junit.framework.TestCase;

public class MAIDLayoutManagerTest extends TestCase {

	private LayoutManager layoutManager = null;

	@Override
	protected void setUp() throws Exception {
		/*
		 * Cachememoryのスペック
		 * 総容量: 12
		 * 遅延: 0.000005s(=5 micro sec)
		 * read用領域の割合： 0.5 (= サイズは6)
		 * レプリカの数： 2 (primary-backup構成)
		 * primary領域サイズ： 3
		 * backup領域サイズ: 3
		 * ディスクのキャッシュサイズ: 1
		 */

		SimParameter.init("./test/scenario/maid/DMTestSimulator.xml");
		SimParameter.setUseCache(true);

		MemoryModel mModel = new MemoryModel("./test/scenario/maid/DMTestMemoryModel.xml");
		DiskModel dModel = new DiskModel("./test/scenario/maid/DMTestDiskModel.xml");
		Environment.setMemoryModel(mModel);
		Environment.setDiskModel(dModel);
		Environment.setCacheDiskModel(dModel);
		Environment.setStorageManager(new StorageManager());
		Environment.setSimTimeManager(new SimTimeManager());
		LogCollector.setOutputDir("./test/scenario/maid");
		LogCollector.LoggerInit();



		String managerFactoryName = SimParameter.getLayoutManagerFactory();
		LayoutManagerFactory managerFactory = null;
		try {
			managerFactory = (LayoutManagerFactory)Class.forName(managerFactoryName).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ファクトリーメソッドでのオブジェクト生成に失敗しました．");
		}

		layoutManager = managerFactory.create();
		Environment.setLayoutManager(layoutManager);
		writeInitialData();
	}

	private void writeInitialData() {
		RAPoSDALayoutManager myLayoutManager = (RAPoSDALayoutManager)this.layoutManager;

		DataEntry d1 = new DataEntry(1, 1, DataType.NORMAL);
		DataEntry d2 = new DataEntry(2, 1, DataType.NORMAL);
		DataEntry d3 = new DataEntry(3, 1, DataType.NORMAL);

		myLayoutManager.decideDestinationDisk(d1);
		myLayoutManager.writeProcess(d1, 0.01);
		myLayoutManager.decideDestinationDisk(d2);
		myLayoutManager.writeProcess(d2, 0.02);
		myLayoutManager.decideDestinationDisk(d3);
		myLayoutManager.writeProcess(d3, 0.03);
		// ここまででキャッシュメモリのprimaryとbackup領域は満杯になる．
	}

	public void testWriteProcess() {
		fail("Not yet implemented");
	}

	public void testReadProcess() {
		fail("Not yet implemented");
	}

}
