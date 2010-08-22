/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controle;

import bean.UsuarioBean;
import dao.UsuarioDao;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import util.CriaHash;
import util.DataAccessLayerException;

/**
 *
 * @author galluzzo
 */
@ManagedBean(name="UsuarioControle")
public class UsuarioControle {
    private UsuarioDao usuDao = new UsuarioDao();
    
    public void novoUsuario() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "UsuarioBean"), Object.class);
        UsuarioBean usuarionovo = (UsuarioBean) expression.getValue(context.getELContext());

        try {
            usuarionovo.setSenha(CriaHash.SHA1(usuarionovo.getSenha()));
            this.usuDao.create(usuarionovo);
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataAccessLayerException ex) {
        	Logger.getLogger(UsuarioControle.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
}
