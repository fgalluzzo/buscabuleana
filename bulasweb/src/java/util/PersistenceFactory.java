package util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

public class PersistenceFactory {

	private static EntityManagerFactory emf = null;
	private static EntityManager em = null;
	
	
	public static EntityManagerFactory getEntityManagerFactory() {
		if (emf == null)
			emf = Persistence.createEntityManagerFactory("bulas_base");
		return emf;
	}
	
	public static EntityManager getEntityManager() {
		if (em != null && em.isOpen()) return em;
		else {
			em = getEntityManagerFactory().createEntityManager();
			return em;
		}
	}
}
