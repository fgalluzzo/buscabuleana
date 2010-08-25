package dao;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import bean.BulaBean;

public class BulaDao extends AbstractDao<BulaBean> {

	public BulaDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	public BulaBean getByCodigo(String codigo) {
		try {
			Query q = em.createQuery("from Bula where codigo = :codigo");
			q.setParameter("codigo", codigo);
			return (BulaBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Integer> getAllAsIntList() {
		try {
			Query q = em.createNativeQuery("select id from bula");
			return (Collection<Integer>) q.getResultList();
		}
		catch (NoResultException e) {
			return null;
		}
	}
}
