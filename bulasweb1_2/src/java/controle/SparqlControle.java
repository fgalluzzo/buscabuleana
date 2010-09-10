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
        //List<List<Row>> res = new ArrayList<List<Row>>();
        List<Row> row = new ArrayList<Row>();
        for (; results.hasNext();) {

            QuerySolution sol = results.next();

            
            Iterator<String> it = sol.varNames();
            Row r = new Row();
            for (; it.hasNext();) {
                RDFNode node = sol.get(it.next());
                String text = node.toString();
                text = text.replaceAll("\"", "&quot;");
                text = text.replaceAll("&", "&lt;");
                text = text.replaceAll("<", "&gt;");
                text = text.replaceAll(">", "&amp;");

                if (node.isURIResource()) {
                    String no = node.toString();
                    String dados[] = no.split("#");
                    String med = dados[1];
                    r.setMed(med);
                    r.setResource(text);

                } else {
                    r.setText(text);
                    row.add(r);
                }
            }

            //res.add(row);
        }
        sparql.setResults(row);

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
