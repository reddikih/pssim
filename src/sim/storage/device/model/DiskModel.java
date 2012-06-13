package sim.storage.device.model;

import java.net.URI;

import org.w3c.dom.Document;

import sim.util.SimUtility;
import sim.util.XMLUtility;

public class DiskModel {

	private String modelName;			// 型番等
	private long capacity;				// GB単位
	private int platters;				// 円盤枚数
	private int rpm;					// 回転数/分
	private int cacheSize;				// MB単位
	private long transferRate;			// データ転送率 Byte/s単位
	private int sectorsPerTrack;		// トラック当たりのセクター数
	private double fullStrokeSeekTime;	// 秒単位
	private double headSwitchTime;		// ヘッドのスイッチ時間(仕様書から) 秒単位
	private double commandOverhead;		// コマンド発行処理のオーバーヘッド時間(仕様書から) 秒単位
	private double activePower;			// アクティブ時の消費電力(仕様書から) Watt単位
	private double idlePower;			// アイドル時の消費電力(仕様書から) Watt単位
	private double standbyPower;		// スタンバイ時の消費電力(仕様書から) Watt単位
	private double spindownEnergy;		// スピンダウン時の消費エネルギー(仕様書から) Jule単位
	private double spinupEnergy;		// スピンアップ時の消費エネルギー(仕様書から) Jule単位
	private double spindownTime;		// 秒単位
	private double spinupTime;			// 秒単位

	public DiskModel(String configPath) {
//	public DiskModel(URI configPath) {
		Document document = XMLUtility.createDomDocument(configPath);

		this.modelName = XMLUtility.getTagValueAsString(document, "model_name");
		String capacityString = XMLUtility.getTagValueAsString(document, "capacity");
		this.capacity = SimUtility.parseSize(capacityString);
		this.platters = XMLUtility.getTagValueAsInt(document, "platters");
		this.rpm = XMLUtility.getTagValueAsInt(document, "rpm");
		String cacheSizeString = XMLUtility.getTagValueAsString(document, "cache_size");
		this.cacheSize = (int)SimUtility.parseSize(cacheSizeString);
		String rateString = XMLUtility.getTagValueAsString(document, "transfer_rate");
		this.transferRate = SimUtility.parseSize(rateString);
		this.sectorsPerTrack = XMLUtility.getTagValueAsInt(document, "sectors_per_track");
		this.fullStrokeSeekTime = XMLUtility.getTagValueAsDouble(document, "full_stroke_seek_time");
		this.headSwitchTime = XMLUtility.getTagValueAsDouble(document, "head_switch_overhead");
		this.commandOverhead = XMLUtility.getTagValueAsDouble(document, "command_overhead");
		this.activePower = XMLUtility.getTagValueAsDouble(document, "active_power");
		this.idlePower = XMLUtility.getTagValueAsDouble(document, "idle_power");
		this.standbyPower = XMLUtility.getTagValueAsDouble(document, "standby_power");
		this.spindownEnergy = XMLUtility.getTagValueAsDouble(document, "spindown_energy");
		this.spinupEnergy = XMLUtility.getTagValueAsDouble(document, "spinup_energy");
		this.spindownTime = XMLUtility.getTagValueAsDouble(document, "spindown_time");
		this.spinupTime = XMLUtility.getTagValueAsDouble(document, "spinup_time");
	}

	public String getModelName() {
		return modelName;
	}

	public long getCapacity() {
		return capacity;
	}

	public int getPlatters() {
		return platters;
	}

	public int getRpm() {
		return rpm;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public long getTransferRate() {
		return transferRate;
	}

	public int getSectorsPerTrack() {
		return sectorsPerTrack;
	}

	public double getFullStrokeSeekTime() {
		return fullStrokeSeekTime;
	}

	public double getHeadSwitchTime() {
		return headSwitchTime;
	}

	public double getCommandOverhead() {
		return commandOverhead;
	}

	public double getActivePower() {
		return activePower;
	}

	public double getIdlePower() {
		return idlePower;
	}

	public double getStandbyPower() {
		return standbyPower;
	}

	public double getSpindownEnergy() {
		return spindownEnergy;
	}

	public double getSpinupEnergy() {
		return spinupEnergy;
	}

	public double getSpindownTime() {
		return spindownTime;
	}

	public double getSpinupTime() {
		return spinupTime;
	}


}
