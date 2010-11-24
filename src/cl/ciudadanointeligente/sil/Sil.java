package cl.ciudadanointeligente.sil;

import cl.ciudadanointeligente.sil.model.Author;
import cl.ciudadanointeligente.sil.model.Bill;
import cl.ciudadanointeligente.sil.parser.BillParser;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sil {
	@SuppressWarnings ("unchecked")
	public static void main (String[] args) throws Throwable {
		BillParser billParser = new BillParser ();
		DateFormat df = new SimpleDateFormat ("dd/MM/yyyy");
		Date startDate = null;
		Date endDate = null;

		if (args.length == 0) {
			startDate = new Date ();
			endDate = startDate;
			System.out.println ("Procesando: " + df.format (startDate));
		} else if (args.length == 1) {
			try {
				startDate = df.parse (args[0]);
				endDate = startDate;
				System.out.println ("Procesando: " + df.format (startDate));
			} catch (ParseException pex) {
				System.err.println ("Fecha inválida");
			}
		} else {
			try {
				startDate = df.parse (args[0]);
				endDate = df.parse (args[1]);
				System.out.println ("Procesando: " + df.format (startDate) + " - " + df.format (endDate));
			} catch (ParseException pex) {
				System.err.println ("Fecha inválida");
			}
		}

		Session session = null;

		try {
			for (Bill newBill : billParser.getBills (startDate, endDate)) {
				session = HibernateUtil.getSession ();
				session.beginTransaction ();

				Set<Author> authors = new HashSet<Author> ();

				// FIXME: Determine properly the author when there's more than 1 author record in the database
				for (Author newAuthor : newBill.getAuthors ()) {
					Criteria criteria = session.createCriteria (Author.class);
					criteria.add (Restrictions.eq ("firstName", newAuthor.getFirstName ()));
					criteria.add (Restrictions.eq ("lastName", newAuthor.getLastName ()));
					criteria.addOrder (Order.desc ("id"));
					List<Author> oldAuthors = criteria.list ();

					if (oldAuthors.size () == 0) {
						session.save (newAuthor);
						authors.add (newAuthor);
					} else {
						authors.add (oldAuthors.get (0));
					}
				}

				Criteria criteria = session.createCriteria (Bill.class);
				criteria.add (Restrictions.eq ("bulletinNumber", newBill.getBulletinNumber ()));
				List<Bill> bills = criteria.list ();

				if (bills.size () == 1) {
					Bill oldBill = bills.get (0);
					oldBill.setTitle (newBill.getTitle ());
					oldBill.setEntryDate (newBill.getEntryDate ());
					oldBill.setInitiative (newBill.getInitiative ());
					oldBill.setType (newBill.getType ());
					oldBill.setOriginChamber (newBill.getOriginChamber ());
					oldBill.setUrgency (newBill.getUrgency ());
					oldBill.setStage (newBill.getStage ());
					oldBill.setSubstage (newBill.getSubstage ());
					oldBill.setLaw (newBill.getLaw ());
					oldBill.setLawUrl (newBill.getLawUrl ());
					oldBill.setDecree (newBill.getDecree ());
					oldBill.setDecreeUrl (newBill.getDecreeUrl ());
					oldBill.setPublishDate (newBill.getPublishDate ());
					oldBill.setInternalNumber (newBill.getInternalNumber ());
					oldBill.setUpdatedAt (newBill.getUpdatedAt ());
					oldBill.setAuthors (authors);
					session.update (oldBill);
				} else if (bills.size () == 0) {
					newBill.setAuthors (authors);
					session.save (newBill);
				} else {
					throw new Exception ("Existe más de un proyecto de ley con el mismo número de boletín: " + newBill.getBulletinNumber ());
				}

				session.getTransaction ().commit ();

				System.out.println ();
				System.out.println ("N° Boletín: " + newBill.getBulletinNumber ());
				System.out.println ("Título: " + newBill.getTitle ());
				System.out.println ("Fecha de Ingreso: " + df.format (newBill.getEntryDate ()));
				System.out.println ("Iniciativa: " + newBill.getInitiative ());
				System.out.println ("Tipo de Proyecto: " + newBill.getType ());
				System.out.println ("Cámara de Origen: " + newBill.getOriginChamber ());
				System.out.println ("Urgencia actual: " + newBill.getUrgency ());
				System.out.println ("Etapa: " + newBill.getStage ());

				if (newBill.getSubstage () != null) {
					System.out.println ("Subetapa: " + newBill.getSubstage ());
				} else if (newBill.getLaw () != null) {
					System.out.println ("Ley: " + newBill.getLaw ().toString ());

					if (newBill.getPublishDate () != null) {
						System.out.println ("Fecha de publicación: " + df.format (newBill.getPublishDate ()));
					}
				} else if (newBill.getDecree () != null) {
					System.out.println ("Decreto: " + newBill.getDecree ().toString ());

					if (newBill.getPublishDate () != null) {
						System.out.println ("Fecha de Publicación: " + df.format (newBill.getPublishDate ()));
					}
				}
			}
		} catch (Throwable ex) {
			ex.printStackTrace (System.err);
			session.getTransaction ().rollback ();
			throw ex;
		}
	}
}
