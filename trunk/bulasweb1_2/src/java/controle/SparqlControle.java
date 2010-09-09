package controle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import DTO.SparqlDTO;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.tdb.TDBFactory;

public class SparqlControle {
	
	public void buscar() {
		
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "SparqlDTO"), Object.class);
        SparqlDTO sparql = (SparqlDTO) expression.getValue(context.getELContext());

        
        // Obtem caminho do indice de N-Triplas e abre Model
		ServletContext sc = (ServletContext) context.getExternalContext().getContext();
		String dir = sc.getRealPath("") + "/";
		String directory = dir + "tdb";
		Model model = TDBFactory.createModel(directory);

		// Create a new query
		String queryString = sparql.getTextoPesquisa();
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		List <List<String>> res = new ArrayList<List<String>>();
		for (; results.hasNext(); ) {
			
			QuerySolution sol = results.next();
			
			List<String> row = new ArrayList<String>();
			Iterator<String> it = sol.varNames();
			for (; it.hasNext() ; ) {
				RDFNode node = sol.get(it.next());
				String text = node.toString();
				text = text.replaceAll("\"", "&quot;");
				text = text.replaceAll("&", "&lt;");
				text = text.replaceAll("<", "&gt;");
				text = text.replaceAll(">", "&amp;");

				if (node.isURIResource())
					row.add("<a href='" + node.toString() + "'>"+text+"</a>");
				else row.add(text);
			}
			
			res.add(row);
		}
		sparql.setResults(res);

		// Important – free up resources used running the query
		qe.close();
	}

}
