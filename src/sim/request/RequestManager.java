package sim.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sim.Environment;
import sim.SimParameter;
import sim.WorkloadRecord;
import sim.storage.StorageManager;
import sim.util.AccessType;
import sim.util.DataType;

public class RequestManager {

	/**
	 * it maps between workload id and request id. workload id is given by workload
	 * issued by a client. request id is used in simulation and it identifies the
	 * request whole of storage layers.
	 */
	private Map<Long, long[]> workloadId2requestId;

	private long sequenceNumber;

	private int blockSize = SimParameter.getBlockSize();

	private boolean isBlockAccess;

	// is it need?
	private Random rand = null;


	/**
	 * This is constructor.
	 *
	 * @param blockAccess
	 */
	public RequestManager(boolean blockAccess) {
		this.isBlockAccess = blockAccess;
		this.workloadId2requestId = new HashMap<Long, long[]>();
		this.rand = new Random();
	}

	/**
	 * This method is called Client. And translate workload record id to inner
	 * data id that is used in the storage components.
	 *
	 * @param record
	 * @return
	 */
	public double receiveWorkloadRecord(WorkloadRecord record) {
		double result = -1.0;

		if (isBlockAccess) {
			AccessType aType = record.getRequestType();
			if (aType.equals(AccessType.READ)) {
				result = readRequest(record);
			} else {
				result = writeRequest(record);
			}
		} else {
			StorageManager sm = Environment.getStorageManager();
			result = sm.execute(record);
		}

		return result;
	}

	/**
	 * Proccessing the read request. Actually this method forwarding the
	 * workload record to the issue method.
	 *
	 * @param record
	 * @return
	 */
	double readRequest(WorkloadRecord record) {
		double result = -1.0;

		result = issue(record);

		return result;
	}

	/**
	 * Processing the write request.
	 *
	 * @param record
	 * @return
	 */
	private double writeRequest(WorkloadRecord record) {
		double result = -1.0;

		long workloadId = record.getDataId();

		if (!workloadId2requestId.containsKey(workloadId)) {
			double temp = (record.getDataSize() / (double)blockSize);
			int n = (int)Math.ceil(temp);
//			int n = (int)Math.ceil((double)(record.getDataSize() / blockSize));

			long[] innerIds = new long[n];
			for (int i = 0; i < n; i++) {
//				DataEntry data = new DataEntry(sequenceNumber + i, record.getDataSize(), record.getDataType());
				innerIds[i] = sequenceNumber++;
			}
			workloadId2requestId.put(workloadId, innerIds);
		}

		result = issue(record);

		return result;
	}

	/**
	 *
	 * @param record
	 * @return
	 */
	private double issue(WorkloadRecord record) {
		double result = -1.0;

		long[] innerIds = workloadId2requestId.get(record.getDataId());

		if (record.getDataType().equals(DataType.NORMAL)) {
//			Random rand = new Random();

			int n = (int)Math.ceil((double)(record.getDataSize() / (double)blockSize));
			long[] issueIds = new long[n];

			for (int i=0; i < issueIds.length; i++) {
//				rand.setSeed(System.currentTimeMillis());
				int j = rand.nextInt(innerIds.length);
				issueIds[i] = innerIds[j];
			}
			innerIds = Arrays.copyOf(issueIds, issueIds.length);
		}

		StorageManager sm = Environment.getStorageManager();
		result = sm.execute(innerIds, record);

		return result;
	}

}
