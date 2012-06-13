package sim.datalayout.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sim.output.LogCollector;

public class MAIDLayoutInfo {

	private HashMap<Long, Integer> diskMap;

	private HashMap<Long, Integer> cacheDiskMap;

	public MAIDLayoutInfo() {
		this.diskMap = new HashMap<Long, Integer>();
		this.cacheDiskMap = new HashMap<Long, Integer>();
	}

	public boolean exist(long id) {
		return diskMap.containsKey(id);
	}

	public boolean existOnCacheDisk(long dataId) {
		return cacheDiskMap.containsKey(dataId);
	}

	public void putDiskMap(long dataId, int dataDiskId) {
		diskMap.put(dataId, dataDiskId);
	}

	public void putCacheDiskMap(long dataId, int cacheDiskId) {
		cacheDiskMap.put(dataId, cacheDiskId);
	}

	public void removeCacheDiskMap(long dataId) {
		cacheDiskMap.remove(dataId);
	}

	public int getDataDiskId(long dataId) {
		return diskMap.get(dataId);
	}

	public int getCacheDiskId(long dataId) {
		int result = -1;
		if (cacheDiskMap.containsKey(dataId)) {
			result = cacheDiskMap.get(dataId);
		}
		return result;
	}

	public void showDataAndDiskMappingInfo() {
		String logStr = null;

		Set<Map.Entry<Long, Integer>> diskMapSet = this.diskMap.entrySet();
		for (Map.Entry<Long, Integer> entry : diskMapSet) {
			logStr = LogCollector.createDataDiskMapRecord(entry.getKey(), entry.getValue());
			LogCollector.outputRecord(logStr, LogCollector.OutputType.DATA_DISK_MAP);
		}
	}

}
