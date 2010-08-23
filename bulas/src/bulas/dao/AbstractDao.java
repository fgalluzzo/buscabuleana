package bulas.dao;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;

public class AbstractDao<T> {

	protected EntityManager em;

	
	public AbstractDao(EntityManager em) {
		super();
		this.em = em;
	}

	@SuppressWarnings("unchecked")
	public Collection<T> findAll(Class<T> clazz) {
		try {
			Query query = em.createQuery("FROM " + clazz.getSimpleName());
			Collection<T> result = (Collection<T>) query.getResultList();
			return result;
		} catch (NoResultException e) {
			return new ArrayList<T>();
		}
	}

	public <K> T findById(Class<T> clazz, K id) {
		return em.find(clazz, id);
	}

	public boolean isLoaded(T entity) {
		PersistenceUnitUtil util = em.getEntityManagerFactory()
				.getPersistenceUnitUtil();
		return util.isLoaded(entity);
	}

	public boolean isLoaded(T entity, String attributeName) {
		PersistenceUnitUtil util = em.getEntityManagerFactory()
				.getPersistenceUnitUtil();
		return util.isLoaded(entity, attributeName);
	}

	public boolean isManaged(T entity) {
		return em.contains(entity);
	}
}
