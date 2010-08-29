package model.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import model.bean.LaboratorioBean;


public class LaboratorioDao extends AbstractDao<LaboratorioBean> {

	public LaboratorioDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}
	
	public LaboratorioBean findByName(String name) {
		return findByName(name, false);
	}

	public LaboratorioBean findByName(String name, boolean create) {
		try {
			Query q = em.createQuery("from Laboratorio where nome = :nome");
			q.setParameter("nome", name);
			return (LaboratorioBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			if (create) {
				LaboratorioBean lab = new LaboratorioBean();
				lab.setNome(name);
				em.persist(lab);
				return lab;
			}
			else return null;
		}
	}

}
