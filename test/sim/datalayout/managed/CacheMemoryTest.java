package sim.datalayout.managed;

import java.util.List;

import sim.datalayout.managed.CacheMemory;
import sim.datalayout.managed.DataEntry;
import sim.util.DataType;
import sim.util.ReplicaType;
import junit.framework.TestCase;

public class CacheMemoryTest extends TestCase {

	int id = 1;
	long capacity = 100;
	long threshold = 40;

	public void testCacheMemory() {
		CacheMemory cache = new CacheMemory(this.id, this.threshold, this.capacity);
		assertNotNull("CacheMemoryの生成に失敗しました．", cache);

		assertSame(this.id, cache.getId());
	}

	public void testConstructor() {
		int id = 1;
		long threshold = 10;
		long capacity = 10;
		double ratioReadArea = 0.5;
		int numReplica = 2;

		CacheMemory cache = new CacheMemory(id, threshold, capacity, ratioReadArea, numReplica);

		assertEquals(2, cache.getPrimaryCapacity());
		/* backup capa = buff area - primary capa だから，端数がある場合
		   backup capa は primary capa より大きくなることがある */
		assertEquals(3, cache.getBackupCapacity());
		assertEquals(5, cache.getReadArea());
	}

	public void testWriteCacheWithReadArea() {
		int id = 1;
		long threshold = 10;
		long capacity = 10;
		double ratioReadArea = 0.5;
		int numReplica = 2;

		CacheMemory cache = new CacheMemory(id, threshold, capacity, ratioReadArea, numReplica);

		//// for primary area ////
		DataEntry p1 = new DataEntry(10, 1, DataType.NORMAL);
		DataEntry p2 = new DataEntry(11, 1, DataType.NORMAL);
		DataEntry p3 = new DataEntry(12, 1, DataType.NORMAL);

		boolean result;
		result = cache.writeCacheData(p1, ReplicaType.PRIMARY);
		assertTrue(result);
		result = cache.writeCacheData(p2, ReplicaType.PRIMARY);
		assertTrue(result);
		result = cache.writeCacheData(p3, ReplicaType.PRIMARY);
		assertFalse(result);


		//// for backup area ////
		DataEntry b1 = new DataEntry(20, 1, DataType.NORMAL);
		DataEntry b2 = new DataEntry(21, 1, DataType.NORMAL);
		DataEntry b3 = new DataEntry(22, 1, DataType.NORMAL);
		DataEntry b4 = new DataEntry(23, 1, DataType.NORMAL);

		result = cache.writeCacheData(b1, ReplicaType.BACKUP);
		assertTrue(result);
		result = cache.writeCacheData(b2, ReplicaType.BACKUP);
		assertTrue(result);
		result = cache.writeCacheData(b3, ReplicaType.BACKUP);
		assertTrue(result);
		result = cache.writeCacheData(b4, ReplicaType.BACKUP);
		assertFalse(result);
	}

	public void testWriteToReadArea() {
		int id = 1;
		long threshold = 10;
		long capacity = 10;
		double ratioReadArea = 0.5;
		int numReplica = 2;

		CacheMemory cache = new CacheMemory(id, threshold, capacity, ratioReadArea, numReplica);
		// read領域はサイズ5． refer to testConstructor()

		// read領域に対する処理
		DataEntry r1 = new DataEntry(30, 1, DataType.NORMAL);
		DataEntry r2 = new DataEntry(31, 1, DataType.NORMAL);
		DataEntry r3 = new DataEntry(32, 1, DataType.NORMAL);
		DataEntry r4 = new DataEntry(33, 1, DataType.NORMAL);
		DataEntry r5 = new DataEntry(34, 1, DataType.NORMAL);
		DataEntry r6 = new DataEntry(35, 1, DataType.NORMAL);

		DataEntry result;
		cache.writeToReadArea(r1, 0.1);
		result = cache.readFromReadArea(r1.getId(),0.11);
		assertNotNull(result);
		cache.writeToReadArea(r2, 0.2);
		result = cache.readFromReadArea(r2.getId(), 0.21);
		assertNotNull(result);
		cache.writeToReadArea(r3, 0.3);
		result = cache.readFromReadArea(r3.getId(), 0.31);
		assertNotNull(result);
		cache.writeToReadArea(r4, 0.4);
		result = cache.readFromReadArea(r4.getId(), 0.41);
		assertNotNull(result);
		cache.writeToReadArea(r5, 0.5);
		result = cache.readFromReadArea(r5.getId(), 0.51);
		assertNotNull(result);

		// キャッシュに書き込まれる時間よりも前の時間ならキャッシュミスするはず
		result = cache.readFromReadArea(r5.getId(), 0.50);
		assertNull(result);
		// キャッシュに書き込まれる時間と同じ時間でもキャッシュミスするはず
		result = cache.readFromReadArea(r5.getId(), 0.51);
		assertNull(result);

		// r6をwriteしたらLRUでr1が置換されるはず
		// readFromReadArea()でキャッシュミスした場合戻り値はnull
		cache.writeToReadArea(r6, 0.6);
		result = cache.readFromReadArea(r1.getId(), 0.61);
		assertNull(result);

		// サイズ2のr7をwriteする．
		// コードが正しければr2とr3が追い出されるはず
		DataEntry r7 = new DataEntry(36, 2, DataType.NORMAL);
		cache.writeToReadArea(r7, 0.7);
		result = cache.readFromReadArea(r2.getId(), 0.71);
		assertNull(result);
		result = cache.readFromReadArea(r3.getId(), 0.72);
		assertNull(result);
		result = cache.readFromReadArea(r7.getId(), 0.73);
		assertNotNull(result);
	}

