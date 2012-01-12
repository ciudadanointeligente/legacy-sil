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
		boolean test = isArgumentPresent(args, "-test");
		if (test)
			System.out.println("MODO TEST - no se guardará nada en la base de datos");

		session = HibernateUtil.getSession();
		df = new SimpleDateFormat("dd/MM/yyyy");
		billParser = new BillParser();
		billProcessor = new BillProcessor(df, test);
		mergedBillProcessor = new MergedBillProcessor(billProcessor,
				billParser, test);

		// PROCESSING ARGUMENT
		String bulletinNumber = getArgument(args, "-bulletin");

		String startDateString = getArgument(args, "-startDate");
		String endDateString = getArgument(args, "-endDate");

		Date startDate = toDate(startDateString, df);
		Date endDate = toDate(endDateString, df);
		if (startDate == null)
			startDate = new Date();
		if (endDate == null)
			endDate = startDate;

		if (bulletinNumber != null) {
			System.out.println("Procesando boletin: " + bulletinNumber);
			processBulletin(bulletinNumber);
		}
		System.out.println("Procesando: " + df.format(startDate) + " - "
				+ df.format(endDate));
		processTimeSpan(startDate, endDate);

	}

	public static Date toDate(String date, DateFormat df) {
		try {
			return df.parse(date);
		} catch (ParseException pex) {
			System.err.println("Fecha " + date + " invalida.");
			return null;
		} catch (NullPointerException exc) {
			return null;
		}
	}

	public static boolean isArgumentPresent(String args[], String arg) {
		for (String eachArg : args)
			if (eachArg.equals(arg))
				return true;
		return false;
	}

	public static String getArgument(String args[], String arg) {
		try {
			for (int i = 0; i < args.length; i++)
				if (args[i].equals(arg))
					return args[i + 1];
			return null;// not found
		} catch (IndexOutOfBoundsException e) {
			return null;// argument name was present but there was no value
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
