package controle;


import DTO.MedicamentoDTO;
import bean.MedicamentoBean;
import dao.MedicamentoDao;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import util.PersistenceFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author galluzzo
 */
public class InteracaoControle {

    private List<MedicamentoBean> medicamentosInteracao = null;
     private EntityManager em;
    MedicamentoDao medDao;
    /** Creates a new instance of InteracaoControle */   

    public InteracaoControle(){

       
    }

    public List<MedicamentoBean> getMedicamentosInteracao() {
         em = PersistenceFactory.createEntityManager();
        medDao = new MedicamentoDao(em);
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());


        medicamentosInteracao = medDao.findInteracaoMedicamentosa(md.getMedicamento().getNome());

        return medicamentosInteracao;
    }

    public void setMedicamentosInteracao(List<MedicamentoBean> medicamentosInteracao) {
        this.medicamentosInteracao = medicamentosInteracao;
    }

    

}
