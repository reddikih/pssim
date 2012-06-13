package sim.storage.device;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import sim.Environment;
import sim.datalayout.managed.DataEntry;
import sim.storage.cache.Cache;
import sim.storage.cache.CacheEntry;
import sim.storage.cache.CacheSource;

//import sim.SimParameter;

public class DiskCache implements Cache {

	private static int maxCacheSize = 32 * 1024 * 1024; // 32MB. It is derived from 32MB divided by one block size(4096).
	private HashMap<Long, CacheEntry> caches;
	private TreeMap<Double, Long> usedKeys;

	private int usedSize = 0;

//	private int blockSize = SimParameter.getBlockSize();
	private double cacheResposneTime = Environment.getMemoryModel().getAccessLatency();

	private CacheSource source;

	public DiskCache(CacheSource source) {
		this(source, maxCacheSize);
	}

	public DiskCache(CacheSource source, int cacheSize) {
		this.caches = new HashMap<Long, CacheEntry>();
		this.usedKeys = new TreeMap<Double, Long>();
		this.source = source;
		this.maxCacheSize = cacheSize;
	}

	public Object read(DataEntry data, double arrivalTime) {
		DataEntry value = null;

		value = (DataEntry)getEntry(data.getId(), arrivalTime);

		if (value == null) {
			value = (DataEntry)source.readFromSource(data, arrivalTime);

			if (usedSize + value.getSize() <= maxCacheSize) {
				addEntry(data, value.getResponseTime() + arrivalTime);
			} else {
				replaceEntry(data, value.getResponseTime() + arrivalTime);
			}
		} else {
			value.setResponseTime(cacheResposneTime);
		}

		return value;
	}

	public Object write(DataEntry entry, double accessTime) {
		Object value = null;

		if ((value = getEntry(entry.getId(), accessTime)) != null) {
			((DataEntry)value).setResponseTime(cacheResposneTime);
		} else {
			value = source.writeToSource(entry, accessTime);
			if (usedSize + entry.getSize() <= maxCacheSize) {
				addEntry(entry, accessTime);
			} else {
				replaceEntry(entry, accessTime);
			}
		}

		return value;
	}

	private void replaceEntry(DataEntry data, double arrivalTime) {
		while (usedSize + data.getSize() > maxCacheSize) {
			Map.Entry<Double, Long> lruEntry = usedKeys.pollFirstEntry();
			CacheEntry tempEntry = caches.remove(lruEntry.getValue());
			usedSize -= tempEntry.getEntry().getSize();
		}
		addEntry(data, arrivalTime);
	}

	private void addEntry(DataEntry data, double arrivalTime) {
		caches.put(data.getId(), new CacheEntry(data, arrivalTime));
		usedKeys.put(arrivalTime, data.getId());
		usedSize += data.getSize();
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

}
