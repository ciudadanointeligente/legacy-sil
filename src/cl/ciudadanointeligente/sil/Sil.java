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
	static MergedBillProcessor mergedBillProcessor = new MergedBillProcessor(billProcessor, billParser);
	public static void main(String[] args) throws Throwable {
		Date startDate = null;
		Date endDate = null;
		String bulletinNumber = null;

		if (args.length == 0) {
			startDate = new Date();
			endDate = startDate;
			System.out.println("Procesando: " + df.format(startDate));
		} else if (args.length == 1) {
			try {
				startDate = df.parse(args[0]);
				endDate = startDate;
				System.out.println("Procesando: " + df.format(startDate));
			} catch (ParseException pex) {
				bulletinNumber = args[0];
				System.out.println("Procesando boletín: " + bulletinNumber);
			}
		} else {
			try {
				startDate = df.parse(args[0]);
				endDate = df.parse(args[1]);
				System.out.println("Procesando: " + df.format(startDate) + " - " + df.format(endDate));
			} catch (ParseException pex) {
				System.err.println("Fecha inválida");
			}
		}
		Session session = HibernateUtil.getSession();
		try {
			session.beginTransaction();
			if (bulletinNumber != null) {
				SilBill newBill = billParser.getBill(bulletinNumber);
				if(newBill.getMergedBulletinNumbers() != null) {
					mergedBillProcessor.process(newBill, session);
				}
				else {
					billProcessor.process(newBill,session);
				}
			}
			else {
				for (SilBill newBill : billParser.getBills(startDate, endDate)) {
					if(newBill.getMergedBulletinNumbers() != null) {
						mergedBillProcessor.process(newBill, session);
					}
					else {
						billProcessor.process(newBill,session);
					}
				}
			}
			session.getTransaction().commit();
		}
		catch (Throwable ex) {
			ex.printStackTrace(System.err);
			session.getTransaction().rollback();
			throw ex;
		}
	}

}
