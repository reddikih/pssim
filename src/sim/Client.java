package sim;

import java.util.List;

import sim.output.LogCollector;
import sim.request.RequestManager;
import sim.util.DataType;

/**
 *
 * @author hikida
 *
 */
public class Client {

	private int numberOfClient = 1;
	private List<WorkloadRecord> workloads;

	public Client(int numberOfClient, List<WorkloadRecord> workloads) {
		this.numberOfClient = numberOfClient;
		this.workloads = workloads;
	}

	public void issueRequests() {
//		StorageManager sm = Environment.getStorageManager();
		RequestManager rm = Environment.getRequestManager();

		for (WorkloadRecord record : workloads) {
//			double responseTimestamp = sm.execute(record) + record.getArrivalTime();
			double executeTime = rm.receiveWorkloadRecord(record);
			double responseTimestamp = record.getArrivalTime() + executeTime;
			// 応答時間のログ出力
			// 初期データは通常のログとは異なります．
			if (record.getDataType().equals(DataType.INITIAL)) {
				// デバッグのために初期データはログ出力しない．
				String logStr = LogCollector.createClientRequestRecord(
						record.getRequestId(),
						record.getDataId(),
						record.getDataSize(),
						record.getArrivalTime(),
						record.getArrivalTime(), // 初期データは応答時間はゼロ
						record.getRequestType());
				LogCollector.outputRecord(logStr, LogCollector.OutputType.CLIENT_REQUEST);
			} else {
				String logStr = LogCollector.createClientRequestRecord(
						record.getRequestId(),
						record.getDataId(),
						record.getDataSize(),
						record.getArrivalTime(),
						responseTimestamp,
						record.getRequestType());
				LogCollector.outputRecord(logStr, LogCollector.OutputType.CLIENT_REQUEST);
			}
		}
	}

}
