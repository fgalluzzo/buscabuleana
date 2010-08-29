/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
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
import bean.ConteudoSecaoBean;
import bean.MedicamentoBean;
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

    public class SingleResult implements Comparable<SingleResult> {
    	private float score;
    	private String selected;
    	private BulaBean bula;
    	private ConteudoSecaoBean secao;

		public SingleResult(float score, BulaBean bb, ConteudoSecaoBean csb) {
			super();
			this.selected = "true";
			this.score = score;
			this.bula = bb;
			this.secao = csb;
		}
		
		public String getSelected() {
			return selected;
		}
		
		public void setSelected(String selected) {
			this.selected = selected;
		}
		
		public float getScore() {
			return score;
		}
		
		public BulaBean getBula() {
			return bula;
		}
		
		public ConteudoSecaoBean getSecao() {
			return secao;
		}

		@Override
		public int compareTo(SingleResult arg) {
			// Para colocar bula sem medicamento associado no final da tabela
			MedicamentoBean aMedic = this.getBula().getMedicamento();
			MedicamentoBean bMedic = arg.getBula().getMedicamento();

			if (aMedic==null && bMedic!=null) return 1;
			if (aMedic!=null && bMedic==null) return -1;

			if (aMedic!=null&& bMedic!=null) {
				String aNome = aMedic.getNome();
				String bNome = bMedic.getNome();

				if ("".equals(aNome) && !"".equals(bNome)) return 1;
				if (!"".equals(aNome) && "".equals(bNome)) return -1;
			}

			// Ordena
			if (this.score > arg.score) return -1;
			else if (this.score < arg.score) return 1;
			else return this.bula.getCodigo().compareTo(arg.bula.getCodigo());
		}
    }


	//private final String INDICE = CarregaCfg.config.getIndice();// nao seria bom ler de uma arquivo properties?
	private final String INDICE = "D:\\home\\expedit\\PPGI\\2010.2\\BRI\\workspace\\indice_bulas";

	private List<String[]> secoes;
	private List< String> secoesEscolhidas;
    private List<SingleResult> results;
    //private Set<SingleResult> selectedResults;
    private String searchAt;


    private EntityManager em;
	private SecaoBulaDao sbd;
	private BulaDao bd;


    /** Creates a new instance of BuscarControle */
    public BuscarControle() {
    	
    	em = PersistenceFactory.createEntityManager();    	
    	bd = new BulaDao(em);
    	sbd = new SecaoBulaDao(em);

        results = new ArrayList<SingleResult>();
        secoesEscolhidas = new ArrayList<String>();
        secoes = new ArrayList<String[]>();
        //selectedResults = new TreeSet<SingleResult>();

		List<Integer> grupos = sbd.getGrupos();
		for (Integer i : grupos) {
			String [] v = new String[2];
			v[0] = i.toString();

			List<SecaoBulaBean> ss = sbd.findByGrupo(i);
    		String text = "";
    		boolean first = true;
    		for (SecaoBulaBean s : ss) {
    			if (!first) text += ", ";
    			text += s;
    			first = false;
    		}
    		v[1] = text;
    		
    		secoes.add(v);
		}

        searchAt = "";
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
                try {
                    dir = FSDirectory.open(f);
                    IndexReader ir;
                    ir = IndexReader.open(dir);
                    IndexSearcher is = new IndexSearcher(ir);
                    SimpleAnalyzer analyzer = new SimpleAnalyzer();

                    if (searchAt.equals("fulltext")) searchFullText(is, analyzer, bt);
                    else if (searchAt.equals("sections")) searchBySections(is, analyzer, bt);

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
    
    private ScoreDoc[] searchInField(IndexSearcher is, SimpleAnalyzer analyzer, BulaDTO bt, String field)
    			throws ParseException, IOException {
        QueryParser parser = new QueryParser(Version.LUCENE_20, field, analyzer);
        Query query = parser.parse(bt.getTextoPesquisa());
        TopDocs topDocs = is.search(query, 50);
        ScoreDoc[] docs = topDocs.scoreDocs;
        return docs;
    }

    private void searchFullText(IndexSearcher is, SimpleAnalyzer analyzer, BulaDTO bt) throws ParseException, IOException {

    	ScoreDoc [] docs = searchInField(is, analyzer, bt, "text");

        for (int i = 0; i < docs.length; i++) {
            Document doc = is.doc(docs[i].doc);
            BulaBean bb = buscaBulaNoBanco(doc.get("code"));
            results.add(new SingleResult(docs[i].score, bb, null));
        }
	}


	private void searchBySections(IndexSearcher is, SimpleAnalyzer analyzer, BulaDTO bt) {
		
		TreeSet<SingleResult> set = new TreeSet<SingleResult>();

    	for (String se : secoesEscolhidas) {//1,2,3,4...
    		
    		for (SecaoBulaBean sbb : sbd.findByGrupo(Integer.parseInt(se))) {
    			ScoreDoc[] docs;
				try {
					docs = searchInField(is, analyzer, bt, sbb.getNomeCurto());

					for (ScoreDoc sd : docs) {
						
						Document doc = is.doc(sd.doc);
						BulaBean bb = buscaBulaNoBanco(doc.get("code"));
						ConteudoSecaoBean csb = bb.getSectionByName(sbb.getNomeCurto());
						set.add(new SingleResult(sd.score, bb, csb));
	    			}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    	for (SingleResult sr : set) {
    		results.add(sr);
    	}
	}
	
	public void bulaSelected(ValueChangeEvent e) {
		System.out.println(e.getComponent().getClientId());
		System.out.println(e.getComponent().getId());
		
		SingleResult sr = null;
		Iterator <SingleResult> it = results.iterator();
		while (it.hasNext()) {
			sr = it.next();

			if (sr.getBula().getCodigo().equals(e.getComponent().getClientId())) {
				break;
			}
		}

		if (e.getNewValue().equals("true")) {
			sr.setSelected("true");
		}
		else if (e.getNewValue().equals("false")) {
			sr.setSelected("false");
		}
	}

	public BulaBean buscaBulaNoBanco(String codigo){
        BulaBean bb = new BulaBean();
        bb = bd.getByCodigo(codigo);        
        return bb;
    }
    
    public List<SingleResult> getResults() {

        return results;
    }
	
	public List<String[]> getSecoes() {
		return secoes;
	}
	
	public List<String> getSecoesEscolhidas() {
		return secoesEscolhidas;
	}

	public String getSearchAt() {
		return searchAt;
	}
	
	public void setSecoesEscolhidas(List<String> secoesEscolhidas) {
		this.secoesEscolhidas = secoesEscolhidas;
	}
	
	public void setSecoes(List<String[]> secoes) {
		this.secoes = secoes;
	}
	
	public void setSearchAt(String searchAt) {
		this.searchAt = searchAt;
	}
}
