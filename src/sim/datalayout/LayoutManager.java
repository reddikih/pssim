package sim.datalayout;

import sim.datalayout.managed.DataEntry;
import sim.datalayout.partitioning.IPartitioning;

public abstract class LayoutManager {

	protected IPartitioning partitioning;

	public abstract void init();

	/**
	 *
	 */
	public abstract void decideDestinationDisk(DataEntry entry);

	/**
	 * 書き込み処理を実行します．
	 *
	 * @param entry
	 * @return 書き込み処理に掛った時間
	 */
	public abstract double writeProcess(DataEntry entry, double arrivalTime);

	/**
	 * 読み出し処理を実行します．
	 *
	 * @param entry
	 * @return 読み出し処理に掛った時間
	 */
	public abstract double readProcess(DataEntry entry, double arrivalTime);

	/**
	 *
	 */
	public abstract void showConfiguration();

	public abstract void showDataAndDiskMappingInfo();
}
