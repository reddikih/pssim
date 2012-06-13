package sim.datalayout.partitioning;

import sim.datalayout.managed.DataEntry;

public class RoundRobinPartitioning implements IPartitioning {

	private int totalNumber;
	private int nextAssigned;

	public RoundRobinPartitioning(int diskNum) {
		this.totalNumber = diskNum;
	}

	public int partition(DataEntry entry) {
		// ラウンドロビンでは引数entryは無視される．
		int result = nextAssigned++ % totalNumber;
		return result;
	}

}
