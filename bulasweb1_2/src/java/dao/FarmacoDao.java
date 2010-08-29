package dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import bean.FarmacoBean;

public class FarmacoDao extends AbstractDao<FarmacoBean> {

	public FarmacoDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	public FarmacoBean findByName(String name) {
		return findByName(name, false);
	}

	public FarmacoBean findByName(String name, boolean create) {
		try {
			Query q = em.createQuery("from Farmaco where nome = :nome");
			q.setParameter("nome", name);
			return (FarmacoBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			if (create) {
				FarmacoBean f = new FarmacoBean();
				f.setNome(name);
				em.persist(f);
				return f;
			}
			else return null;
		}
	}
}
