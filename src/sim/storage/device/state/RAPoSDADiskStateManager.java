package sim.storage.device.state;

import sim.output.LogCollector;
import sim.storage.device.HardDiskDrive;
import sim.util.DiskState;
import sim.util.AccessType;

public class RAPoSDADiskStateManager extends DiskStateManager {

	public DiskState stateCheck(double timestamp, HardDiskDrive hdd) {
		DiskState result;

		double delta = timestamp - (hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime());

		if (delta < 0) {
			// Active中に再アクセス
			result = DiskState.ACTIVE;
		} else if (delta <= hdd.getThreshold()) {
			// Idle中に再アクセス
			result = DiskState.IDLE;
		} else if (delta <= (hdd.getThreshold() + hdd.getModel().getSpindownTime())) {
			// Spindown中に再アクセス
			result = DiskState.SPINDOWN;
		} else {
			// Standby中に再アクセス
			result = DiskState.STANDBY;
		}
		return result;
	}

	public double diskStateUpdate(double timestamp, HardDiskDrive hdd, LogCollector.OutputType type, AccessType aType) {
		double delay = 0.0;

		DiskState state = stateCheck(timestamp, hdd);

		String logStr;
		double start, end, energy;

		switch (state) {
		case ACTIVE :
			// Active状態時間のログ出力
			start = hdd.getLastArrivalTimestamp();
			end = hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
			energy = calculateEnergyConsumption(hdd, DiskState.ACTIVE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.ACTIVE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// 状態変化はないが，遅延（delay）は発生する
			delay = (hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime()) - timestamp;
			break;
		case IDLE :
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
		case SPINDOWN :
			// Active状態のログ出力
			start = hdd.getLastArrivalTimestamp();
			end = hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
			energy = calculateEnergyConsumption(hdd, DiskState.ACTIVE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.ACTIVE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Idle状態のログ出力
			start = end;
			end = start + hdd.getThreshold();
			energy = calculateEnergyConsumption(hdd, DiskState.IDLE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.IDLE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Spindown状態のログ出力
			start = end;
			end = start + hdd.getModel().getSpindownTime();
			energy = calculateEnergyConsumption(hdd, DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.SPINDOWN, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			delay = (end + hdd.getModel().getSpinupTime()) - timestamp;
			break;
		case STANDBY :
			// Active状態のログ出力
			start = hdd.getLastArrivalTimestamp();
			end = hdd.getLastArrivalTimestamp() + hdd.getLastResponseTime();
			energy = calculateEnergyConsumption(hdd, DiskState.ACTIVE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.ACTIVE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Idle状態のログ出力
			start = end;
			end = start + hdd.getThreshold();
			energy = calculateEnergyConsumption(hdd, DiskState.IDLE, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.IDLE, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Spindown状態のログ出力
			start = end;
			end = start + hdd.getModel().getSpindownTime();
			energy = calculateEnergyConsumption(hdd, DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.SPINDOWN, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			// Standby状態のログ出力
			start = end;
			end = timestamp;
			energy = calculateEnergyConsumption(hdd, DiskState.STANDBY, end - start);
			if (energy > 0) {
				logStr = LogCollector.createDataDiskStateRecord(hdd.getId(), hdd.getLastAccessDataId(), hdd.getLastAccessDataSize(), DiskState.STANDBY, start, end, energy, aType);
				LogCollector.outputRecord(logStr, type);
			}

			break;
		}
		return delay;
	}

}
