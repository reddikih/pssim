package sim;

import sim.util.DataType;
import sim.util.AccessType;

public class WorkloadRecord {

	private long requestId;
	private long dataId;
	private double arrivalTime;
	private int dataSize;
	private AccessType requestType;
	private DataType dataType;

	public WorkloadRecord(long requestId, long dataId, double arrivalTime,
			int dataSize, AccessType requestType, DataType dataType) {

		this.requestId = requestId;
		this.dataId = dataId;
		this.arrivalTime = arrivalTime;
		this.dataSize = dataSize;
		this.requestType = requestType;
		this.dataType = dataType;
	}

	public long getRequestId() {
		return requestId;
	}

	public long getDataId() {
		return dataId;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public int getDataSize() {
		return dataSize;
	}

	public AccessType getRequestType() {
		return requestType;
	}

	public DataType getDataType() {
		return dataType;
	}

}
