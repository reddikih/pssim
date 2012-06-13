package sim.storage.device.model;

import java.lang.reflect.Method;

import sim.storage.device.model.DiskModel;
import junit.framework.TestCase;

public class DiskModelTest extends TestCase {

// 設定ファイルから設定情報を読み取り，その値に応じて処理を行うような場合のうまい単体テスト
// 方法はどんなのがあるか？JUnitを使って効率良くやりたいから要調査である．
// このテストは，以前DiskModelで実装していたプライベートメソッドであるparseCapacity()の単体テスト
// である．結果は正常だったので，今はSimUtility.parseSize()にそのまま移動している．
//

	DiskModel model;

	public void setUp() {
		model = new DiskModel("./test/sim/storage/device/model/testDiskModel.xml");
	}


	public void testCacheSize() {
		int cacheSize = model.getCacheSize();
		assertTrue(cacheSize > (32*1024)); // more than 32KB
		assertTrue(cacheSize == (32*1024*1024)); // 32MB
	}

//	public void testParseCapacity() {
//
//		Method m;
//		try {
//			m = DiskModel.class.getDeclaredMethod("parseCapacity", String.class);
//			m.setAccessible(true);
//
//			DiskModel model = new DiskModel("src/test/sim/storage/device/model/testDiskModel.xml");
//
//			String str1 = "32GB";
//			String str2 = "1M";
//			String str3 = "2TB";
//
//			long long1 = (Long)m.invoke(model, str1);
//			assertEquals(34359738368L, long1); // 32 * (1024)^3と比較
//			long long2 = (Long)m.invoke(model, str2);
//			assertEquals(1048576L, long2); // 1 * (1024)^2 と比較
//			long long3 = (Long)m.invoke(model, str3);
//			assertEquals(2199023255552L, long3);	// 2 * (1024)^4 と比較
//
//			// 異常系テスト
//			str1 = "a32GB";
//			str2 = "mb";
//			str3 = "";
//
//			long1 = (Long)m.invoke(model, str1);
//			assertEquals(-1, long1);
//			long2 = (Long)m.invoke(model, str2);
//			assertEquals(-1, long2);
//			long3 = (Long)m.invoke(model, str3);
//			assertEquals(-1, long3);
//
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			if (e != null && e.getCause() instanceof RuntimeException) return;
//			e.printStackTrace();
//			fail("Exceptionが発生しました．");
//		}
//	}

	public void testGetCapacity() {
//		DiskModel model = new DiskModel("src/test/sim/storage/device/model/testDiskModel.xml");
//		// testDiskModel.xml ではcapacityの値を18GBにしているものとする．
//		// 他の数値で確認する場合は，xmlファイルの該当パラメータを変更すること
//		long expected = 18 * 1024 * 1024;
//		assertEquals(expected, model.getCapacity());
	}

}