	public void testWriteCacheData() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		// プライマリ領域に対する処理
		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry p2 = new DataEntry(101, 20, DataType.NORMAL);
		DataEntry p3 = new DataEntry(102, 10, DataType.NORMAL);
		DataEntry p4 = new DataEntry(103, 1, DataType.NORMAL);

		boolean result = cache.writeCacheData(p1, ReplicaType.PRIMARY);
		assertTrue("プライマリキャッシュ(" + p1.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(p2, ReplicaType.PRIMARY);
		assertTrue("プライマリキャッシュ(" + p2.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(p3, ReplicaType.PRIMARY);
		assertTrue("プライマリキャッシュ(" + p3.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(p4, ReplicaType.PRIMARY);
		assertFalse("プライマリキャッシュ(" + p4.getId() + ")の書き込み制限処理がおかしいです．", result);


		// ここからバックアップ領域に対する処理
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);
		DataEntry b2 = new DataEntry(201, 20, DataType.NORMAL);
		DataEntry b3 = new DataEntry(202, 10, DataType.NORMAL);
		DataEntry b4 = new DataEntry(203, 1, DataType.NORMAL);

		result = cache.writeCacheData(b1, ReplicaType.BACKUP);
		assertTrue("バックアップキャッシュ(" + b1.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(b2, ReplicaType.BACKUP);
		assertTrue("バックアップキャッシュ(" + b2.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(b3, ReplicaType.BACKUP);
		assertTrue("バックアップキャッシュ(" + b3.getId() + ")の書き込みに失敗しました．", result);
 		result = cache.writeCacheData(b4, ReplicaType.BACKUP);
		assertFalse("バックアップキャッシュ(" + b4.getId() + ")の書き込み制限処理がおかしいです．", result);
	}


	public void testReadCacheData() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);

		cache.writeCacheData(p1, ReplicaType.PRIMARY);
		cache.writeCacheData(b1, ReplicaType.BACKUP);

		DataEntry result = null;

		// 正常系
		result = cache.readCacheData(p1.getId(), ReplicaType.PRIMARY);
		assertNotNull("プライマリキャッシュ（" + p1.getId() + "）の読み出しに失敗しました．", result);
		result = cache.readCacheData(b1.getId(), ReplicaType.BACKUP);
		assertNotNull("バックアップキャッシュ（" + b1.getId() + "）の読み出しに失敗しました．", result);

		// 異常系 その１
		result = cache.readCacheData(p1.getId(), ReplicaType.BACKUP);
		assertNull("読み出せないはずのプライマリキャッシュ（" + p1.getId() + "）が読み出せてしまいました．", result);
		result = cache.readCacheData(b1.getId(), ReplicaType.PRIMARY);
		assertNull("読み出せないはずのバックアップキャッシュ（" + b1.getId() + "）が読み出せてしまいました．", result);

		// 異常系 その２
		long notExist = 300;
		result = cache.readCacheData(notExist, ReplicaType.PRIMARY);
		assertNull("存在しないはずのプライマリキャッシュ（" + notExist + "）が読み出せてしまいました．", result);
		result = cache.readCacheData(notExist, ReplicaType.BACKUP);
		assertNull("存在しないはずのバックアップキャッシュ（" + notExist + "）が読み出せてしまいました．", result);
	}

	public void testIsHit() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);

		cache.writeCacheData(p1, ReplicaType.PRIMARY);
		cache.writeCacheData(b1, ReplicaType.BACKUP);

		boolean result = false;

		// 正常系
		result = cache.isHit(p1.getId(), ReplicaType.PRIMARY);
		assertTrue("キャッシュヒットするはずがしていません．(" + p1.getId() + ")", result);
		result = cache.isHit(b1.getId(), ReplicaType.BACKUP);
		assertTrue("キャッシュヒットするはずがしていません．(" + b1.getId() + ")", result);

		// 異常系 その１
		result = cache.isHit(p1.getId(), ReplicaType.BACKUP);
		assertFalse("ヒットしないはずのプライマリキャッシュ（" + p1.getId() + "）がヒットしてしまいました．", result);
		result = cache.isHit(b1.getId(), ReplicaType.PRIMARY);
		assertFalse("ヒットしないはずのバックアップキャッシュ（" + b1.getId() + "）がヒットしてしまいました．", result);

