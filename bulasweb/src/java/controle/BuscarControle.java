/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import bean.BulaBean;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author galluzzo
 */
@ManagedBean(name="BuscarControle")
@RequestScoped
public class BuscarControle {

    private final String INDICE = "c:/indice_bulas";
    private List<String> Bulas;

    /** Creates a new instance of BuscarControle */
    public BuscarControle() {
        Bulas = new ArrayList<String>();

    }

    public void buscar() {

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "BulaBean"), Object.class);

        BulaBean bb = (BulaBean) expression.getValue(context.getELContext());

        if (!bb.getTexto().equals("")) {


            File f = new File(INDICE);
            Directory dir;
            {
                Query query = null;
                try {
                    dir = FSDirectory.open(f);
                    IndexReader ir;
                    ir = IndexReader.open(dir);
                    IndexSearcher is = new IndexSearcher(ir);
                    SimpleAnalyzer analyzer = new SimpleAnalyzer();
                    QueryParser parser = new QueryParser(Version.LUCENE_20, "indicacao", analyzer);
                    query = parser.parse(bb.getTexto());

                    TopDocs topDocs = is.search(query, 50);

                    ScoreDoc[] docs = topDocs.scoreDocs;

                    for (int i = 0; i < docs.length; i++) {
                        Document doc = is.doc(docs[i].doc);
                        Bulas.add(doc.get("id"));
                    }

                    is.close();
                    dir.close();

                } catch (ParseException ex) {
                    Logger.getLogger(BuscarControle.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CorruptIndexException ex) {
                    Logger.getLogger(BuscarControle.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BuscarControle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public List<String> getBulas() {

        return Bulas;
    }

    public void iniciar() {
        Bulas.add("Remedio 1");
        Bulas.add("Remedio 2");
        Bulas.add("Remedio 3");
        Bulas.add("Remedio 4");
        Bulas.add("Remedio 5");
        Bulas.add("Remedio 6");
    }
}
