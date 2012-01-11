package cl.ciudadanointeligente.sil.processor;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import cl.ciudadanointeligente.sil.model.SilBill;
import cl.votainteligente.legislativo.model.Bill;
import cl.votainteligente.legislativo.model.Chamber;
import cl.votainteligente.legislativo.model.Person;
import cl.votainteligente.legislativo.model.Stage;
import cl.votainteligente.legislativo.model.StageDescription;
import cl.votainteligente.legislativo.model.Substage;

public class BillProcessor implements Processor<SilBill, Bill> {
	private DateFormat df;
	private boolean test;

	public BillProcessor(DateFormat df, boolean test) {
		this.df = df;
		this.test = test;
	}

	public Bill process(SilBill newSilBill, Session session) throws Throwable {
		Bill newBill = newSilBill.getBill();
		Bill processedBill = null;
		Set<Person> authors = new HashSet<Person>();

		for (Person newAuthor : newBill.getAuthors()) {
			/*
			 * we look for Person records for each author of newBill. if any are found, then the existing record is
			 * added as one of newBill's authors
			 */
			Query personQuery = session
					.createQuery("select p from Person p where first_name like upper(?) and last_name like upper(?) order by id desc limit 1");
			personQuery.setParameter(0, newAuthor.getFirstName().toUpperCase());
			personQuery.setParameter(1, newAuthor.getLastName().toUpperCase());
			Person existingAuthor = (Person) personQuery.uniqueResult();
			if (existingAuthor == null) {
				if (!test)
					session.save(newAuthor);
				authors.add(newAuthor);
			} else {
				if (!test)
					session.save(existingAuthor);
				authors.add(existingAuthor);
			}
		}
		newBill.setAuthors(authors);

		/*
		 * if the stage description obtained from the Sil site is found in the database, the existing record is used as
		 * the description for this bill's stage. if not, a new stage description record is added
		 */
		Stage newBillStage = new Stage();
		Query stageDescriptionQuery = session
				.createQuery("select sd from StageDescription sd where sd.description like upper(?)");
		stageDescriptionQuery.setParameter(0, newSilBill.getStageName().toUpperCase());
		StageDescription stageDescription = (StageDescription) stageDescriptionQuery.uniqueResult();
		if (stageDescription != null) {
			newBillStage.setStageDescription(stageDescription);
		} else {
			StageDescription newStageDescription = new StageDescription();
			if (!test)
				session.save(newStageDescription);
			newStageDescription.setDescription(newSilBill.getStageName());
			newBillStage.setStageDescription(newStageDescription);
		}
		/*
		 * we assume that the new bill entered its current stage and substage today. later, if the new bill is found in
		 * the database, we check its existing stage records to make sure we don't duplicate them (and discard the
		 * information obtained from the Sil site if necessary)
		 */
		newBillStage.setEntryDate(new Date());
		Substage newBillSubstage = new Substage();
		newBillSubstage.setEntryDate(new Date());
		newBillSubstage.setDescription(newSilBill.getSubstageName());
		newBillStage.setSubStages(new HashSet<Substage>());
		newBillStage.getSubStages().add(newBillSubstage);
		newBill.setStages(new HashSet<Stage>());
		newBill.getStages().add(newBillStage);

		/*
		 * we obtain the correct Chamber object from the database and assign it to the new bill as its origin chamber
		 */
		Query chamberQuery = session.createQuery("select c from Chamber c where upper(c.name) like upper(?)");
		chamberQuery.setParameter(0, (newSilBill.getOriginChamberName().equals("Senado")) ? "%SEN%" : "%DIP%");
		Chamber chamber = (Chamber) chamberQuery.uniqueResult();
		newBill.setOriginChamber(chamber);

		Query billQuery = session.createQuery("select b from Bill b where b.bulletinNumber = ?");
		billQuery.setString(0, newBill.getBulletinNumber());
		Bill oldBill = (Bill) billQuery.uniqueResult();

		/*
		 * if a bill with the same bulletin number is already stored in the database, it should be updated with new info
		 */
		if (oldBill != null) {
			/*
			 * only bills that are still active should be updated, so we determine the last stage and update if it's
			 * different from "Archivado", "Retirado" and "Tramitación terminada"
			 */
			Stage lastOldBillStage = (Stage) oldBill.getStages().toArray()[0];
			for (Stage oldBillStage : oldBill.getStages()) {
				if (oldBillStage.getEntryDate().compareTo(lastOldBillStage.getEntryDate()) > 0) {
					lastOldBillStage = oldBillStage;
				}
			}
			if (!lastOldBillStage.getStageDescription().getDescription().equals("Archivado")
					&& !lastOldBillStage.getStageDescription().getDescription().equals("Retirado")
					&& !lastOldBillStage.getStageDescription().getDescription().equals("Tramitación terminada")) {
				oldBill.setTitle(newBill.getTitle());
				oldBill.setEntryDate(newBill.getEntryDate());
				oldBill.setInitiative(newBill.getInitiative());
				oldBill.setType(newBill.getType());
				oldBill.setOriginChamber(newBill.getOriginChamber());
				oldBill.setUrgency(newBill.getUrgency());
				oldBill.setBcnLawId(newBill.getBcnLawId());
				oldBill.setBcnLawURL(newBill.getBcnLawURL());
				oldBill.setDecree(newBill.getDecree());
				oldBill.setDecreeUrl(newBill.getDecreeUrl());
				oldBill.setPublicationDate(newBill.getPublicationDate());
				oldBill.setSilIndicationsId(newBill.getSilIndicationsId());
				oldBill.setSilUrgenciesId(newBill.getSilUrgenciesId());
				oldBill.setSilOficiosId(newBill.getSilOficiosId());
				oldBill.setSilProcessingsId(newBill.getSilProcessingsId());
				oldBill.setUpdatedAt(newBill.getUpdatedAt());
				oldBill.setAuthors(authors);

				/*
				 * to update the stage history of a bill, we check existing records. if there are no records, we
				 * associate a new stage to the existing bill
				 */
				if (oldBill.getStages() == null || oldBill.getStages().isEmpty()) {
					if (oldBill.getStages() == null) {
						oldBill.setStages(new HashSet<Stage>());
					}
					oldBill.getStages().add(newBillStage);
					if (!test)
						session.save(newBillStage);
				} else {
					/*
					 * if there are stage records, there are two possible scenarios: 1. the current stage obtained from
					 * the Sil site (newBillStage) is already recorded. in this case, we look for substage records to
					 * update 2. the current stage obtained from the Sil site is not recorded. in this case we should
					 * set an end date for the latest stage on record, and add the stage and substage information from
					 * the site
					 */
					boolean foundStage = false;
					Stage lastStage = (Stage) oldBill.getStages().toArray()[0];
					for (Stage oldStage : oldBill.getStages()) {
						if (oldStage.getEntryDate().compareTo(lastStage.getEntryDate()) > 0) {
							lastStage = oldStage;
						}
						if (oldStage.getStageDescription().getDescription()
								.equals(newBillStage.getStageDescription().getDescription())) {
							/*
							 * oldBill has a stage on its history that matches the information obtained from the site.
							 * we look for existing substages. if there are none, then we add the substage obtained from
							 * the site to oldBill's records
							 */
							if (oldStage.getSubStages() == null || oldStage.getSubStages().isEmpty()) {
								if (oldStage.getSubStages() == null) {
									oldStage.setSubStages(new HashSet<Substage>());
								}
								oldStage.getSubStages().add(newBillSubstage);
								if (!test) {
									session.save(newBillSubstage);
									session.save(oldStage);
								}
							} else {
								/*
								 * if there are substages on oldBill's records, we check if the current substage
								 * obtained from the site (newBillSubstage) is recorded on oldBill
								 */
								boolean foundSubstage = false;
								Substage lastSubstage = (Substage) oldStage.getSubStages().toArray()[0];
								for (Substage oldSubstage : oldStage.getSubStages()) {
									if (oldSubstage.getEntryDate().compareTo(lastSubstage.getEntryDate()) > 0) {
										lastSubstage = oldSubstage;
									}
									if (oldSubstage.getDescription().equals(newBillSubstage.getDescription())) {
										/*
										 * newBillSubstage was found on oldBill's records, so nothing should be updated
										 */
										foundSubstage = true;
										break;
									}
								}
								if (!foundSubstage) {
									/*
									 * newBillSubstage was not found on oldBill's records. we set today as the end date
									 * for the previous substage, and add newBillSubstage to oldBill as its latest
									 * substage
									 */
									lastSubstage.setEndDate(new Date());
									oldStage.getSubStages().add(newBillSubstage);
									if (!test) {
										session.save(newBillSubstage);
										session.save(oldStage);
									}
								}
							}
							/*
							 * after updating oldBill's substages, nothing else should be done
							 */
							foundStage = true;
							break;
						}
					}
					if (!foundStage) {
						/*
						 * newBillStage was not found on oldBill's records. we set today as the end date for the
						 * previous stage, and add newBillStage to oldBill as its latest stage. newBill stage contains
						 * newBillSubstage as its latest (and only) substage.
						 */
						lastStage.setEndDate(new Date());
						oldBill.getStages().add(newBillStage);
						if (!test)
							session.save(newBillStage);
					}
				}
				session.update(oldBill);
				processedBill = oldBill;
			}
		} else {
			/*
			 * the bulletin number was not found. we save newBill's information
			 */
			if (!test) {
				session.save(newBillSubstage);
				session.save(newBillStage);
				session.save(newBill);
			}
			processedBill = newBill;
		}

		System.out.println();
		System.out.println("N° Boletín: " + newBill.getBulletinNumber());
		System.out.println("Título: " + newBill.getTitle());
		System.out.println("Fecha de Ingreso: " + df.format(newBill.getEntryDate()));
		System.out.println("Iniciativa: " + newBill.getInitiative());
		System.out.println("Tipo de Proyecto: " + newBill.getType());
		System.out.println("Cámara de Origen: " + newBill.getOriginChamber().getName());
		System.out.println("Urgencia actual: " + newBill.getUrgency());
		System.out.println("Etapa: " + newBillStage.getStageDescription().getDescription());
		System.out.println("Subetapa: " + newBillSubstage.getDescription());
		if (newBill.getBcnLawId() != null) {
			System.out.println("Ley: " + newBill.getBcnLawId().toString());

			if (newBill.getPublicationDate() != null) {
				System.out.println("Fecha de publicación: " + df.format(newBill.getPublicationDate()));
			}
		} else if (newBill.getDecree() != null) {
			System.out.println("Decreto: " + newBill.getDecree().toString());

			if (newBill.getPublicationDate() != null) {
				System.out.println("Fecha de Publicación: " + df.format(newBill.getPublicationDate()));
			}
		}

		return processedBill;

	}
}
