package sim.stat;

public interface IStatistics {

	public static enum COUNTER_TYPE {
		WRITE_BUFF,
		READ_AREA,
		CACHE_MEMORY,
		CACHE_DISK,
	};

	public void outputStats();

}
