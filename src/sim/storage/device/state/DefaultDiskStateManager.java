package sim.storage.device.state;

import sim.output.LogCollector;
import sim.output.LogCollector.OutputType;
import sim.storage.device.HardDiskDrive;
import sim.util.DiskState;
import sim.util.AccessType;

public class DefaultDiskStateManager extends DiskStateManager {

	@Override
	public DiskState stateCheck(double timestamp, HardDiskDrive hdd) {
		DiskState result;

		double delta = timestamp - (hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime());

		if (delta < 0) {
			// Active中に再アクセス
			result = DiskState.ACTIVE;
		} else {
			// Idle中に再アクセス
			result = DiskState.IDLE;
		}
		return result;
	}

	@Override
	public double diskStateUpdate(double timestamp, HardDiskDrive hdd, OutputType type, AccessType aType) {
		double delay = 0.0;
		DiskState state = stateCheck(timestamp, hdd);

		String logStr;
		double start, end, energy;

		switch (state) {
		case ACTIVE :
			// 直前のActive状態のログ出力
			start = hdd.getLastArrivalTimestamp();
			end = hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
			energy = calculateEnergyConsumption(hdd, DiskState.ACTIVE, end - start);
			logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.ACTIVE, start, end, energy, aType);
			LogCollector.outputRecord(logStr, type);

			// 状態変化はないが，遅延（delay）は発生する
			delay = (hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime()) - timestamp;
			break;
		default :
			// Active状態時間のログ出力
			start = hdd.getLastArrivalTimestamp();
			end = hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
			energy = calculateEnergyConsumption(hdd, DiskState.ACTIVE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.ACTIVE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Idle状態時間のログ出力
			start = end;
			end = timestamp;
			energy = calculateEnergyConsumption(hdd, DiskState.IDLE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.IDLE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}
			break;
		}

		return delay;
	}

}
