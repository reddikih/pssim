package sim.datalayout.managed;

import java.util.ArrayList;
import java.util.List;

public class CacheUnit {

	private List<Integer> primaryDiskIdList;
	private List<Integer> backupDiskIdList;

	public CacheUnit() {
		this.primaryDiskIdList = new ArrayList<Integer>();
		this.backupDiskIdList = new ArrayList<Integer>();
	}

	public List<Integer> getPrimaryDiskIdList() {
		return this.primaryDiskIdList;
	}

	public void addToPrimaryDiskIdList(int id) {
		this.primaryDiskIdList.add(id);
	}

	public List<Integer> getBackupDiskIdList() {
		return this.backupDiskIdList;
	}

	public void addToBackupDiskIdList(int id) {
		this.backupDiskIdList.add(id);
	}

}
