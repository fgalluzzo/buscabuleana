package controle;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import DTO.BulaDTO;
import DTO.TabelaDTO;

public class TabelaControle {

	
	public void buscar() {

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "TabelaDTO"), Object.class);
        TabelaDTO tb = (TabelaDTO) expression.getValue(context.getELContext());

        
        System.out.println(tb.getAlergenicos());
        
	}
}
