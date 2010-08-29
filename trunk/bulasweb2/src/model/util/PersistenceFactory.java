
package model.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class PersistenceFactory {

	private static EntityManagerFactory emf = null;
	private static EntityManager em = null;
	
	
	/**
	 * Singleton para criar factory apenas uma vez
	 * @return
	 */
	public static EntityManagerFactory getEntityManagerFactory() {
		if (emf == null)
			emf = Persistence.createEntityManagerFactory("bulas_base");
		return emf;
	}
	
	/**
	 * Retorna ultimo EM aberto e em execucao ou cria um novo.
	 * @return
	 */
	public static EntityManager getEntityManager() {
		if (em != null && em.isOpen()) return em;
		else {
			em = getEntityManagerFactory().createEntityManager();
			return em;
		}
	}
	
	/**
	 * Retorna uma nova instancia de EntityManager sem substituir o anterior.
	 * @return
	 */
	public static EntityManager createEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
				
		emf.close();
		
		System.out.println("finalize");
	}
}

