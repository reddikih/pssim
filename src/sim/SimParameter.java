package sim;

import org.w3c.dom.Document;

import sim.util.SimUtility;
import sim.util.XMLUtility;

public class SimParameter {

	private static int numberOfCacheDisks;
	private static int numberOfCacheMemory;
	private static int disksPerCacheMemory;
	private static int numberOfDataDisk;
	private static int numberOfClients;
	private static String layoutManagerFactory;
	private static double thresholdOfToSpindown;
	private static long thresholdOfMemoryBuffer;
	private static String modelOfCacheDisk;
	private static String modelOfDataDisk;
	private static String modelOfCacheMemory;

	// new parameter to apply block access
	private static int blockSize;;

	// new parameter that indicate either use cache or not.
	private static boolean useCache = true;


	public static void init(String configPath) {
//	public static void init(URI configPath) {
		Document document = XMLUtility.createDomDocument(configPath);

		numberOfCacheDisks = XMLUtility.getTagValueAsInt(document, "number_of_cache_disks");
		numberOfCacheMemory = XMLUtility.getTagValueAsInt(document, "number_of_cache_memory");
		disksPerCacheMemory = XMLUtility.getTagValueAsInt(document, "disks_per_cache_memory");

		// Defaultではキャッシュメモリは使用しないが，統一した設定ファイルを使用したいので，
		// 仮想的にキャッシュメモリ数とキャッシュメモリ当たりのディスク数の積でディスク数を表す．
		// 将来的には変更しようと思っている．
		numberOfDataDisk = numberOfCacheMemory * disksPerCacheMemory;

		numberOfClients = XMLUtility.getTagValueAsInt(document, "number_of_clients");
		layoutManagerFactory = XMLUtility.getTagValueAsString(document, "layout_manager_factory");
		thresholdOfToSpindown = XMLUtility.getTagValueAsDouble(document, "threshold_of_to_spindown");

		String memBuffStr = XMLUtility.getTagValueAsString(document, "threshold_of_memory_buffer");
		thresholdOfMemoryBuffer = SimUtility.parseSize(memBuffStr);

//		modelOfCacheDisk = XMLUtility.getTagValueAsString(document, "model_of_cache_disk");
//		modelOfDataDisk = XMLUtility.getTagValueAsString(document, "model_of_data_disk");
//		modelOfCacheMemory = XMLUtility.getTagValueAsString(document, "model_of_cache_memory");

		blockSize = XMLUtility.getTagValueAsInt(document, "block_size");

		useCache = XMLUtility.getTagValueAsBoolean(document, "use_cache");

	}

	public static int getNumberOfCacheDisks() {
		return numberOfCacheDisks;
	}

	public static int getNumberOfCacheMemory() {
		return numberOfCacheMemory;
	}

	public static int getDisksPerCacheMemory() {
		return disksPerCacheMemory;
	}

	public static int getNumberOfClients() {
		return numberOfClients;
	}

	public static String getLayoutManagerFactory() {
		return layoutManagerFactory;
	}

	public static double getThresholdOfToSpindown() {
		return thresholdOfToSpindown;
	}

	public static long getThresholdOfMemoryBuffer() {
		return thresholdOfMemoryBuffer;
	}

	public static String getModelOfCacheDisk() {
		return modelOfCacheDisk;
	}

	public static void setModelOfCacheDisk(String modelOfCacheDisk) {
		SimParameter.modelOfCacheDisk = modelOfCacheDisk;
	}

	public static String getModelOfDataDisk() {
		return modelOfDataDisk;
	}

	public static void setModelOfDataDisk(String modelOfDataDisk) {
		SimParameter.modelOfDataDisk = modelOfDataDisk;
	}

	public static String getModelOfCacheMemory() {
		return modelOfCacheMemory;
	}

	public static void setModelOfCacheMemory(String modelOfCacheMemory) {
		SimParameter.modelOfCacheMemory = modelOfCacheMemory;
	}

	public static int getNumberOfDataDisk() {
		return numberOfDataDisk;
	}

	public static int getBlockSize() {
		return blockSize;
	}

	public static boolean isUseCache() {
		return useCache;
	}

	public static void setUseCache(boolean useCache) {
		SimParameter.useCache = useCache;
	}
}
