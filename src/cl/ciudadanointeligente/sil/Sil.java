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
	static DateFormat df;
	static BillParser billParser;
	static BillProcessor billProcessor;
	static MergedBillProcessor mergedBillProcessor;
	static Session session;

	public static void main(String[] args) throws Throwable {
		String[] newArgs;
		boolean test = false;
		if (args.length > 0 && args[args.length - 1].equals("-test")) {
			test = true;
			newArgs = new String[args.length - 1];
			System.out.println("MODO TEST - no se guardará nada en la base de datos");
		} else {
			newArgs = new String[args.length];
		}

		session = HibernateUtil.getSession();
		df = new SimpleDateFormat("dd/MM/yyyy");
		billParser = new BillParser();
		billProcessor = new BillProcessor(df, test);
		mergedBillProcessor = new MergedBillProcessor(billProcessor, billParser, test);
		for (int i = 0; i < newArgs.length; i++)
			newArgs[i] = args[i];

		switch (newArgs.length) {
		case 0:
			Date today = new Date();
			System.out.println("Procesando: " + df.format(today));
			processTimeSpan(today, today);
			break;
		case 1:
			try {
				Date particularDate = df.parse(newArgs[0]);
				System.out.println("Procesando: " + df.format(particularDate));
				processTimeSpan(particularDate, particularDate);
			} catch (ParseException pex) {
				String bulletinNumber = newArgs[0];
				System.out.println("Procesando boletín: " + bulletinNumber);
				processBulletin(bulletinNumber);
			}
			break;
		default:
			try {
				Date startDate = df.parse(newArgs[0]);
				Date endDate = df.parse(newArgs[1]);
				System.out.println("Procesando: " + df.format(startDate) + " - " + df.format(endDate));
				processTimeSpan(startDate, endDate);
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

	public static void processTimeSpan(Date startDate, Date endDate) throws Throwable {
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
