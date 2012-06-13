package sim.datalayout.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sim.output.LogCollector;

public class DefaultLayoutInfo {

	/**
	 * DataEntryと（Primary）DataDiskのマッピング情報を保持します．
	 * Key: DataEntryのID，Value: 格納先（primary）DataDiskのID
	 */
	private HashMap<Long, Integer> diskMap;

	public DefaultLayoutInfo() {
		this.diskMap = new HashMap<Long, Integer>();
	}

	public void putDiskMap(long dataId, int dataDiskId) {
		diskMap.put(dataId, dataDiskId);
	}

	public int getDataDiskId(long id) {
		return diskMap.get(id);
	}

	public boolean exist(long id) {
		return diskMap.containsKey(id);
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
