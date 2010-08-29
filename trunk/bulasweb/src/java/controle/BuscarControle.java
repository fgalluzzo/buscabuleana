/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
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

import util.PersistenceFactory;
import DTO.BulaDTO;
import bean.BulaBean;
import bean.SecaoBulaBean;
import dao.BulaDao;
import dao.SecaoBulaDao;

/**
 *
 * @author galluzzo
 */
@ManagedBean(name="BuscarControle")
@RequestScoped
public class BuscarControle {

    public class SecoesBusca {
    	List <SecaoBulaBean> secoes = new ArrayList<SecaoBulaBean>();
    	
    	@Override
    	public String toString() {
    		String text = "";
    		boolean first = true;
    		for (SecaoBulaBean s : secoes) {
    			if (!first) text += ", ";
    			text += s;
    			first = false;
    		}
    		return text;
    	}
	}

	//private final String INDICE = CarregaCfg.config.getIndice();// nao seria bom ler de uma arquivo properties?
	private final String INDICE = "D:\\home\\expedit\\PPGI\\2010.2\\BRI\\workspace\\indice_bulas";

	private List<SecoesBusca> secoes;
	private List<SecoesBusca> secoesEscolhidas;
    private List<BulaBean> bulas;
    private int searchAt;
    
    
    private EntityManager em;
	private SecaoBulaDao sbd;
	private BulaDao bd;


    /** Creates a new instance of BuscarControle */
    public BuscarControle() {
    	
    	em = PersistenceFactory.createEntityManager();    	
    	bd = new BulaDao(em);
    	sbd = new SecaoBulaDao(em);
    	
        bulas = new ArrayList<BulaBean>();
        secoes = new ArrayList<SecoesBusca>();
        secoesEscolhidas = new ArrayList<SecoesBusca>();

        Collection<SecaoBulaBean> secoesBD = sbd.findAll(SecaoBulaBean.class);
        
        Map<Integer, SecoesBusca> m = new TreeMap<Integer, SecoesBusca> ();
        for (SecaoBulaBean s : secoesBD) {
        	
        	if (s.getGrupo() != null) {

            	SecoesBusca sb = null;
            	if (!m.containsKey(s.getGrupo())) sb = new SecoesBusca();
            	else sb = m.get(s.getGrupo());
            	
            	sb.secoes.add(s);
            	m.put(s.getGrupo(), sb);
        	}
        }
        for (Integer key : m.keySet()) {
        	secoes.add(m.get(key));
        }
        
        searchAt = 0;
    }
    
    @Override
    protected void finalize() throws Throwable {
    	// TODO Auto-generated method stub
    	super.finalize();
    	
    	em.close();
    }

    public void buscar() {

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression  expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "BulaDTO"), Object.class);
        BulaDTO bt = (BulaDTO) expression.getValue(context.getELContext());

        /*expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "BulaBean"), Object.class);
        BulaBean bb = (BulaBean) expression.getValue(context.getELContext());
        */
        if (!bt.getTextoPesquisa().equals("")) {


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
                    query = parser.parse(bt.getTextoPesquisa());

                    TopDocs topDocs = is.search(query, 50);

                    ScoreDoc[] docs = topDocs.scoreDocs;

                    for (int i = 0; i < docs.length; i++) {
                        Document doc = is.doc(docs[i].doc);
                        bulas.add(buscaBulaNoBanco(doc.get("code")));
                        
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
    
    public BulaBean buscaBulaNoBanco(String codigo){
        BulaBean bb = new BulaBean();
        bb = bd.getByCodigo(codigo);        
        return bb;
    }
    
    public List<BulaBean> getBulas() {

        return bulas;
    }
	
	public List<SecoesBusca> getSecoes() {
		return secoes;
	}
	
	public List<SecoesBusca> getSecoesEscolhidas() {
		return secoesEscolhidas;
	}

	public int getSearchAt() {
		return searchAt;
	}
}
