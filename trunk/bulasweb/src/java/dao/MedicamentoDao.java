package dao;

import java.util.ArrayList;
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
	
	
	@SuppressWarnings("unchecked")
	public List<MedicamentoBean> findByFarmacoAndLab(List<String> labs, List<String> farmacos) {
		try {//f in m.farmacos and 
			
			if (labs.size() == 0 || farmacos.size()==0) throw new Exception();//Remover
			
			Query q = em.createQuery("select m from Medicamento m, Farmaco f "
					+ "where m.laboratorio.nome in (:labs) and f.nome in (:farmacos)");
			q.setParameter("labs", labs);
			q.setParameter("farmacos", farmacos);
			return (List<MedicamentoBean>) q.getResultList();
		}
		catch (Exception e) {
			//e.printStackTrace();
			return new ArrayList<MedicamentoBean>();
		}
	}
	
}
