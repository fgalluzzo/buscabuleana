package model.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import model.bean.UsuarioBean;


/**
 *
 * @author galluzzo, expedit
 */
public class UsuarioDao extends AbstractDao <UsuarioBean> {
     public UsuarioDao(EntityManager em) {
        super(em);
    }
     
     public UsuarioBean findByLogin(String name) {
 		try {
			Query q = em.createQuery("from Usuario where nome = :nome");
			q.setParameter("nome", name);
			return (UsuarioBean) q.getResultList();
		}
		catch (NoResultException e) {
			return null;
		}
     }

    /**
     * Insert a new Event into the database.
     * @param event
     */
    /*public UsuarioBean findByLogin(UsuarioBean usuario){
        HibernateFactory.buildIfNeeded();
        Session session = null;
        UsuarioBean usuarioL = null;
        Transaction tx = null;
        try {
            session = HibernateFactory.openSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UsuarioBean WHERE login ='"+usuario.getLogin()+"'");
            usuarioL = (UsuarioBean)query.uniqueResult();
            Hibernate.initialize(usuarioL.getQuestions_owned());
            Hibernate.initialize(usuarioL.getQuestions_participation());
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
             HibernateFactory.close(session);
        }
        return usuarioL;
    }*/
 
//    public void create(UsuarioBean usuario) throws DataAccessLayerException {
//        super.saveOrUpdate(usuario);
//    }

    /**
     * Delete a detached Event from the database.
     * @param event
     */
//    public void delete(UsuarioBean usuario) throws DataAccessLayerException {
//        super.delete(usuario);
//    }

    /**
     * Find an Event by its primary key.
     * @param id
     * @return
     */
//    public UsuarioBean find(Integer id) throws DataAccessLayerException {
//        return (UsuarioBean) super.find(UsuarioBean.class, id);
//    }

    /**
     * Updates the state of a detached Event.
     *
     * @param event
     */
//    public void update(UsuarioBean usuario) throws DataAccessLayerException {
//        super.saveOrUpdate(usuario);
//    }

    /**
     * Finds all Events in the database.
     * @return
     */
//    @SuppressWarnings("unchecked")
//	public List<UsuarioBean> findAll() throws DataAccessLayerException{
//        return super.findAll(UsuarioBean.class);
//    }

    /*@SuppressWarnings("unchecked")
    public List<UsuarioBean> findAllDifferentLogin(UsuarioBean usuario) {
        HibernateFactory.buildIfNeeded();
        Session session = null;
        List<UsuarioBean> usuarios = null;
        Transaction tx = null;

        try {
            session = HibernateFactory.openSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UsuarioBean WHERE login <> :login");
            query.setString("login", usuario.getLogin());
            usuarios = query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
             HibernateFactory.close(session);
        }

        return usuarios;
    } */
}
