package sim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import sim.WorkloadRecord;

public class SimUtility {

	public static long parseSize(String arg) {
		long result = -1;
		String lowerStr = arg.toLowerCase();

		boolean bool = Pattern.matches("[1-9][0-9]*(k|m|g|t|)?b?", lowerStr);
		if (bool) {
			int i = -1;
			i = lowerStr.indexOf("k");
			if (i != -1) {
				result = Long.parseLong(lowerStr.substring(0,i)) * 1024;
				return result;
			}
			i = lowerStr.indexOf("m");
			if (i != -1) {
				result = Long.parseLong(lowerStr.substring(0, i)) * 1024 * 1024;
				return result;
			}
			i = lowerStr.indexOf("g");
			if (i != -1) {
				result = Long.parseLong(lowerStr.substring(0, i)) * 1024 * 1024 * 1024;
				return result;
			}
			i = lowerStr.indexOf("t");
			if (i != -1) {
				result = Long.parseLong(lowerStr.substring(0, i)) * 1024 * 1024 * 1024 * 1024;
				return result;
			}
			i = lowerStr.indexOf("b");
			if (i != -1) {
				result = Long.parseLong(lowerStr.substring(0, i));
			} else {
				result = Long.parseLong(lowerStr);
			}
		}

		if (result == -1) throw new IllegalArgumentException("サイズの指定方法に誤りがあります．：" + arg);
		return result;
	}

//	public static void main(String[] args) {
//		String input = "1";
//		Long result = SimUtility.parseSize(input);
//	}

	public static List<WorkloadRecord> getWorkloads(String workloadPath) throws Exception {
//	public static List<WorkloadRecord> getWorkloads(URI workloadPath) {
		List<WorkloadRecord> workloads = new ArrayList<WorkloadRecord>();

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		try {
//			fis = new FileInputStream(workloadPath);
			fis = new FileInputStream(new File(workloadPath));
			isr = new InputStreamReader(fis, "ASCII");
//			InputStream is = workloadPath.toURL().openStream();
//			isr = new InputStreamReader(is, "ASCII");
			br = new BufferedReader(isr);

			String line;
			int lineNum = 1;
			while((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] tokens = line.split(",");
					long requestId = Long.parseLong(tokens[0]);
					long dataId = Long.parseLong(tokens[1]);
					double arrivalTime = Double.parseDouble(tokens[2]);
					int dataSize = Integer.parseInt(tokens[3]);

					// add error check strictly
					AccessType requestType;
					if (AccessType.READ.name().equals(tokens[4])) {
						requestType = AccessType.READ;
					} else if (AccessType.WRITE.name().equals(tokens[4])) {
						requestType = AccessType.WRITE;
					} else {
						throw new Exception("Access type of workload record is invalid. line number : [" + lineNum + "]");
					}
//					AccessType requestType =
//						AccessType.READ.name().equals(tokens[4]) ? AccessType.READ : AccessType.WRITE;

					// add error check strictly
					DataType dataType;
					if (DataType.INITIAL.name().equals(tokens[5])) {
						dataType = DataType.INITIAL;
					} else if (DataType.NORMAL.name().equals(tokens[5])) {
						dataType = DataType.NORMAL;
					} else {
						throw new Exception("Data type of workload record is invalid. line number : [" + lineNum + "]");
					}
//					DataType dataType =
//						DataType.I.name().equals(tokens[5]) ? DataType.I : DataType.N;

					WorkloadRecord record = new WorkloadRecord(requestId, dataId, arrivalTime, dataSize, requestType, dataType);
					workloads.add(record);
					lineNum++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return workloads;
	}

}
