package cl.ciudadanointeligente.sil.model;

import cl.votainteligente.legislativo.model.Bill;

public class SilBill {
	private Bill bill;
	private String originChamberName;
	private String stageName;
	private String substageName;
	private String[] mergedBulletinNumbers;

	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public String getOriginChamberName() {
		return originChamberName;
	}

	public void setOriginChamberName(String originChamberName) {
		this.originChamberName = originChamberName;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public String getSubstageName() {
		return substageName;
	}

	public void setSubstageName(String substageName) {
		this.substageName = substageName;
	}

	public String[] getMergedBulletinNumbers() {
		return mergedBulletinNumbers;
	}

	public void setMergedBulletinNumbers(String[] mergedBulletinNumbers) {
		this.mergedBulletinNumbers = mergedBulletinNumbers;
	}
}
