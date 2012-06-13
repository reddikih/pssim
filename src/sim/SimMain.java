package sim;

import java.util.Date;
import java.util.List;

import sim.datalayout.LayoutManager;
import sim.datalayout.factory.LayoutManagerFactory;
import sim.output.LogCollector;
import sim.request.RequestManager;
import sim.storage.StorageManager;
import sim.storage.device.model.DiskModel;
import sim.storage.device.model.MemoryModel;
import sim.util.SimUtility;

public class SimMain {

	private String simulatorConfig;
	private String memoryModelConfig;
	private String diskModelConfig;
	private String cacheDiskModelConfig;
	private String workloadFile;
	private String blockAccess;
	private String useCache;
	private String outputDes;

	public static void main(String[] args) throws Exception {
		SimMain simulator = new SimMain();

		simulator.readCommandLine(args);

		showStartMessage();

		long start = System.currentTimeMillis();

		simulator.setUp();
		System.out.println("Initializing is finished.");

		simulator.execute();
		System.out.println("Executing is finished.");

		simulator.postProcess();
		System.out.println("Postprocess is finished.");

		long end = System.currentTimeMillis();

		showEndMessage(end-start);
	}

	public void setUp() throws Exception {
		// Creation SimParameter
		String simulatorConfig = this.simulatorConfig;
		SimParameter.init(simulatorConfig);
		SimParameter.setModelOfCacheMemory(this.memoryModelConfig);
		SimParameter.setModelOfCacheDisk(this.cacheDiskModelConfig);
		SimParameter.setModelOfDataDisk(this.diskModelConfig);

		// Creation MemoryModel
		MemoryModel mmodel = new MemoryModel(this.memoryModelConfig);
		Environment.setMemoryModel(mmodel);

		// Creation DataDiskModel
		DiskModel dmodel = new DiskModel(this.diskModelConfig);
		Environment.setDiskModel(dmodel);

		// Creation CacheDiskModel
		DiskModel cdmodel = new DiskModel(this.cacheDiskModelConfig);
		Environment.setCacheDiskModel(cdmodel);

		// Creation Client
		String workloadPath = this.workloadFile;
		List<WorkloadRecord> workloads = SimUtility.getWorkloads(workloadPath);
		Client client = new Client(SimParameter.getNumberOfClients(), workloads);
		Environment.setClient(client);

		// Create the Request Manager
		boolean isBlockAccess = false;
		if (this.blockAccess.equalsIgnoreCase("on")) isBlockAccess = true;
		RequestManager rm = new RequestManager(isBlockAccess);
		Environment.setRequestManager(rm);

		// Creation LayoutManager
		String layoutManagerFactoryName = SimParameter.getLayoutManagerFactory();
		LayoutManagerFactory managerFactory = (LayoutManagerFactory)Class.forName(layoutManagerFactoryName).newInstance();
		LayoutManager layoutManager = managerFactory.create();
		Environment.setLayoutManager(layoutManager);

		// Creation StorageManager
		StorageManager storageManager = new StorageManager();
		Environment.setStorageManager(storageManager);

		// Creation SimTimeManager
		SimTimeManager simTimeManager = new SimTimeManager();
		Environment.setSimTimeManager(simTimeManager);

		// Disk Cache setting
		boolean useCache = false;
		if (this.useCache.equalsIgnoreCase("on")) useCache = true;
		SimParameter.setUseCache(useCache);

		// Initializing for log
		LogCollector.setOutputDir(this.outputDes);
		LogCollector.LoggerInit();

		// Display configuration of Simulation
		layoutManager.showConfiguration();
	}

	public void execute() {
		Client client = Environment.getClient();
		client.issueRequests();
	}

	public void postProcess() {
		StorageManager sm = Environment.getStorageManager();
		double latestAccessTime = Environment.getSimTimeManager().getLatestAccessTime();
		sm.postProcess(latestAccessTime + 1);

		// for debug
		// show the map information between dataid and data disk id
		LayoutManager layoutManager = Environment.getLayoutManager();
		layoutManager.showDataAndDiskMappingInfo();

		LogCollector.flush();
	}

	private static void showStartMessage() {
		System.out.println("Storage Power Consumption Simulator");
		Date nowDate = new Date();
		System.out.println("Simulator is starting..." + nowDate);
	}

	private static void showEndMessage(long elapsedTime) {
		Date nowDate = new Date();
		System.out.println("Simulation elapsed time : " + elapsedTime + "[ms]");
		System.out.println("Simulator is end. thank you. " + nowDate);
	}

	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("    java -jar spsim.jar sim_cfg mmodel_cfg ddmodel_cfg ccmodel_cfg workload_path blcsw casw out\n");
		System.out.println("    sim_cfg - a name of simulator config file");
		System.out.println("    mmodel_cfg - a name of memory model config file");
		System.out.println("    ddmodel_cfg - a name of data disk model config file");
		System.out.println("    cdmodel_cfg - a name of cache disk model config file");
		System.out.println("    workload_path - a name of workload file");
		System.out.println("    blcsw - \"on\" is using block access \"off\" is not using block access");
		System.out.println("    casw - \"on\" is using disk cache \"off\" is not using disk cache");
		System.out.println("    output - a path of output logs");
	}

	private void readCommandLine(String[] args) {
		if (args == null || args.length != 8) {
			showUsage();
			System.exit(0);
		}

		this.simulatorConfig = args[0];
		this.memoryModelConfig = args[1];
		this.diskModelConfig = args[2];
		this.cacheDiskModelConfig = args[3];
		this.workloadFile = args[4];
		this.blockAccess = args[5];
		this.useCache = args[6];
		this.outputDes = args[7];

        showArguments();
	}

    private void showArguments() {
        System.out.println("Arguments :");
        System.out.println("  " + this.simulatorConfig);
        System.out.println("  " + this.memoryModelConfig);
        System.out.println("  " + this.diskModelConfig);
        System.out.println("  " + this.cacheDiskModelConfig);
        System.out.println("  " + this.workloadFile);
        System.out.println("  " + this.blockAccess);
        System.out.println("  " + this.useCache);
        System.out.println("  " + this.outputDes);
    }
}
