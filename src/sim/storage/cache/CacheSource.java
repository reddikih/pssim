package sim.storage.cache;

import sim.datalayout.managed.DataEntry;

public interface CacheSource {
	public Object readFromSource(DataEntry entry, double accessTime);
	public Object writeToSource(DataEntry entry, double arrivalTime);
}
