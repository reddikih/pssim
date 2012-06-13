package sim.storage.cache;

import sim.datalayout.managed.DataEntry;

public interface Cache {
	public Object read(DataEntry data, double accessTime);
	public Object write(DataEntry entry, double arrivalTime);
}
