package dao;

import bean.ConteudoSecaoBean;
import java.util.List;

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
        } catch (NoResultException e) {
            return null;
        }
    }

    public SecaoBulaBean findByName(String name) {
        try {
            Query q = em.createQuery("from SecaoBula where nome = :nome");
            q.setParameter("nome", name);
            return (SecaoBulaBean) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<SecaoBulaBean> findByGrupo(Integer grupo) {
        try {
            Query q = em.createQuery("from SecaoBula where grupo = :grupo");
            q.setParameter("grupo", grupo);
            return (List<SecaoBulaBean>) q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ConteudoSecaoBean> getSecaoByNameByMedicamento(String secao, String medicamento) {
        try {
            Query q = em.createNativeQuery("select csb.id from secao_bula sb "
                    + "inner join conteudo_secao_bula csb on csb.secao_id = sb.id "
                    + "inner join bula b on b.id = csb.bula_id "
                    + "inner join medicamento m on m.id = b.medicamento_fk "
                    + "where nome_curto = (:secao) "
                    + "and m.nome=(:medicamento)");
            q.setParameter("secao", secao);
            q.setParameter("medicamento", medicamento);
            List<Integer> mids = q.getResultList();
            if (mids.size() == 0) {
                throw new NoResultException();
            }

            q = em.createQuery("from ConteudoSecao where id in (:mids)");
            q.setParameter("mids", mids);

            return (List<ConteudoSecaoBean>) q.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    public List<Integer> getGrupos() {
        try {
            Query q = em.createNativeQuery("select distinct grupo from secao_bula "
                    + "where grupo is not null order by grupo asc");
            return (List<Integer>) q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
