/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controle;

import DTO.MedicamentoDTO;
import bean.ConteudoSecaoBean;
import bean.SecaoBulaBean;
import dao.SecaoBulaDao;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import util.PersistenceFactory;


/**
 *
 * @author galluzzo
 */
public class SecaoControle {
    private List<ConteudoSecaoBean> secoes;
    SecaoBulaDao sbDao;
    private EntityManager em;
    /** Creates a new instance of SecaoControle */
    public SecaoControle() {
    }

    public List<ConteudoSecaoBean> getComposicoes(){
         em = PersistenceFactory.createEntityManager();
        sbDao = new SecaoBulaDao(em);
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());

        secoes = sbDao.getSecaoByNameByMedicamento("composicao", md.getMedicamento().getNome());

        return secoes;
    }
    public  List<ConteudoSecaoBean> getFormaFarmaceutica(){
         em = PersistenceFactory.createEntityManager();
        sbDao = new SecaoBulaDao(em);
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());

        secoes = sbDao.getSecaoByNameByMedicamento("formaFarmaceutica", md.getMedicamento().getNome());

        return secoes;
    }
    public List<ConteudoSecaoBean> getSecoes() {

        return secoes;
    }

    public void setSecoes(List<ConteudoSecaoBean> secoes) {
        this.secoes = secoes;
    }




}
