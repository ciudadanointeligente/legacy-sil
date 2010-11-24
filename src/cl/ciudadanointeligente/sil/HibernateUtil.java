package cl.ciudadanointeligente.sil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static SessionFactory sessionFactory = null;

	public static Session getSession () {
		if (sessionFactory == null) {
			try {
				sessionFactory = new Configuration ().configure ().buildSessionFactory ();
			} catch (Throwable ex) {
				System.err.println ("Couldn't get Hibernate session: " + ex.getMessage ());
				throw new ExceptionInInitializerError (ex);
			}
		}

		return sessionFactory.getCurrentSession ();
	}
}
