package cl.ciudadanointeligente.sil.model;

import cl.votainteligente.legislativo.model.Vote;

public class SilVote {
	private Vote vote;
	private String[] noVotes;
	private String[] yesVotes;
	private String[] absentVotes;
	private String[] dispensed;
	private String billId;

	public Vote getVote() {
		return vote;
	}

	public void setVote(Vote vote) {
		this.vote = vote;
	}

	public String[] getNoVotes() {
		return noVotes;
	}

	public void setNoVotes(String[] noVotes) {
		this.noVotes = noVotes;
	}

	public String[] getYesVotes() {
		return yesVotes;
	}

	public void setYesVotes(String[] yesVotes) {
		this.yesVotes = yesVotes;
	}

	public String[] getAbsentVotes() {
		return absentVotes;
	}

	public void setAbsentVotes(String[] absentVotes) {
		this.absentVotes = absentVotes;
	}

	public String[] getDispensed() {
		return dispensed;
	}

	public void setDispensed(String[] dispensed) {
		this.dispensed = dispensed;
	}

	public String getBillId() {
		return billId;
	}

	public void setBillId(String billId) {
		this.billId = billId;
	}

}
