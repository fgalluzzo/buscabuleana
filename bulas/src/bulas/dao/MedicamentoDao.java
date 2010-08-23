package bulas.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import bulas.bean.LaboratorioBean;
import bulas.bean.MedicamentoBean;

public class MedicamentoDao extends AbstractDao<MedicamentoBean> {

	public MedicamentoDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	
	public MedicamentoBean findByName(String name) {
		return findByName(name, false);
	}

	public MedicamentoBean findByName(String name, boolean create) {
		try {
			Query q = em.createQuery("from Medicamento where nome = :nome");
			q.setParameter("nome", name);
			return (MedicamentoBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			if (create) {
				MedicamentoBean m = new MedicamentoBean();
				m.setNome(name);
				em.persist(m);
				return m;
			}
			else return null;
		}
	}
	
}
