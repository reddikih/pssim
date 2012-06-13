package sim.storage.device;

import sim.storage.device.Memory;
import sim.storage.device.model.MemoryModel;
import junit.framework.TestCase;

public class MemoryTest extends TestCase {

//	public void testMemory() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//		assertNotNull("Memoryの生成に失敗しました．", memory);
//	}
//
//	public void testClear() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		long data = 1;
//		int size = 32768;
//
//		int result = memory.write(data, size);
//		assertEquals(size, memory.getUsageVolume());
//
//		memory.clear();
//		assertEquals(0, memory.getUsageVolume());
//	}
//
//	public void testWrite() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		long dataId = 1;
//		int size = 8192; // 8KB
//
//		// 書き込み正常ケース
//		int result = memory.write(dataId, size);
//		assertEquals(size, result);
//
//		// 書き込み異常ケース
//		memory.clear();
//		long capacity = memory.getCapacity();
//		long divided = capacity / Integer.MAX_VALUE;
//		for (long i = 0; i < divided; i++) {
//			result = memory.write(i, Integer.MAX_VALUE);
//			assertNotSame(-1, result);
//		}
//		divided += 1;
//		result = memory.write(divided, Integer.MAX_VALUE);
//		assertEquals(-1, result);
//	}
//
//	public void testRead() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		long dataId = 1;
//		int size = 8192; // 8KB
//
//		// 書き込みOKなのはテスト済み
//		int result = memory.write(dataId, size);
//
//		// 読み出し正常ケース
//		result = memory.read(dataId);
//		assertEquals(result, size);
//
//		// 読み出し異常ケース
//		result = memory.read(dataId + 1);
//		assertEquals(-1, result);
//	}
//
//	public void testRemove() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		int size = 8192; // 8KB
//
//		// data id = 1 の書き込み
//		int result = memory.write(1L, size);
//		// data id = 2 の書き込み
//		result = memory.write(2L, size);
//
//		assertEquals(size*2, memory.getUsageVolume());
//
//		// data id = 1 のREMOVE
//		memory.remove(1);
//		assertEquals(size, memory.getUsageVolume());
//		result = memory.read(1);
//		assertEquals(-1, result);
//	}
//
//
//	public void testIsHit() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		int size = 8192; // 8KB
//		// data id = 1 の書き込み
//		int result = memory.write(1L, size);
//		// data id = 2 の書き込み
//		result = memory.write(2L, size);
//
//
//		// キャッシュヒットのケース
//		assertTrue("expect true, but it's false.", memory.isHit(1L));
//
//		// キャッシュミスのケース
//		assertFalse("expect false, but it's true.", memory.isHit(3L));
//	}
//
//	public void testIsWritable() {
//		MemoryModel model = new MemoryModel("src/sim/resources/memoryModel.xml");
//		Memory memory = new Memory(0, model);
//
//		int size = 8192;
//		// 書き込み可能ケース (初期状態時)
//		assertTrue("expect true, but it's false.", memory.isWritable(size));
//
//		// 書き込み可能ケース（空きスペースがあるとき）
//		memory.write(1, size);
//		assertTrue("expect true, but it's false.", memory.isWritable(size));
//
//		// 書き込み可能ケース（容量境界値）
//		memory.clear();
//		long capacity = memory.getCapacity();
//		long divided = capacity / Integer.MAX_VALUE;
//		for (long i = 0; i < divided - 1; i++) {
//			memory.write(i, Integer.MAX_VALUE);
//		}
//		assertTrue("expect true, but it's false.", memory.isWritable(Integer.MAX_VALUE));
//
//		divided += 1;
//		memory.write(divided, Integer.MAX_VALUE);
//
//		// 書き込み不可能ケース（容量境界値）
//		assertFalse("expect false, but it's true.", memory.isWritable(Integer.MAX_VALUE));
//
//		// 書き込み不可能ケース（容量オーバー）
//		memory.remove(divided);
//		memory.write(divided, Integer.MAX_VALUE - 1);
//		assertTrue("expect true, but it's false.", memory.isWritable(1));
//		assertFalse("expect false, but it's true.", memory.isWritable(Integer.MAX_VALUE));
//	}

}
