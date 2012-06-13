package sim.datalayout.managed;

import sim.util.AccessType;
import sim.util.DataType;

public class DataEntry {

	private long id;
	private int size;
	private DataType dataType;
	private AccessType accessType; // MAID用に追加

	// add for maid cache
	private double responseTime;

	// add for raposda cache replace policy
	private double accessTime;

	public DataEntry(long id, int size, DataType dataType) {
		this.id = id;
		this.size = size;
		this.dataType = dataType;
	}

	// MAIDを動作させるときには，こちらのコンストラクタを使用する．
	public DataEntry(long id, int size, DataType dataType, AccessType accessType, double accessTime) {
		this.id = id;
		this.size = size;
		this.dataType = dataType;
		this.accessType = accessType;
		this.accessTime = accessTime;
	}

	public long getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	public DataType getDataType() {
		return dataType;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public double getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof DataEntry)) return false;
		DataEntry target = (DataEntry)obj;
		return target.getId() == this.getId();
	}

	@Override
	public int hashCode() {
		// follows Effective Java pp35-39
		int result = 17;
		result = 37 * result + (int)(this.getId() ^ (this.getId() >>> 32));
		return result;
	}

	public double getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(double accessTime) {
		// Cache hit したときはアクセス時間が更新されるため，
		// このプロパティはsetterが必要
		this.accessTime = accessTime;
	}

}
