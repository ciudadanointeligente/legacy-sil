package cl.ciudadanointeligente.sil.tests;

import java.text.SimpleDateFormat;
import java.util.Set;

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
import cl.votainteligente.legislativo.model.Bill;
import cl.votainteligente.legislativo.model.Chamber;

public class TestBillParser {

	private static SessionFactory sessionFactory;
	private static String testBulletinNumber = "8142-13";
	private static String testBulletinNumberMerged = "7721-25";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static BillParser billParser = new BillParser();
	private static BillProcessor billProcessor = new BillProcessor(new SimpleDateFormat("dd/MM/yyyy"), false);

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBills() {
	}

	@Test
	public void testGetBillByBulletinNumber() {
		try {
			Session session = sessionFactory.getCurrentSession();
			SilBill silBill = billParser.getBill(testBulletinNumber);
			session.beginTransaction();
			billProcessor.process(silBill, session);
			Query billQuery = session.createQuery("select b from Bill b where b.bulletinNumber=?");
			billQuery.setParameter(0, testBulletinNumber);
			Bill savedBill = (Bill) billQuery.uniqueResult();
			System.out.println(savedBill.getTitle());
			Assert.assertNotNull(savedBill);
			Assert.assertEquals(testBulletinNumber, savedBill.getBulletinNumber());
			Assert.assertEquals("Establece la reajustabilidad de las pensiones del sistema público y privado.", savedBill.getTitle().trim());
			Assert.assertEquals(dateFormat.parse("10-01-2012"), savedBill.getEntryDate());
			Assert.assertEquals(5, savedBill.getAuthors().size());
			Assert.assertEquals("Moción", savedBill.getInitiative().trim());
			Assert.assertEquals("Senado", savedBill.getOriginChamber().getName().trim());
			Assert.assertEquals("Reforma constitucional", savedBill.getType().trim());
			session.getTransaction().commit();
		}
		catch(Throwable ex){
			ex.printStackTrace();
			Assert.fail();
		}
	}
	@Test
	public void testGetMergedBill() {
		try {
			Session session = sessionFactory.getCurrentSession();
			SilBill silBill = billParser.getBill(testBulletinNumberMerged);
			session.beginTransaction();
			billProcessor.process(silBill, session);
			Query billQuery = session.createQuery("select b from Bill b where b.bulletinNumber=?");
			//TODO: assert something!
		}
		catch(Throwable ex){
			ex.printStackTrace();
			Assert.fail();
		}
	}

}
