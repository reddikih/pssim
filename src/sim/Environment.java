package sim;

import sim.datalayout.LayoutManager;
import sim.request.RequestManager;
import sim.stat.IStatistics;
import sim.storage.StorageManager;
import sim.storage.device.model.DiskModel;
import sim.storage.device.model.MemoryModel;

public class Environment {

	private static LayoutManager layoutManager;
	private static MemoryModel mmodel;
	private static DiskModel dmodel;
	private static DiskModel cdmodel;
	private static Client client;
	private static StorageManager storageManager;
	private static SimTimeManager simTimeManager;
	private static RequestManager requestManager;
	private static IStatistics stats;

	public static MemoryModel getMemoryModel() {
		return mmodel;
	}

	public static void setMemoryModel(MemoryModel model) {
		Environment.mmodel = model;
	}

	public static DiskModel getDiskModel() {
		return dmodel;
	}

	public static void setDiskModel(DiskModel model) {
		Environment.dmodel = model;
	}

	public static DiskModel getCacheDiskModel() {
		return cdmodel;
	}

	public static void setCacheDiskModel(DiskModel model) {
		Environment.cdmodel = model;
	}

	public static LayoutManager getLayoutManager() {
		return layoutManager;
	}

	public static void setLayoutManager(LayoutManager layoutManager) {
		Environment.layoutManager = layoutManager;
	}

	public static Client getClient() {
		return client;
	}

	public static void setClient(Client client) {
		Environment.client = client;
	}

	public static StorageManager getStorageManager() {
		return storageManager;
	}

	public static void setStorageManager(StorageManager storageManager) {
		Environment.storageManager = storageManager;
	}

	public static SimTimeManager getSimTimeManager() {
		return simTimeManager;
	}

	public static void setSimTimeManager(SimTimeManager simTimeManager) {
		Environment.simTimeManager = simTimeManager;
	}

	public static RequestManager getRequestManager() {
		return requestManager;
	}

	public static void setRequestManager(RequestManager requestManager) {
		Environment.requestManager = requestManager;
	}

	public static IStatistics getStats() {
		return stats;
	}

	public static void setStats(IStatistics stats) {
		Environment.stats = stats;
	}

}
