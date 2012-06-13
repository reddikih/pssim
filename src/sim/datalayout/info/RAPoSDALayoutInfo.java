package sim.datalayout.info;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sim.datalayout.managed.CacheUnit;
import sim.output.LogCollector;
import sim.util.ReplicaType;

public class RAPoSDALayoutInfo {

	/**
	 * DataEntryと（Primary）DataDiskのマッピング情報を保持します．
	 * Key: DataEntryのID，Value: 格納先（primary）DataDiskのID
	 */
	private HashMap<Long, Integer> pDiskMap;

	/**
	 * CacheMemoryとDataDiskのマッピング情報を管理しているCacheUnitを保持します．
	 * Key: CacheMemoryのID，Value: CacheUnitオブジェクト．
	 */
	private HashMap<Integer, CacheUnit> cacheUnitMap;

	/**
	 * DataEntryとCacheDiskIdのマッピング情報を保持します．
	 * Key: DataEntryのID，Value: 格納先CacheDiskのID
	 */
	private HashMap<Long, Integer> cacheDiskMap;


	public RAPoSDALayoutInfo() {
		this.pDiskMap = new HashMap<Long, Integer>();
		this.cacheDiskMap = new HashMap<Long, Integer>();
		this.cacheUnitMap = new HashMap<Integer, CacheUnit>();
	}

	public void putDiskMap(long dataId, int dataDiskId) {
		pDiskMap.put(dataId, dataDiskId);
	}

	public void putCacheDiskMap(long dataId, int cacheDiskId) {
		cacheDiskMap.put(dataId, cacheDiskId);
	}

	public void removeCacheDiskMap(long dataId) {
		cacheDiskMap.remove(dataId);
	}

	public int getDataDiskId(long dataId) {
		return pDiskMap.get(dataId);
	}

	public int getCacheDiskId(long dataId) {
		int result = -1;
		if (cacheDiskMap.containsKey(dataId)) {
			result = cacheDiskMap.get(dataId);
		}
		return result;
	}

	public void putCacheUnitMap(int cacheMemoryId, CacheUnit cacheUnit) {
		cacheUnitMap.put(cacheMemoryId, cacheUnit);
	}

	public CacheUnit getCacheUnit(int cacheMemoryId) {
		return cacheUnitMap.get(cacheMemoryId);
	}

	public List<Integer> getDiskIds(int cacheMemoryId, ReplicaType type) {
		List<Integer> result = null;
		if (type.equals(ReplicaType.PRIMARY)) {
			result = this.cacheUnitMap.get(cacheMemoryId).getPrimaryDiskIdList();
		} else if (type.equals(ReplicaType.BACKUP)) {
			result = this.cacheUnitMap.get(cacheMemoryId).getBackupDiskIdList();
		}
		return result;
	}

	/**
	 * 指定したレプリカ種別の領域に，指定したディスクIDの情報を持っているキャッシュメモリ
	 * を検索します．
	 *
	 * @param diskId 対象ディスクID
	 * @param type レプリカ種別
	 * @return 対応するキャッシュメモリのID．検索に失敗した場合，-1を返します
	 */
	public int searchCacheMemoryIdRelatedToDiskId(int diskId, ReplicaType type) {
		int result = -1;

		Set<Integer> cacheMemoryIds = cacheUnitMap.keySet();
		List<Integer> ids;

		for (int cacheMemoryId : cacheMemoryIds) {
			CacheUnit cacheUnit = cacheUnitMap.get(cacheMemoryId);
			if (type.equals(ReplicaType.PRIMARY)) {
				ids = cacheUnit.getPrimaryDiskIdList();
			} else {
				ids = cacheUnit.getBackupDiskIdList();
			}

			if (ids.contains(diskId)) {
				result = cacheMemoryId;
				break;
			}
		}

		return result;
	}

	public boolean exist(long id) {
		return pDiskMap.containsKey(id);
	}

	public boolean existOnCacheDisk(long dataId) {
		return cacheDiskMap.containsKey(dataId);
	}

	public void showDataAndDiskMappingInfo() {
		String logStr = null;

		Set<Map.Entry<Long, Integer>> diskMapSet = this.pDiskMap.entrySet();
		for (Map.Entry<Long, Integer> entry : diskMapSet) {
			logStr = LogCollector.createDataDiskMapRecord(entry.getKey(), entry.getValue());
			LogCollector.outputRecord(logStr, LogCollector.OutputType.DATA_DISK_MAP);
		}
	}

}
