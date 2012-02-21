package cl.ciudadanointeligente.sil;

import cl.ciudadanointeligente.sil.model.SilBill;
import cl.ciudadanointeligente.sil.model.SilSession;
import cl.ciudadanointeligente.sil.parser.BillParser;
import cl.ciudadanointeligente.sil.parser.DiputeeChamberSessionParser;
import cl.ciudadanointeligente.sil.processor.BillProcessor;
import cl.ciudadanointeligente.sil.processor.MergedBillProcessor;

import org.hibernate.Session;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sil {
	static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

	static DateFormat df;
	static BillParser billParser;
	static BillProcessor billProcessor;
	static MergedBillProcessor mergedBillProcessor;

	static DiputeeChamberSessionParser diputeeChamberSessionParser;
	static Session session;

	public static void main(String[] args) throws Throwable {
		boolean testMode = isArgumentPresent(args, "-test");
		if (testMode) {
			System.out.println("MODO TEST - no se guardará nada en la base de datos");
		}

		session = HibernateUtil.getSession();
		df = new SimpleDateFormat("dd/MM/yyyy");
		billParser = new BillParser();
		diputeeChamberSessionParser = new DiputeeChamberSessionParser();
		billProcessor = new BillProcessor(df, testMode);
		mergedBillProcessor = new MergedBillProcessor(billProcessor, billParser, testMode);

		// PROCESSING ARGUMENT
		String bulletinNumber = getArgument(args, "-bulletin");
		String startDateString = getArgument(args, "-startDate");
		String endDateString = getArgument(args, "-endDate");

		Date startDate = toDate(startDateString, df);
		Date endDate = toDate(endDateString, df);

		if (startDate == null) {
			startDate = df.parse(df.format(new Date()));
		}
		if (endDate == null) {
			endDate = getNextDay(startDate);
		}
		if (bulletinNumber != null) {
			System.out.println("Procesando boletin: " + bulletinNumber);
			processBulletin(bulletinNumber);
		}
		System.out.println("Procesando: " + df.format(startDate) + " - " + df.format(endDate));
		processTimeSpan(startDate, endDate);
	}

	private static Date getNextDay(Date date) {
		long time = date.getTime() + MILLIS_IN_DAY;
		return new Date(time);
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
		for (String eachArg : args) {
			if (eachArg.equals(arg)) {
				return true;
			}
		}
		return false;
	}

	public static String getArgument(String args[], String arg) {
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals(arg)) {
					return args[i + 1];
				}
			}
			return null; // not found
		} catch (IndexOutOfBoundsException e) {
			return null; // argument name was present but there was no value
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
		processTimeSpanBill(startDate, endDate);
		processTimeSpanSession(startDate, endDate);
	}

	public static void processTimeSpanSession(Date startDate, Date endDate) throws Throwable {
		try {
			// session.beginTransaction();
			int[][] legislatures = diputeeChamberSessionParser.findLegislatures();
			List<SilSession> sessions = new ArrayList<SilSession>();
			for (int[] legislature : legislatures) {
				List<SilSession> sessionsTmp = diputeeChamberSessionParser.getSessionSummaryFromLegislature(legislature[0], startDate, endDate);
				sessions.addAll(sessionsTmp);
			}
			// session.getTransaction().commit();
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
			// session.getTransaction().rollback();
			throw ex;
		}
	}

	public static void processTimeSpanBill(Date startDate, Date endDate) throws Throwable {
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
