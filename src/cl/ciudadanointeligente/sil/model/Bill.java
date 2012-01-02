package cl.ciudadanointeligente.sil.model;

import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "bill")
public class Bill {

	@Id
	@GeneratedValue
	private Long id;
	@ManyToMany
	private Set<Person> authors;
	@Column(name = "bulletin_number")
	private String bulletinNumber;
	@Type(type = "text")
	private String title;
	@Temporal(TemporalType.DATE)
	@Column(name = "entry_date", nullable = false)
	private Date entryDate;
	private boolean published;
	@Column(name = "publication_date")
	@Temporal(TemporalType.DATE)
	private Date publicationDate;
	@Column(name = "bcn_law_id")
	private Long bcnLawId;
	@Column(name = "bcn_law_url")
	private String bcnLawUrl;
	private String initiative;
	@ManyToOne
	@JoinColumn(name = "origin_chamber")
	private Chamber originChamber;
	private String urgency;
	@OneToMany
	private Set<Stage> stages;
	@Column
	private Long decree;
	@Type(type = "text")
	private String summary;
	@Column(name = "sil_processings_id")
	private Long silProcessingsId;
	@Column(name = "sil_oficios_id")
	private Long silOficiosId;
	@Column(name = "sil_indications_id")
	private Long silIndicationsId;
	@Column(name = "sil_urgencies_id")
	private Long silUrgenciesId;
	@Temporal(TemporalType.DATE)
	@Column(name = "created_at")
	private Date createdAt;
	@Temporal(TemporalType.DATE)
	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;
	@Column
	private String type;
	@Column(name = "decree_url")
	private String decreeUrl;
	@Transient
	private String originChamberName;
	@Transient
	private String stageName;
	@Transient
	private String substageName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Person> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Person> authors) {
		this.authors = authors;
	}

	public String getBulletinNumber() {
		return bulletinNumber;
	}

	public void setBulletinNumber(String bulletinNumber) {
		this.bulletinNumber = bulletinNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public Long getBcnLawId() {
		return bcnLawId;
	}

	public void setBcnLawId(Long bcnLawId) {
		this.bcnLawId = bcnLawId;
	}

	public String getBcnLawUrl() {
		return bcnLawUrl;
	}

	public void setBcnLawUrl(String bcnLawUrl) {
		this.bcnLawUrl = bcnLawUrl;
	}

	public String getInitiative() {
		return initiative;
	}

	public void setInitiative(String initiative) {
		this.initiative = initiative;
	}

	public Chamber getOriginChamber() {
		return originChamber;
	}

	public void setOriginChamber(Chamber originChamber) {
		this.originChamber = originChamber;
	}

	public String getUrgency() {
		return urgency;
	}

	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}

	public Set<Stage> getStages() {
		return stages;
	}

	public void setStages(Set<Stage> stages) {
		this.stages = stages;
	}

	public Long getDecree() {
		return decree;
	}

	public void setDecree(Long decree) {
		this.decree = decree;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Long getSilProcessingsId() {
		return silProcessingsId;
	}

	public void setSilProcessingsId(Long silProcessingsId) {
		this.silProcessingsId = silProcessingsId;
	}

	public Long getSilOficiosId() {
		return silOficiosId;
	}

	public void setSilOficiosId(Long silOficiosId) {
		this.silOficiosId = silOficiosId;
	}

	public Long getSilIndicationsId() {
		return silIndicationsId;
	}

	public void setSilIndicationsId(Long silIndicationsId) {
		this.silIndicationsId = silIndicationsId;
	}

	public Long getSilUrgenciesId() {
		return silUrgenciesId;
	}

	public void setSilUrgenciesId(Long silUrgenciesId) {
		this.silUrgenciesId = silUrgenciesId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDecreeUrl() {
		return decreeUrl;
	}

	public void setDecreeUrl(String decreeUrl) {
		this.decreeUrl = decreeUrl;
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
}