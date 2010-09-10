package controle;

import DTO.MedicamentoDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import DTO.SparqlDTO;
import DTO.StringDTO;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.tdb.TDBFactory;
import dao.MedicamentoDao;
import util.PersistenceFactory;

public class SparqlControle {

    public class Cell {
        private String resource;
        private String med;
        private String text;
        private boolean literal = false;
        private boolean details = false;
        private boolean external = false;
                
        public Cell(String text) {
			super();
			this.text = text;
			literal = true;
		}
		public Cell(String resource, String med, String text) {
			super();
			this.resource = resource;
			this.med = med;
			this.text = text;
			details = true;
		}
		
		public Cell(String resource, String text) {
			super();
			this.resource = resource;
			this.text = text;
			external = true;
		}
		
		public boolean isDetails() {
			return details;
		}
		public void setDetails(boolean details) {
			this.details = details;
		}
		public boolean isExternal() {
			return external;
		}
		public void setExternal(boolean external) {
			this.external = external;
		}
		public boolean isLiteral() {
			return literal;
		}
        public void setLiteral(boolean literal) {
			this.literal = literal;
		}

        public String getMed() {
            return med;
        }

        public void setMed(String med) {
            this.med = med;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

	}

	// Model estatico para n�o precisar criar varias vezes
    private static Model model = loadModel();

    public static Model loadModel() {
        // Obtem caminho do indice de N-Triplas e abre Model
        FacesContext context = FacesContext.getCurrentInstance();
        ServletContext sc = (ServletContext) context.getExternalContext().getContext();
        String dir = sc.getRealPath("") + "/";
        String directory = dir + "tdb";
        Model model = TDBFactory.createModel(directory);
        return model;
    }

    public void buscar() {

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "SparqlDTO"), Object.class);
        SparqlDTO sparql = (SparqlDTO) expression.getValue(context.getELContext());


        // Create a new query
        String queryString = "";
        for (int i = 0; i < sparql.getPrefixos().size(); i++) {
            queryString += sparql.getPrefixos().get(i);
        }
        queryString += sparql.getTextoPesquisa();
        Query query = QueryFactory.create(queryString);

        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        
        // Output query results
        List<List<Cell>> res = new ArrayList<List<Cell>>();
        //List<Row> rows = new ArrayList<Row>();
        for (; results.hasNext();) {

            QuerySolution sol = results.next();

            List<Cell> row = new ArrayList<Cell>();
            Iterator<String> it = sol.varNames();
            //Row r = new Row();
            Cell cell = null;
            for (; it.hasNext();) {
                RDFNode node = sol.get(it.next());
                String text = node.toString();
                text = text.replaceAll("\"", "&quot;");
                text = text.replaceAll("&", "&lt;");
                text = text.replaceAll("<", "&gt;");
                text = text.replaceAll(">", "&amp;");

                if (node.isURIResource()) {
                    String no = node.toString();
                    
                    if (no.contains("bulasweb")) {
                        String dados[] = no.split("#");
                        String med = dados[1];
                    
                        cell = new Cell(text, med, text);
                    //r.setMed(med);
                    //r.setResource(text);
                    }
                    else if (no.contains("dbpedia")) {
                    	cell = new Cell(text, text);
                    }

                } else {
                	cell = new Cell(text);
                    //r.setText(text);
                    //rows.add(r);
                }
                row.add(cell);
            }

            res.add(row);
        }
        sparql.setResults(res);
        sparql.setHeaders(new ArrayList<String>(results.getResultVars()));

        // Important � free up resources used running the query
        qe.close();
    }

    public void detalhar() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "StringDTO"), Object.class);
        StringDTO sdto = (StringDTO) expression.getValue(context.getELContext());
        expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());
        MedicamentoDao mdao = new MedicamentoDao(PersistenceFactory.getEntityManager());

        md.setMedicamento(mdao.findByName(sdto.getTexto()));
        try {
            context.getExternalContext().redirect("/bulasweb1_2/faces/detalhe.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SparqlControle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class Row{
        private String resource;
        private String med;
        private String text;

        public String getMed() {
            return med;
        }

        public void setMed(String med) {
            this.med = med;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        
    }
}
