package sim.storage.device.model;

import org.w3c.dom.Document;

import sim.util.SimUtility;
import sim.util.XMLUtility;

public class MemoryModel {

	private String modelName;
	private long capacity;			// 容量 Byte単位
	private double accessLatency;	// 秒単位
	private double readAreaRatio;	// read用領域に割り当てる割合
	private int numberOfReplica;	// データを複製する数

	public MemoryModel(String configPath) {
//	public MemoryModel(URI configPath) {
		Document document = XMLUtility.createDomDocument(configPath);

		this.modelName = XMLUtility.getTagValueAsString(document, "model_name");

		String capacityString = XMLUtility.getTagValueAsString(document, "capacity");
		this.capacity = SimUtility.parseSize(capacityString);

		this.accessLatency = XMLUtility.getTagValueAsDouble(document, "access_latency");
		this.readAreaRatio = XMLUtility.getTagValueAsDouble(document, "read_area_ratio");
		this.numberOfReplica = XMLUtility.getTagValueAsInt(document, "number_of_replica");
	}

	public String getModelName() {
		return modelName;
	}
	public long getCapacity() {
		return capacity;
	}
	public double getAccessLatency() {
		return accessLatency;
	}

	public double getReadAreaRatio() {
		return readAreaRatio;
	}

	public int getNumberOfReplica() {
		return numberOfReplica;
	}
}
