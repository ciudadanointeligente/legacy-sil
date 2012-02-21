package cl.ciudadanointeligente.sil.model;

import cl.votainteligente.legislativo.model.Session;

import java.util.List;

public class SilSession {
	private Session session;
	private String chamber;
	private String date;
	private String[][] assistants;
	private List<SilVote> votes;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getChamber() {
		return chamber;
	}

	public void setChamber(String chamber) {
		this.chamber = chamber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String[][] getAssistants() {
		return assistants;
	}

	public void setAssistants(String[][] assistants) {
		this.assistants = assistants;
	}

	public List<SilVote> getVotes() {
		return votes;
	}

	public void setVotes(List<SilVote> votes) {
		this.votes = votes;
	}
}
