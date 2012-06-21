package sim.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import sim.util.DiskState;
import sim.util.AccessType;
import sim.util.ReplicaType;

public class LogCollector {
	// TODO: 出力先ディレクトリを設定ファイルなどで動的に決定する
	private static String OUTPUT_DIR = System.getProperty("user.dir") + File.separator + "out";

	private static final int BUFFER_SIZE = 50 * 10000;

	/**
	 * ログファイルに書き込むためのライタ
	 */
	private static Hashtable<OutputType, PrintWriter> writers = new Hashtable<OutputType, PrintWriter>();

	public static enum OutputType {
		DATA_DISK,
		CACHE_DISK,
		CLIENT_REQUEST,
		CACHE_MEMORY_HIT_RATIO,
		CACHE_DISK_HIT_RATIO,
		BUFFER_WRITABLE_RATIO,
		DISK_ROTATION_RATIO,
		DATA_DISK_MAP,
		STATS,
	};

	/**
	 * ログ出力を初期化する
	 */
	public static void LoggerInit() {

		// outputディレクトリが存在しなければ新規作成
		File outDir = new File(OUTPUT_DIR);
		outDir.mkdirs();
		if (!outDir.exists()) {
			try {
				outDir.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (OutputType type : OutputType.values()) {
			// OutputType の名前から出力ファイル名決定 （SNAKE_CASE -> camelCase）
			String fileName = "";
			String[] words = type.name().split("_");
			for (String word : words) {
				fileName += word.substring(0, 1).toUpperCase();
				fileName += word.substring(1).toLowerCase();
			}

			// ライタ生成
			fileName = LogCollector.OUTPUT_DIR + File.separator + fileName + ".out";

			// 既存の出力ファイルは削除する
			File tempExt = new File(fileName);
			if (tempExt.exists()) {
				tempExt.delete();
			}

			try {
//				LogCollector.writers.put(type, new PrintWriter(fileName));
				LogCollector.writers.put(type, new PrintWriter(new BufferedWriter(new FileWriter(fileName), BUFFER_SIZE)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(0);
			}
		}

		// ヘッダ出力
		LogCollector.outputRecord("OUTPUT_TYPE, DISK_ID, DATA_ID, SIZE, STATE, START_TIME, END_TIME, ENERGY, ACCESS_TYPE", LogCollector.OutputType.DATA_DISK);
		LogCollector.outputRecord("OUTPUT_TYPE, DISK_ID, DATA_ID, SIZE, STATE, START_TIME, END_TIME, ENERGY, ACCESS_TYPE", LogCollector.OutputType.CACHE_DISK);
		LogCollector.outputRecord("OUTPUT_TYPE, REQUEST_ID, DATA_ID, DATA_SIZE, ARRIVAL_TIME, RESPONSE_TIME, ACCESS_TYPE", LogCollector.OutputType.CLIENT_REQUEST);
		LogCollector.outputRecord("OUTPUT_TYPE, DATA_ID, MEMORY_ID, ACCESS_TIME, REPLICA_TYPE, RESULT", LogCollector.OutputType.CACHE_MEMORY_HIT_RATIO);
		LogCollector.outputRecord("OUTPUT_TYPE, DATA_ID, DISK_ID, ACCESS_TIME, RESULT", OutputType.CACHE_DISK_HIT_RATIO);
		LogCollector.outputRecord("OUTPUT_TYPE, DATA_ID, MEMORY_ID, ACCESS_TIME, REPLICA_TYPE, RESULT", OutputType.BUFFER_WRITABLE_RATIO);
		LogCollector.outputRecord("OUTPUT_TYPE, DATA_ID, DISK_ID, ACCESS_TIME, REPLICA_TYPE, RESULT", OutputType.DISK_ROTATION_RATIO);
		LogCollector.outputRecord("DATA_ID,DISK_ID", OutputType.DATA_DISK_MAP);

	}

	public static void outputRecord(String record, OutputType type) {
		PrintWriter writer = LogCollector.writers.get(type);
		writer.println(record);
	}

	public static void flush() {
		for (PrintWriter writer : LogCollector.writers.values()) {
			writer.flush();
		}
	}

	public static String createDataDiskStateRecord(int id, long dataId, int size, DiskState state, double start, double end, double energy, AccessType atype) {
		StringBuilder builder = new StringBuilder();

		builder.append("DD,");
		builder.append(id);builder.append(",");
		builder.append(dataId);builder.append(",");
		builder.append(size);builder.append(",");
		builder.append(state.toString());builder.append(",");
		builder.append(String.format("%.5f", start));builder.append(",");
		builder.append(String.format("%.5f", end));builder.append(",");
		builder.append(String.format("%.5f", energy));builder.append(",");
		builder.append(atype.toString());

		return builder.toString();
	}

	public static String createCacheDiskStateRecord(int id, long dataId, int size, DiskState state, double start, double end, double energy, AccessType atype) {
		StringBuilder builder = new StringBuilder();

		builder.append("CD,");
		builder.append(id);builder.append(",");
		builder.append(dataId);builder.append(",");
		builder.append(size);builder.append(",");
		builder.append(state.toString());builder.append(",");
		builder.append(String.format("%.5f", start));builder.append(",");
		builder.append(String.format("%.5f", end));builder.append(",");
		builder.append(String.format("%.5f", energy));builder.append(",");
		builder.append(atype.toString());

		return builder.toString();
	}

	public static String createClientRequestRecord(long requestId, long dataId, int dataSize, double arrivalTime, double responseTime, AccessType atype) {
		StringBuilder builder = new StringBuilder();

		builder.append("CR,");
		builder.append(requestId);builder.append(",");
		builder.append(dataId);builder.append(",");
		builder.append(dataSize);builder.append(",");
		builder.append(String.format("%.5f", arrivalTime));builder.append(",");
		builder.append(String.format("%.5f", responseTime));builder.append(",");
		builder.append(atype.toString());

		return builder.toString();
	}

	public static String createCacheMemoryHitRatioRecord(long dataId, int memoryId, double accessTime, ReplicaType type, boolean result) {
		StringBuilder builder = new StringBuilder();

		builder.append("HCM,");
		builder.append(dataId);builder.append(",");
		builder.append(memoryId);builder.append(",");
		builder.append(String.format("%.5f", accessTime));builder.append(",");
		if (type != null) builder.append(type.toString());
		builder.append(",");
		builder.append(result);

		return builder.toString();
	}

	public static String createCacheDiskHitRatioRecord(long dataId, int diskId, double accessTime, boolean result) {
		StringBuilder builder = new StringBuilder();

		builder.append("HCD,");
		builder.append(dataId);builder.append(",");
		builder.append(diskId);builder.append(",");
		builder.append(String.format("%.5f", accessTime));builder.append(",");
		builder.append(result);

		return builder.toString();
	}

	public static String createBufferWritableRatioRecord(long dataId, int memoryId, double accessTime, ReplicaType type, boolean result) {
		StringBuilder builder = new StringBuilder();

		builder.append("BW,");
		builder.append(dataId);builder.append(",");
		builder.append(memoryId);builder.append(",");
		builder.append(String.format("%.5f", accessTime));builder.append(",");
		builder.append(type.toString());builder.append(",");
		builder.append(result);

		return builder.toString();
	}

	public static String createDiskRotationRatioRecord(long dataId, int diskId, double accessTime, ReplicaType type, boolean result) {
		StringBuilder builder = new StringBuilder();

		builder.append("DRR,");
		builder.append(dataId);builder.append(",");
		builder.append(diskId);builder.append(",");
		builder.append(String.format("%.5f", accessTime));builder.append(",");
		if (type != null) builder.append(type.toString());
		builder.append(",");
		builder.append(result);

		return builder.toString();
	}

	public static String createDataDiskMapRecord(long dataId, int diskId) {
		StringBuilder builder = new StringBuilder();

		builder.append(dataId);builder.append(",");
		builder.append(diskId);

		return builder.toString();
	}

	public static void setOutputDir(String dir) {
		if (dir != null)
			OUTPUT_DIR  = OUTPUT_DIR + File.separator + dir;
	}
}
