package sim.datalayout;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import sim.Environment;
import sim.SimParameter;
import sim.SimTimeManager;
import sim.datalayout.factory.LayoutManagerFactory;
import sim.datalayout.factory.RAPoSDALayoutManagerFactory;
import sim.datalayout.managed.DataEntry;
import sim.output.LogCollector;
import sim.storage.StorageManager;
import sim.storage.device.model.DiskModel;
import sim.storage.device.model.MemoryModel;
import sim.util.DataType;

public class RAPoSDALayoutManagerTest extends TestCase {

	private LayoutManager layoutManager = null;

	@Before
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

		SimParameter.init("./test/sim/datalayout/DMTestSimulator.xml");
		SimParameter.setUseCache(true);

		MemoryModel mModel = new MemoryModel("./test/sim/datalayout/DMTestMemoryModel.xml");
		DiskModel dModel = new DiskModel("./test/sim/datalayout/DMTestDiskModel.xml");
		Environment.setMemoryModel(mModel);
		Environment.setDiskModel(dModel);
		Environment.setCacheDiskModel(dModel);
		Environment.setStorageManager(new StorageManager());
		Environment.setSimTimeManager(new SimTimeManager());
		LogCollector.setOutputDir("./test/sim/datalayout");
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

	@Test
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


	@Test
	public void testConstraction() {
		SimParameter.init("./test/sim/datalayout/DMTestSimulator.xml");

		try {
			String managerFactoryName = SimParameter.getLayoutManagerFactory();
			LayoutManagerFactory managerFactory = (LayoutManagerFactory)Class.forName(managerFactoryName).newInstance();

			assertNotNull("RAPoSDALayoutManagerFactoryの生成に失敗しました．", managerFactory);
			assertTrue("生成したFactoryクラスはRAPoSDALayoutManagerFactoryではありませんでした．",
					   (managerFactory instanceof RAPoSDALayoutManagerFactory));

		} catch (InstantiationException e) {
			e.printStackTrace();
			fail("InstantiationException が発生しました．");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("IllegalAccessException が発生しました．");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("ClassNotFoundException が発生しました．");
		}
	}

	@Test
	public void testInit() {
		RAPoSDALayoutManager myLayoutManager = (RAPoSDALayoutManager)this.layoutManager;
		myLayoutManager.init();
		myLayoutManager.debugCreateManagedDevices();
	}

	@Test
	public void testDeviceMapping() {
		RAPoSDALayoutManager myLayoutManager = (RAPoSDALayoutManager)this.layoutManager;
		myLayoutManager.init();
		myLayoutManager.debugDeviceMapping();
	}

	@Test
	public void testReadProcess() {
		RAPoSDALayoutManager myLayoutManager = (RAPoSDALayoutManager)this.layoutManager;

		DataEntry d1 = new DataEntry(1, 1, DataType.NORMAL);
		DataEntry d2 = new DataEntry(2, 1, DataType.NORMAL);
		DataEntry d3 = new DataEntry(3, 1, DataType.NORMAL);

		double result;
		result = myLayoutManager.readProcess(d1, 0.1);
		assertTrue(result < 0.001);
		result = myLayoutManager.readProcess(d2, 0.2);
		assertTrue(result < 0.001);
		result = myLayoutManager.readProcess(d3, 0.3);
		assertTrue(result < 0.001);

		/*
		 * ここの動き結構微妙．primaryサイズとbackupサイズがともに3で，初期処理でサイズ1のデータを3つ
		 * 入れておいた状態（primary,backupともに3データずつキャッシュに入っている）で，新しくサイズ1のデータ
		 * を書き込むと，キャッシュのprimary領域はディスクに書き込まれるが，そのとき同じディスクのbackup領域
		 * データも書き込まれる．キャッシュメモリ，データディスクをそれぞれ1つずつの構成にするとprimaryとbackupは
		 * 常に同じキャッシュメモリと同じデータディスクになってキャッシュのprimary領域データをディスクに書き込む
		 * と同時にキャッシュのbackup領域データもデータディスクに書き込まれる．そのすぐ後でキャッシュのbackup領域
		 * にデータを書き込む動作になっているが，このときはすでにキャッシュのbackup領域は空になっているから
		 * 書き込めてしまう．本当は完全に同時に書き込みにいってprimaryとbackupは両方オーバーフロー処理
		 * をしてほしいのだが，そのように動作させるには根本的なプログラムの改良が必要に思われる．
		 */
		DataEntry d4 = new DataEntry(4, 1, DataType.NORMAL);
		myLayoutManager.decideDestinationDisk(d4);
		myLayoutManager.writeProcess(d4, 0.4);
		result = myLayoutManager.readProcess(d4, 0.41);
		assertTrue(result < 0.001);

		/*
		 * キャッシュメモリのprimary or backup領域から追い出されたデータでread用領域に書き込まれて
		 * いるはずのデータのアクセス性能のチェック
		 */
		// キャッシュ追い出し時には追い出しデータをread用領域にコピーしてからデータディスクへ書き込むため，
		// 応答時間はメモリの速度となるはず
		result = myLayoutManager.readProcess(d1, 0.41);
		assertTrue(result < 0.001);
		// read用領域にコピーされた時刻よりも遅いアクセスの場合，メモリの速度で応答されるはず
		result = myLayoutManager.readProcess(d1, 0.51);
		assertTrue(result < 0.001);
	}

}
