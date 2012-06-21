package sim.datalayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sim.Environment;
import sim.SimParameter;
import sim.datalayout.info.DefaultLayoutInfo;
import sim.datalayout.managed.DataDisk;
import sim.datalayout.managed.DataEntry;
import sim.datalayout.partitioning.RoundRobinPartitioning;
import sim.storage.StorageManager;
import sim.util.AccessType;

public class DefaultLayoutManager extends LayoutManager {

	private int numberOfDataDisk;

	private List<DataDisk> dataDiskList;

	private DefaultLayoutInfo layoutInfo;


	@Override
	public void decideDestinationDisk(DataEntry entry) {
		if (!layoutInfo.exist(entry.getId())) {
			int nextAssignedDiskId = this.partitioning.partition(entry);
			this.layoutInfo.putDiskMap(entry.getId(), nextAssignedDiskId);
		}
	}

	@Override
	public void init() {
		this.numberOfDataDisk = SimParameter.getNumberOfDataDisk();
		this.dataDiskList = new ArrayList<DataDisk>();

		this.partitioning = new RoundRobinPartitioning(numberOfDataDisk);
		this.layoutInfo = new DefaultLayoutInfo();

		createManagedDevices();
	}

	@Override
	public double readProcess(DataEntry entry, double arrivalTime) {
		double result = -1;

// Modified to random select a copy of primary and backup
		int diskId = this.layoutInfo.getDataDiskId(entry.getId());

		Random rand = new Random();
		if(rand.nextInt(2) == 1) {
			diskId = this.dataDiskList.get(diskId).getBackupDestinationId();
		}
// Modified to random select a copy of primary and backup

		StorageManager sm = Environment.getStorageManager();
		result = sm.accessToDataDiskNormal(diskId, entry, arrivalTime, AccessType.READ);

		return result;
	}

	@Override
	public double writeProcess(DataEntry entry, double arrivalTime) {
		double result = -1;

		int diskId = this.layoutInfo.getDataDiskId(entry.getId());
		int bDiskId = this.dataDiskList.get(diskId).getBackupDestinationId();

		StorageManager sm = Environment.getStorageManager();

		double pResult = sm.accessToDataDiskNormal(diskId, entry, arrivalTime, AccessType.WRITE);
		double bResult = sm.accessToDataDiskNormal(bDiskId, entry, arrivalTime, AccessType.WRITE);

		result = pResult >= bResult ? pResult : bResult;

		return result;
	}

	private void createManagedDevices() {
		long diskCapacity = Environment.getDiskModel().getCapacity();
		int divid = 2;

		for (int i = 0; i < this.numberOfDataDisk; i++) {
			DataDisk dDisk = new DataDisk(i, diskCapacity, divid);
			int sourceBackupId = (i + (numberOfDataDisk - 1)) % numberOfDataDisk;
			int destinationBackupId = (i + 1) % numberOfDataDisk;
			dDisk.setBackupSourceId(sourceBackupId);
			dDisk.setBackupDestinationId(destinationBackupId);
			this.dataDiskList.add(dDisk);
		}
	}

	@Override
	public void showConfiguration() {
		System.out.println("<< This is Normal Configuration >>");
		System.out.println("---createManagedDevices-------");
		System.out.println("Number of DataDisk : " + this.numberOfDataDisk);
		System.out.println("------------------------------");
	}

	public void showDataAndDiskMappingInfo() {
		layoutInfo.showDataAndDiskMappingInfo();
	}

	@Override
	public String getStorageType() {
		return "Normal";
	}

}
