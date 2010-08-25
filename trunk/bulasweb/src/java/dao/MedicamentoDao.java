package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import bean.MedicamentoBean;

public class MedicamentoDao extends AbstractDao<MedicamentoBean> {

	public MedicamentoDao(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}
	
	public List <MedicamentoBean> findByNameAssocLike(String name, String assoc) {
		return null;
	}

	public MedicamentoBean findByNameAssocExactly(String name, String assoc) {
		try {
			Query q = em.createQuery("from Medicamento where nome = :nome and associacao = :assoc");
			q.setParameter("nome", name);
			q.setParameter("assoc", assoc);
			return (MedicamentoBean) q.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<MedicamentoBean> findByName(String name) {
		try {
			Query q = em.createQuery("from Medicamento where nome = :nome");
			q.setParameter("nome", name);
			return (List<MedicamentoBean>) q.getResultList();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
}
