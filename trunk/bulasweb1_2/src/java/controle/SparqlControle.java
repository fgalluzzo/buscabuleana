package controle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SparqlControle {
	
	public void buscar() {
		
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "SparqlDTO"), Object.class);
        SparqlDTO sparql = (SparqlDTO) expression.getValue(context.getELContext());

        
        try {
			// Open the bloggers RDF graph from the filesystem
        	ServletContext sc = (ServletContext) context.getExternalContext().getContext();
            String dir = sc.getRealPath("") + "/";
			InputStream in = new FileInputStream(new File(dir + "ntriple/labels_pt.nt"));

			// Create an empty in-memory model and populate it from the graph
			Model model = ModelFactory.createMemModelMaker().createModel("labels");
			model.read(in,null,"N-TRIPLE"); // null base URI, since model URIs are absolute
			in.close();

			// Create a new query
			String queryString = sparql.getTextoPesquisa();

			Query query = QueryFactory.create(queryString);

			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();

			// Output query results
			//ResultSetFormatter.out(System.out, results, query);
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}

}
