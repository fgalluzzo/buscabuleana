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

import arq.load;

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
import javax.persistence.EntityManager;
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
        List<List<String>> res = new ArrayList<List<String>>();
        for (; results.hasNext();) {

            QuerySolution sol = results.next();

            List<String> row = new ArrayList<String>();
            Iterator<String> it = sol.varNames();
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
                    row.add("<h:commandLink action='#{SparqlControle.detalhar}' value='" + text + "'>"
                            + "<f:setPropertyActionListener value='" + med + "' target='#{MedicamentoDTO.medicamento.nome}'></f:setPropertyActionListener>"
                            + "</h:commandLink>");

                } else {
                    row.add(text);
                }
            }

            res.add(row);
        }
        sparql.setResults(res);

        // Important � free up resources used running the query
        qe.close();
    }

    public void detalhar() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());
        MedicamentoDao mdao = new MedicamentoDao(PersistenceFactory.getEntityManager());

        md.setMedicamento(mdao.findByName(md.getMedicamento().getNome()));
        try {
            context.getExternalContext().redirect("/bulasweb1_2/faces/detalhe.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SparqlControle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
