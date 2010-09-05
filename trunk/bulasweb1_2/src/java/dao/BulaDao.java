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
			Query q = em.createNativeQuery("select id from bula order by id asc");
			return (Collection<Integer>) q.getResultList();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Integer> getByLengthAndSectionsAsIntList(int minLength, int minNumberOfSections) {
		try {
			Query q = em.createNativeQuery(
				"select b.id " +
				"from bula b, medicamento m, conteudo_secao_bula s " +
				"where b.medicamento_fk = m.id " +
				"    and s.bula_id = b.id " +
				"    and length(b.texto) >= :length " +
				"    and (select count(*) from conteudo_secao_bula s where s.bula_id=b.id) >= :sections " +
				"group by m.id");
			q.setParameter("length", minLength);
			q.setParameter("sections", minNumberOfSections);
			return (Collection<Integer>) q.getResultList();
		}
		catch (NoResultException e) {
			return null;
		}
	}
}