		// 異常系 その２
		long notExist = 300;
		result = cache.isHit(notExist, ReplicaType.PRIMARY);
		assertFalse("存在しないはずのプライマリキャッシュ（" + notExist + "）がヒットしてしまいました．", result);
		result = cache.isHit(notExist, ReplicaType.BACKUP);
		assertFalse("存在しないはずのバックアップキャッシュ（" + notExist + "）がヒットしてしまいました．", result);
	}

	public void testRemoveCacheData() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry p2 = new DataEntry(101, 20, DataType.NORMAL);
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);
		DataEntry b2 = new DataEntry(201, 20, DataType.NORMAL);

		cache.writeCacheData(p1, ReplicaType.PRIMARY);
		cache.writeCacheData(b1, ReplicaType.BACKUP);
		cache.writeCacheData(p2, ReplicaType.PRIMARY);
		cache.writeCacheData(b2, ReplicaType.BACKUP);

		DataEntry result = null;
		long before = 0;
		long after = 0;

		// 正常系
		before = cache.getUsageVolume(ReplicaType.PRIMARY);
		result = cache.removeCacheData(p1.getId(), ReplicaType.PRIMARY);
		after = cache.getUsageVolume(ReplicaType.PRIMARY);

		assertNotNull("削除処理が不正" + p1.getId(), result);
		assertSame(p1.getId(), result.getId());
		assertSame(after, before - result.getSize());

		assertTrue("余計なデータも削除しているかもしれない．"+ p2.getId(), cache.isHit(p2.getId(), ReplicaType.PRIMARY));
		assertTrue("余計なデータも削除しているかもしれない．"+ b1.getId(), cache.isHit(b1.getId(), ReplicaType.BACKUP));

	}

	public void testIsUnderThreshold() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		boolean result = false;

		// プライマリ領域に対する処理
		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry p2 = new DataEntry(101, 20, DataType.NORMAL);
		DataEntry p3 = new DataEntry(102, 5, DataType.NORMAL);

		cache.writeCacheData(p1, ReplicaType.PRIMARY);
		result = cache.isUnderThreshold(ReplicaType.PRIMARY);
		assertTrue("閾値処理が不正っぽいです．", result);
		cache.writeCacheData(p2, ReplicaType.PRIMARY);
		result = cache.isUnderThreshold(ReplicaType.PRIMARY);
		assertTrue("閾値処理が不正っぽいです．", result);
		cache.writeCacheData(p3, ReplicaType.PRIMARY);
		result = cache.isUnderThreshold(ReplicaType.PRIMARY);
		assertFalse("閾値処理が不正っぽいです．", result);

		// バックアップ領域に対する処理
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);
		DataEntry b2 = new DataEntry(201, 20, DataType.NORMAL);
		DataEntry b3 = new DataEntry(202, 5, DataType.NORMAL);

		cache.writeCacheData(b1, ReplicaType.BACKUP);
		result = cache.isUnderThreshold(ReplicaType.BACKUP);
		assertTrue("閾値処理が不正っぽいです．", result);
		cache.writeCacheData(b2, ReplicaType.BACKUP);
		result = cache.isUnderThreshold(ReplicaType.BACKUP);
		assertTrue("閾値処理が不正っぽいです．", result);
		cache.writeCacheData(b3, ReplicaType.BACKUP);
		result = cache.isUnderThreshold(ReplicaType.BACKUP);
		assertFalse("閾値処理が不正っぽいです．", result);
	}

	public void testGetCacheLines() {
		CacheMemory cache = new CacheMemory(1, this.threshold, this.capacity);

		DataEntry p1 = new DataEntry(100, 20, DataType.NORMAL);
		DataEntry p2 = new DataEntry(101, 20, DataType.NORMAL);
		DataEntry[] pArray = {p1, p2};
		DataEntry b1 = new DataEntry(200, 20, DataType.NORMAL);
		DataEntry b2 = new DataEntry(201, 20, DataType.NORMAL);
		DataEntry[] bArray = {b1, b2};

		cache.writeCacheData(p1, ReplicaType.PRIMARY);
		cache.writeCacheData(p2, ReplicaType.PRIMARY);
		cache.writeCacheData(b1, ReplicaType.BACKUP);
		cache.writeCacheData(b2, ReplicaType.BACKUP);

		// Primaryデータの確認
		List<DataEntry> entries = cache.getCacheLines(ReplicaType.PRIMARY);
		for (int i = 0; i < entries.size(); i++) {
			assertSame(entries.get(i).getId(), pArray[i].getId());
		}

		// Backupデータの確認
		// TODO Map.values()で返されるCollection<V>はまMapへの挿入順を保持してはいない．
		// 単純に配列と比較してもだめだから，本当はSortメソッドをどこかで実装しておいた方がよさそう．
		entries = cache.getCacheLines(ReplicaType.BACKUP);
		for (int i = 0; i < entries.size(); i++) {
			assertSame(entries.get(i).getId(), bArray[i].getId());
		}
	}

}
