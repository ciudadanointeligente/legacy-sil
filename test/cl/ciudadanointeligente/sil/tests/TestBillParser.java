package cl.ciudadanointeligente.sil.tests;

import java.util.Date;
import java.text.SimpleDateFormat;
import org.junit.Assert;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import cl.ciudadanointeligente.sil.model.SilBill;
import cl.ciudadanointeligente.sil.parser.BillParser;
import cl.ciudadanointeligente.sil.processor.BillProcessor;
import cl.ciudadanointeligente.sil.processor.MergedBillProcessor;
import cl.votainteligente.legislativo.model.Bill;
import cl.votainteligente.legislativo.model.Chamber;
import cl.votainteligente.legislativo.model.Stage;
import java.util.HashSet;
import java.util.List;

public class TestBillParser {

	private static SessionFactory sessionFactory;
	private Session session;
	private String testBulletinNumber;
	private String testBulletinNumberMerged;
	private String testBulletinNumberTest;
	private String testBulletinNumberStageChange;
	private HashSet<String> mergedBulletinNumbers, dateRangeBulletinNumbers;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static BillParser billParser = new BillParser();
	private static BillProcessor billProcessor = new BillProcessor(new SimpleDateFormat("dd/MM/yyyy"), false);
	private static MergedBillProcessor mergedBillProcessor = new MergedBillProcessor(billProcessor, billParser, false);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			sessionFactory = new AnnotationConfiguration().configure("hibernate.test.cfg.xml").buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Couldn't get Hibernate session: " + ex.getMessage());
			throw new ExceptionInInitializerError(ex);
		}
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Chamber senado = new Chamber();
		senado.setName("Senado");
		Chamber diputados = new Chamber();
		diputados.setName("C. de Diputados");
		session.save(senado);
		session.save(diputados);
		session.getTransaction().commit();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		session = sessionFactory.getCurrentSession();

		testBulletinNumber = "7971-24";
		testBulletinNumberMerged = "7721-25";
		testBulletinNumberTest = "8146-15";
		testBulletinNumberStageChange = "8145-15";

		mergedBulletinNumbers = new HashSet<String>();
		mergedBulletinNumbers.add("5877-07");
		mergedBulletinNumbers.add("6205-25");
		mergedBulletinNumbers.add("7251-07");
		mergedBulletinNumbers.add("7509-07");
		mergedBulletinNumbers.add("7718-25");
		mergedBulletinNumbers.add("7600-25");
		mergedBulletinNumbers.add("6055-25");
		mergedBulletinNumbers.add("6175-25");
		mergedBulletinNumbers.add("6210-25");
		mergedBulletinNumbers.add("7229-07");
		mergedBulletinNumbers.add("7603-25");
		mergedBulletinNumbers.add("7741-25");
		mergedBulletinNumbers.add("7721-25");

		dateRangeBulletinNumbers = new HashSet<String>();
		dateRangeBulletinNumbers.add("8130-13");
		dateRangeBulletinNumbers.add("8129-07");
		dateRangeBulletinNumbers.add("8132-26");
		dateRangeBulletinNumbers.add("8131-15");
		dateRangeBulletinNumbers.add("8133-17");
		dateRangeBulletinNumbers.add("8134-07");
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetBillsByDateRange() {
		try {
			Date from = dateFormat.parse("03-01-2012");
			Date to = dateFormat.parse("05-01-2012");
			session.beginTransaction();
			for (SilBill silBill : billParser.getBills(from, to)) {
				if (silBill.getMergedBulletinNumbers() != null) {
					mergedBillProcessor.process(silBill, session);
				} else {
					billProcessor.process(silBill, session);
				}
			}
			Query savedBillsQuery = session
					.createQuery("select b from Bill b where b.entryDate >= ? and b.entryDate <= ?");
			savedBillsQuery.setDate(0, from);
			savedBillsQuery.setDate(1, to);
			List<Bill> savedBills = (List<Bill>) savedBillsQuery.list();
			session.getTransaction().commit();
			for (Bill savedBill : savedBills) {
				Assert.assertTrue(dateRangeBulletinNumbers.contains(savedBill.getBulletinNumber()));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetBillByBulletinNumber() {
		try {
			SilBill silBill = billParser.getBill(testBulletinNumber);
			session.beginTransaction();
			billProcessor.process(silBill, session);
			Query billQuery = session.createQuery("select b from Bill b where b.bulletinNumber=?");
			billQuery.setParameter(0, testBulletinNumber);
			Bill savedBill = (Bill) billQuery.uniqueResult();
			session.getTransaction().commit();
			Assert.assertNotNull(savedBill);
			Assert.assertEquals(testBulletinNumber, savedBill.getBulletinNumber());
			Assert.assertEquals("Declara el 29 de noviembre como día nacional del niño y niña nacidos prematuro.",
					savedBill.getTitle().trim());
			Assert.assertEquals(dateFormat.parse("04-10-2011"), savedBill.getEntryDate());
			Assert.assertEquals(9, savedBill.getAuthors().size());
			Assert.assertEquals("Moción", savedBill.getInitiative().trim());
			Assert.assertEquals("C. de Diputados", savedBill.getOriginChamber().getName().trim());
			Assert.assertEquals("Proyecto de ley", savedBill.getType().trim());
			Stage[] savedBillStages = savedBill.getStages().toArray(new Stage[0]);
			Assert.assertEquals("Tramitación terminada", savedBillStages[0].getStageDescription().getDescription());
			Assert.assertEquals("http://www.leychile.cl/Navegar?idLey=20558&idVersion=2012-01-07",
					savedBill.getBcnLawURL());
			Assert.assertEquals(new Long(20558), savedBill.getBcnLawId());
		} catch (Throwable ex) {
			ex.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetMergedBill() {
		try {
			session.beginTransaction();
			SilBill silBill = billParser.getBill(testBulletinNumberMerged);
			if (silBill.getMergedBulletinNumbers() != null) {
				mergedBillProcessor.process(silBill, session);
			}
			Query billQuery = session.createQuery("select b from Bill b where b.bulletinNumber=?");
			billQuery.setParameter(0, testBulletinNumberMerged);
			Bill savedBill = (Bill) billQuery.uniqueResult();
			session.getTransaction().commit();
			for (Bill mergedBill : savedBill.getMergedBills().getBills()) {
				Assert.assertTrue(mergedBulletinNumbers.contains(mergedBill.getBulletinNumber()));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testTestMode() {
		try {
			session.beginTransaction();
			SilBill silBill = billParser.getBill(testBulletinNumberTest);
			BillProcessor billProcessor = new BillProcessor(new SimpleDateFormat("dd/MM/yyyy"), true);
			billProcessor.process(silBill, session);
			Query savedBillQuery = session.createQuery("select b from Bill b where b.bulletinNumber=?");
			savedBillQuery.setParameter(0, testBulletinNumberMerged);
			Bill savedBill = (Bill) savedBillQuery.uniqueResult();
			session.getTransaction().commit();
			Assert.assertNull(savedBill);
		} catch (Throwable ex) {
			ex.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testStageChange() {
		try {
			session.beginTransaction();
			SilBill silBill = billParser.getBill(testBulletinNumberStageChange);
			Bill firstProcessedBill = billProcessor.process(silBill, session);
			Stage firstStage = (Stage) firstProcessedBill.getStages().toArray()[0];
			silBill = billParser.getBill(testBulletinNumberStageChange);
			silBill.setStageName("Otra etapa");
			Bill secondProcessedBill = billProcessor.process(silBill, session);
			Stage secondStage = (Stage) secondProcessedBill.getStages().toArray()[1];
			session.getTransaction().commit();
			Assert.assertTrue(secondProcessedBill.getStages().contains(firstStage));
			Assert.assertTrue(secondProcessedBill.getStages().contains(secondStage));
		} catch (Throwable ex) {
			ex.printStackTrace();
			Assert.fail();
		}
	}
}