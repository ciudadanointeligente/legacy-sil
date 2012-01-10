package cl.ciudadanointeligente.sil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;

import cl.ciudadanointeligente.sil.model.SilBill;
import cl.ciudadanointeligente.sil.parser.BillParser;
import cl.ciudadanointeligente.sil.processor.BillProcessor;
import cl.ciudadanointeligente.sil.processor.MergedBillProcessor;

public class Sil {
	static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	static BillParser billParser = new BillParser();
	static BillProcessor billProcessor = new BillProcessor(df);
	static MergedBillProcessor mergedBillProcessor = new MergedBillProcessor(
			billProcessor, billParser);
	static Session session = HibernateUtil.getSession();

	public static void main(String[] args) throws Throwable {
		switch(args.length){
		case 0:
			Date today = new Date();
			System.out.println("Procesando: " + df.format(today));
			processTimeSpan(today, today);
			break;
		case 1:
			try {
				Date particularDate = df.parse(args[0]);
				System.out.println("Procesando: " + df.format(particularDate));
				processTimeSpan(particularDate,particularDate);
			} catch (ParseException pex) {
				String bulletinNumber = args[0];
				System.out.println("Procesando boletín: " + bulletinNumber);
				processBulletin(bulletinNumber);
			}
			break;
		default:
			try {
				Date startDate = df.parse(args[0]);
				Date endDate = df.parse(args[1]);
				System.out.println("Procesando: " + df.format(startDate)
						+ " - " + df.format(endDate));
				processTimeSpan(startDate,endDate);
			} catch (ParseException pex) {
				System.err.println("Fecha inválida");
			}
			break;
		}
	}

	public static void processBulletin(String bulletinNumber) throws Throwable {
		try {
			session.beginTransaction();
			SilBill newBill = billParser.getBill(bulletinNumber);
			if (newBill.getMergedBulletinNumbers() != null) {
				mergedBillProcessor.process(newBill, session);
			} else {
				billProcessor.process(newBill, session);
			}
			session.getTransaction().commit();
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
			session.getTransaction().rollback();
			throw ex;
		}
	}

	public static void processTimeSpan(Date startDate, Date endDate)
			throws Throwable {
		try {
			session.beginTransaction();
			for (SilBill newBill : billParser.getBills(startDate, endDate)) {
				if (newBill.getMergedBulletinNumbers() != null) {
					mergedBillProcessor.process(newBill, session);
				} else {
					billProcessor.process(newBill, session);
				}
			}
			session.getTransaction().commit();
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
			session.getTransaction().rollback();
			throw ex;
		}
	}
}
