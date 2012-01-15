package cl.ciudadanointeligente.sil.model;

import cl.votainteligente.legislativo.model.Vote;

public class SilVote {
	private Vote vote;
	private String[] noVotes;
	private String[] yesVotes;
	private String[] abstentionVotes;
	private String[] dispensed;
	private String[] paredVotes;
	private String tittle;
	private String article;
	private String matter;
	private String stage;
	private String result;

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

	public String[] getAbstentionVotes() {
		return abstentionVotes;
	}

	public void setAbstentionVotes(String[] absentVotes) {
		this.abstentionVotes = absentVotes;
	}

	public String[] getDispensed() {
		return dispensed;
	}

	public void setDispensed(String[] dispensed) {
		this.dispensed = dispensed;
	}

	public void setParedVotes(String[] paredVotes) {
		this.paredVotes = paredVotes;
	}

	public String[] getParedVotes() {
		return paredVotes;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setTittle(String tittle) {
		this.tittle = tittle;
	}

	public String getTittle() {
		return tittle;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String getArticle() {
		return article;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getStage() {
		return stage;
	}

	public void setMatter(String matter) {
		this.matter = matter;
	}

	public String getMatter() {
		return matter;
	}

}
