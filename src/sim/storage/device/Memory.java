package sim.storage.device;

import java.util.HashMap;

import sim.storage.device.model.MemoryModel;

public class Memory {

	private MemoryModel model;
	private HashMap<Long, CacheEntry> lines;
	private long usageVolume;

	private int id;

	public Memory(int id, MemoryModel model) {
		this.model = model;
		this.lines = new HashMap<Long, CacheEntry>();
	}

	public int write(long id, int size) {
		int result = -1;

		long tempUsage = this.usageVolume + size;
		if (model.getCapacity() >= tempUsage) {
			CacheEntry entry = new CacheEntry(id, size);
			lines.put(id, entry);
			this.usageVolume = tempUsage;
			result = size;
		}

		return result;
	}

	public int read(long id) {
		int result = -1;

		if (this.lines.containsKey(id)) {
			CacheEntry entry = this.lines.get(id);
			result = entry.getSize();
		}
		return result;
	}

	public void remove(long id) {
		CacheEntry entry = this.lines.remove(id);
		if (entry != null) {
			this.usageVolume -= entry.getSize();
		}
	}

	public boolean isHit(long id) {
		return this.lines.containsKey(id);
	}

	public boolean isWritable(int size) {
		boolean result = false;
		long available = this.model.getCapacity() - this.usageVolume;
		if (available > 0 && available >= size) result = true;
		return result;
	}

	public long getUsageVolume() {
		return this.usageVolume;
	}

	public long getCapacity() {
		return this.model.getCapacity();
	}

	public void clear() {
		this.lines.clear();
		this.usageVolume = 0;
	}

	private class CacheEntry {

		long id;
		int size;

		private CacheEntry(long id, int size) {
			this.id = id;
			this.size = size;
		}

		private long getId() {
			return this.id;
		}

		private int getSize() {
			return this.size;
		}

	}

	public int getId() {
		return id;
	}

	public double getLatency() {
		return this.model.getAccessLatency();
	}
}
