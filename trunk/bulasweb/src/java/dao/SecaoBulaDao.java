package dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import bean.SecaoBulaBean;

public class SecaoBulaDao extends AbstractDao<SecaoBulaBean> {

	public SecaoBulaDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}
	
	public SecaoBulaBean findByShortName(String shortName) {
		try {
			Query q = em.createQuery("from SecaoBula where nomeCurto = :nome");
			q.setParameter("nome", shortName);
			return (SecaoBulaBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	public SecaoBulaBean findByName(String name) {
		try {
			Query q = em.createQuery("from SecaoBula where nome = :nome");
			q.setParameter("nome", name);
			return (SecaoBulaBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
}
