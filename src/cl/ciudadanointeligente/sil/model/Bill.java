package cl.ciudadanointeligente.sil.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class Bill implements Serializable {
	private static final long serialVersionUID = -9147118439381409109L;

	private Long id;
	private String bulletinNumber;
	private String title;
	private String sessionTitle;
	private Date entryDate;
	private String initiative;
	private String type;
	private String originChamber;
	private String urgency;
	private String stage;
	private String substage;
	private Long law;
	private String lawUrl;
	private Long decree;
	private String decreeUrl;
	private Date publishDate;
	private Long matterId;
	private Long internalNumber;
	private Long advance;
	private Long processingNumber;
	private String processingActive;
	private String summary;
	private Date createdAt;
	private Date updatedAt;
	private Set<Author> authors;

	public Long getId () {
		return id;
	}

	public void setId (Long id) {
		this.id = id;
	}

	public String getBulletinNumber () {
		return bulletinNumber;
	}

	public void setBulletinNumber (String bulletinNumber) {
		this.bulletinNumber = bulletinNumber;
	}

	public String getTitle () {
		return title;
	}

	public void setTitle (String title) {
		this.title = title;
	}

	public String getSessionTitle () {
		return sessionTitle;
	}

	public void setSessionTitle (String sessionTitle) {
		this.sessionTitle = sessionTitle;
	}

	public Date getEntryDate () {
		return entryDate;
	}

	public void setEntryDate (Date entryDate) {
		this.entryDate = entryDate;
	}

	public String getInitiative () {
		return initiative;
	}

	public void setInitiative (String initiative) {
		this.initiative = initiative;
	}

	public String getType () {
		return type;
	}

	public void setType (String type) {
		this.type = type;
	}

	public String getOriginChamber () {
		return originChamber;
	}

	public void setOriginChamber (String originChamber) {
		this.originChamber = originChamber;
	}

	public String getUrgency () {
		return urgency;
	}

	public void setUrgency (String urgency) {
		this.urgency = urgency;
	}

	public String getStage () {
		return stage;
	}

	public void setStage (String stage) {
		this.stage = stage;
	}

	public String getSubstage () {
		return substage;
	}

	public void setSubstage (String substage) {
		this.substage = substage;
	}

	public Long getLaw () {
		return law;
	}

	public void setLaw (Long law) {
		this.law = law;
	}

	public String getLawUrl () {
		return lawUrl;
	}

	public void setLawUrl (String lawUrl) {
		this.lawUrl = lawUrl;
	}

	public Long getDecree () {
		return decree;
	}

	public void setDecree (Long decree) {
		this.decree = decree;
	}

	public String getDecreeUrl () {
		return decreeUrl;
	}

	public void setDecreeUrl (String decreeUrl) {
		this.decreeUrl = decreeUrl;
	}

	public Date getPublishDate () {
		return publishDate;
	}

	public void setPublishDate (Date publishDate) {
		this.publishDate = publishDate;
	}

	public Long getMatterId () {
		return matterId;
	}

	public void setMatterId (Long matterId) {
		this.matterId = matterId;
	}

	public Long getInternalNumber () {
		return internalNumber;
	}

	public void setInternalNumber (Long internalNumber) {
		this.internalNumber = internalNumber;
	}

	public Long getAdvance () {
		return advance;
	}

	public void setAdvance (Long advance) {
		this.advance = advance;
	}

	public Long getProcessingNumber () {
		return processingNumber;
	}

	public void setProcessingNumber (Long processingNumber) {
		this.processingNumber = processingNumber;
	}

	public String getProcessingActive () {
		return processingActive;
	}

	public void setProcessingActive (String processingActive) {
		this.processingActive = processingActive;
	}

	public String getSummary () {
		return summary;
	}

	public void setSummary (String summary) {
		this.summary = summary;
	}

	public Date getCreatedAt () {
		return createdAt;
	}

	public void setCreatedAt (Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt () {
		return updatedAt;
	}

	public void setUpdatedAt (Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<Author> getAuthors () {
		return authors;
	}

	public void setAuthors (Set<Author> authors) {
		this.authors = authors;
	}
}
