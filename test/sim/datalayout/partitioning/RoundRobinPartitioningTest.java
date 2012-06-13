package sim.datalayout.partitioning;

import sim.datalayout.partitioning.IPartitioning;
import sim.datalayout.partitioning.RoundRobinPartitioning;
import junit.framework.TestCase;

public class RoundRobinPartitioningTest extends TestCase {

	public void testPartition() {
		// ラウンドロビンで次のディスクid が指定できることを確認する
		IPartitioning partitioning = new RoundRobinPartitioning(3);
		int next = -1;

		next = partitioning.partition(null);
		assertEquals(next, 0);
		next = partitioning.partition(null);
		assertEquals(next, 1);
		next = partitioning.partition(null);
		assertEquals(next, 2);
		next = partitioning.partition(null);
		assertEquals(next, 0);
	}

}
