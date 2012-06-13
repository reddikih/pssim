package sim.storage.device.state;

import sim.output.LogCollector;
import sim.storage.device.HardDiskDrive;
import sim.util.DiskState;
import sim.util.AccessType;

public abstract class DiskStateManager {

	protected AccessType previousAccessType;


	/**
	 * 現在の状態における消費エネルギーを算出します．このメソッドは主にログ出力時に
	 * 呼び出されることを想定しています．
	 *
	 * @param hdd HardDiskDrive
	 * @param state 現在の状態
	 * @param term 状態の期間
	 * @return 現在の状態の期間における消費エネルギー（単位：Jule）
	 */
	public double calculateEnergyConsumption(HardDiskDrive hdd, DiskState state, double term) {
		double result = -0.0;

		switch (state) {
		case ACTIVE :
			result = hdd.getModel().getActivePower() * term;
			break;
		case IDLE :
			result = hdd.getModel().getIdlePower() * term;
			break;
		case SPINDOWN :
			result = hdd.getModel().getSpindownEnergy();
			break;
		case STANDBY :
			result = hdd.getModel().getStandbyPower() * term;
			break;
		case SPINUP :
			result = hdd.getModel().getSpinupEnergy();
			break;
		}
		return result;
	}

	/**
	 *
	 * @param timestamp
	 * @param hdd
	 * @return
	 */
	public abstract DiskState stateCheck(double timestamp, HardDiskDrive hdd);

	/**
	 * 引数で指定した時刻にアクセスがあったとして，ディスクの状態を更新致します．
	 * 状態が変更される場合は，各状態期間のログがログファイルに出力されます．
	 *
	 * @param timestamp 状態更新対象時刻
	 * @param hdd HardDiskDrive
	 * @param type ログファイルの種類
	 * @param リクエストの種別
	 * @return wait時間．timeにアクセスがあったリクエストは，time + wait 時間後がarrival timeとなります．
	 */
//	public abstract double stateUpdate(double timestamp, HardDiskDrive hdd, LogCollector.OutputType type, AccessType rType);
	public double stateUpdate(double timestamp, HardDiskDrive hdd, LogCollector.OutputType type, AccessType aType) {
		double result = 0.0;

		// 直前のアクセス時の種別でログを出力する
		if (previousAccessType == null) previousAccessType = aType;
		result = diskStateUpdate(timestamp, hdd, type, previousAccessType);

		// 今回のアクセス時の種別を保持しておく
		previousAccessType = aType;

		return result;
	}

	protected abstract double diskStateUpdate(double timestamp, HardDiskDrive hdd, LogCollector.OutputType type, AccessType aType);

}
