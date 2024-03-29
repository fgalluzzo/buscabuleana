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
			
			if (labs.size() == 0 || farmacos.size()==0) throw new NoResultException();
						
//			Query q = em.createQuery("select m from Medicamento m, Farmaco f "
//					+ "where m.laboratorio.nome in (:labs) and f.nome in (:farmacos)");
			
			Query q = em.createNativeQuery("select distinct m.id from laboratorio l, farmaco f, associacao ass, medicamento m "
				+ "where l.id=m.lab_detentor_id and ass.farmaco_id=f.id and ass.medicamento_id=m.id and f.nome in (:farmacos) and l.nome in (:labs)");
			q.setParameter("labs", labs);
			q.setParameter("farmacos", farmacos);
			List <Integer> mids = q.getResultList();
			if (mids.size()==0) throw new NoResultException();
			
			q = em.createQuery("from Medicamento where id in (:mids)");
			q.setParameter("mids", mids);
			return (List<MedicamentoBean>) q.getResultList();
		}
		catch (NoResultException e) {
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<MedicamentoBean>();
	}
	
}
