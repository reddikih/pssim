package sim.storage.device;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.Environment;
import sim.SimParameter;
import sim.datalayout.MAIDLayoutManager;
import sim.datalayout.managed.DataEntry;
import sim.output.LogCollector;
import sim.output.LogCollector.OutputType;
import sim.stat.MAIDStats;
import sim.stat.Statistics;
import sim.storage.cache.Cache;
import sim.storage.cache.CacheEntry;
import sim.util.ReplicaType;

public class MAIDCacheMemory implements Cache {

//	private final static long DEFAULT_MAX_SIZE = 16L * 8 * 1024 * 1024 * 1024;
	private final static int DEFAULT_DIVID = 4;
	private long maxCacheSize = 0; // default 16GB
	private long usingSize = 0;

	private HashMap<Long, CacheEntry> caches;
	private TreeMap<Double, Long> usedKeys;

//	private int blockSize = SimParameter.getBlockSize();
	private static final double cacheResposneTime = Environment.getMemoryModel().getAccessLatency();

	private MAIDLayoutManager source;

	public MAIDCacheMemory(MAIDLayoutManager source) {
//		this(source, DEFAULT_MAX_SIZE);
		this(source, calcMaxCacheSize(DEFAULT_DIVID));
	}

	public MAIDCacheMemory(MAIDLayoutManager source, long cacheSize) {
		this.caches = new HashMap<Long, CacheEntry>();
		this.usedKeys = new TreeMap<Double, Long>();
		this.source = source;
		this.maxCacheSize = cacheSize;
	}

	public MAIDCacheMemory(MAIDLayoutManager source, long cacheSize, int divid) {
		this.caches = new HashMap<Long, CacheEntry>();
		this.usedKeys = new TreeMap<Double, Long>();
		this.source = source;
		if (divid != 0)
			this.maxCacheSize = calcMaxCacheSize(divid);
		else
			this.maxCacheSize = cacheSize;	// this process for debug easily
	}

	private static long calcMaxCacheSize(int divid) {
		int numDataDisk = SimParameter.getNumberOfDataDisk();
		long percent = divid != 0 ? numDataDisk / divid : 0;
		return (long)percent * 1024 * 1024 * 1024;
	}

	public Object read(DataEntry data, double arrivalTime) {
		Object value = null;

		value = getEntry(data.getId(), arrivalTime);

		if (value == null) {
			value = source.readFromSource(data, arrivalTime);

			if ((usingSize + data.getSize()) <= maxCacheSize) {
				addEntry(data, ((DataEntry)value).getResponseTime() + arrivalTime);
			} else {
				replaceEntry(data, ((DataEntry)value).getResponseTime() + arrivalTime);
			}
			// Cache memory hit miss log.
			String logStr = LogCollector.createCacheMemoryHitRatioRecord(data.getId(), 0, arrivalTime, ReplicaType.PRIMARY, false);
			LogCollector.outputRecord(logStr, OutputType.CACHE_MEMORY_HIT_RATIO);
		} else {
			((DataEntry)value).setResponseTime(cacheResposneTime);

			MAIDStats stats = (MAIDStats)Environment.getStats();
			stats.incrementReadCounter(MAIDStats.READ_COUNTER_TYPE.CACHE_MEMORY);
			stats.addingResponseTime(Statistics.RESPONSE_TYPE.MEMORY, ((DataEntry)value).getResponseTime());

			// Cache memory hit log.
			String logStr = LogCollector.createCacheMemoryHitRatioRecord(data.getId(), 0, arrivalTime, ReplicaType.PRIMARY, true);
			LogCollector.outputRecord(logStr, OutputType.CACHE_MEMORY_HIT_RATIO);
		}

		return value;
	}


	public Object write(DataEntry entry, double arrivalTime) {
		Object value = null;

		// MAID のキャッシュポリシーは Write through だからメモリの速度で応答することは無いし
		// 常にディスクまで書き込むようにする．
		value = source.writeToSource(entry, arrivalTime);
		if ((usingSize + entry.getSize()) <= maxCacheSize) {
			addEntry(entry, arrivalTime);
		} else {
			// Write through なので追い出されたデータは無視しても問題無い
			replaceEntry(entry, arrivalTime);
		}
//		if ((value = getEntry(entry.getId(), arrivalTime)) != null) {
//			((DataEntry)value).setResponseTime(cacheResposneTime);
//		} else {
//			value = source.writeToSource(entry, arrivalTime);
//			if ((usingSize + entry.getSize()) <= maxCacheSize) {
//				addEntry(entry, arrivalTime);
//			} else {
//				replaceEntry(entry, arrivalTime);
//			}
//		}

		return value;
	}

	private void replaceEntry(DataEntry entry, double arrivalTime) {
		while (maxCacheSize < (usingSize + entry.getSize())) {
			Map.Entry<Double, Long> lruEntry = usedKeys.pollFirstEntry();
			if (lruEntry == null) return;
			CacheEntry tempEntry = caches.remove(lruEntry.getValue());
			usingSize -= tempEntry.getEntry().getSize();
		}
		addEntry(entry, arrivalTime);
	}

	private void addEntry(DataEntry entry, double arrivalTime) {
		caches.put(entry.getId(), new CacheEntry(entry, arrivalTime));
		usedKeys.put(arrivalTime, entry.getId());
		usingSize += entry.getSize();
	}

	private Object getEntry(Long dataId, double accessTime) {
		Object value = null;
		if (caches.containsKey(dataId)) {
			CacheEntry cEntry = caches.get(dataId);
			usedKeys.remove(cEntry.getAccessedTime());
			usedKeys.put(accessTime, cEntry.getEntry().getId());
			cEntry.setAccessedTime(accessTime);
			value = cEntry.getEntry();
		}
		return value;
	}

	public long getMaxCacheSize() {
		return maxCacheSize;
	}

	public long getUsingSize() {
		return usingSize;
	}

}
