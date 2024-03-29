/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controle;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import util.CriaHash;
import util.DataAccessLayerException;
import util.PersistenceFactory;
import bean.UsuarioBean;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityExistsException;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author galluzzo
 */
@ManagedBean(name="UsuarioControle")
public class UsuarioControle {
    //private UsuarioDao usuDao = new UsuarioDao();

    public void novoUsuario() {
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ValueExpression expression = app.getExpressionFactory()
				.createValueExpression(context.getELContext(),
						String.format("#{%s}", "UsuarioBean"), Object.class);
		UsuarioBean usuarionovo = (UsuarioBean) expression.getValue(context
				.getELContext());
		expression = app.getExpressionFactory().createValueExpression(
				context.getELContext(),
				String.format("#{%s}", "MensagemControle"), Object.class);

		try {
			usuarionovo.setSenha(CriaHash.SHA1(usuarionovo.getSenha()));
			// this.usuDao.create(usuarionovo);

			// Persiste usuario
			EntityManager em = PersistenceFactory.getEntityManager();
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				em.persist(usuarionovo);
				tx.commit();

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Info:",
						"Usuário cadastrado com sucesso!"));

			} catch (EntityExistsException e) {
				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "Problema:",
						"Nome de usuário já existente!"));
				tx.rollback();
			} catch (Exception e) {
				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_FATAL, "Erro Grave:",
						"Contate o administrador do sistema!"));
				tx.rollback();
			} finally {
				em.close();
				usuarionovo.setDtNascimento(null);
				usuarionovo.setNome(null);
				usuarionovo.setSenha(null);
				usuarionovo.setUsuario(null);
			}

		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DataAccessLayerException ex) {
			Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
		}
    }
}
