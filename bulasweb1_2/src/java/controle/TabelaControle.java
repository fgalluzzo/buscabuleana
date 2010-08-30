package controle;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import util.CarregaCfg;
import util.LuceneSearcher;
import util.PersistenceFactory;
import util.SymptomFieldConfig;
import DTO.TabelaDTO;
import DTO.TabelaDTO.ColumnData;
import bean.BulaBean;
import bean.MedicamentoBean;
import dao.BulaDao;

public class TabelaControle {
	
	private final SymptomFieldConfig config = CarregaCfg.loadSymptomFieldConfig();

	
	private EntityManager em = PersistenceFactory.createEntityManager();	
	private BulaDao bd = new BulaDao(em);
	
	
	public void buscar() {

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "TabelaDTO"), Object.class);
        TabelaDTO tb = (TabelaDTO) expression.getValue(context.getELContext());
        /*
        sintomas em indicacao -> 0.0 verde
        sintomas em informacao -> 0.75 amarelo
        sintomas em aviso -> 1.0 vermelho
        sintomas em contraIndicacao -> 1.0 vermelho
        sintomas em reacaoAdversa -> 1.0 vermelho

        doencas em indicacao -> 0.0 verde
        doencas em informacao -> 0.75 amarelo
        doencas em aviso -> 1.0 vermelho
        doencas em contraIndicacao -> 1.0 vermelho
        
        alergenicos em composicao - > 1.0 vermelho
        alergenicos em formaFarmaceutica -> 1.0 vermelho
        alergenicos em informacao -> 0.75 amarelo
        alergenicos em aviso -> 0.75 amarelo
        alergenicos em contraIndicacao -> 1.0 vermelho
        
        
        farmacos em interacao -> 1.0 vermelho
        farmacos em contraIndicacao -> 1.0 vermelho
        
        */
        
        Map <String, Map <String, Float []>> rows = new TreeMap<String, Map <String, Float []>>();

        try {
			LuceneSearcher searcher = new LuceneSearcher();

	        for (SymptomFieldConfig.Input input : config.getInputs()) {
	        	
	            expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
	                    String.format("#{%s.%s}", "TabelaDTO", input.getNome()), Object.class);
	            String inputString = (String) expression.getValue(context.getELContext());
	            
	            String [] inputTerms = inputString.split("[\\p{Punct} ]+");
	            for (String term : inputTerms) {
	
		            for (SymptomFieldConfig.Field field : input.getFields()) {
		            	
		            	System.out.println("fields="+field.getName());
	
		            	try {
							ScoreDoc [] docs = searcher.searchInField(term, field.getName());
							
		                    for (ScoreDoc sd : docs) {
	
		                        Document doc = searcher.getDocument(sd.doc);
		                        BulaBean bb = bd.getByCodigo(doc.get("code"));
		                        
		                        MedicamentoBean medb = bb.getMedicamento();
		                        if (medb != null) {
			                        String key = input.getNome() + "." + term;
			                        System.out.print("\tmed=" + medb.getNome() + "  key="+key);
			                        Float value = field.getWeight() * sd.score;
			                        
			                        if (!rows.containsKey(key)) {
			                        	Map <String, Float []> map = new TreeMap <String, Float []>();
			                        	rows.put(key, map);
			                        	System.out.print("  NE");
			                        }
			                        
			                        try {
			                        	Map <String, Float []> map = rows.get(key);
			                        	
			                        	if (!map.containsKey(medb.getNome())) {
			                        		Float [] v = new Float[] { 0.0f, 0.0f };//total, #sums
			                        		map.put(medb.getNome(), v);
			                        		System.out.print("  NE2");
			                        	}
				                        
				                        System.out.print("  map="+map);
				                        Float [] v = map.get(medb.getNome());
				                        v[0] += value;
				                        v[1] += 1;
				                        System.out.print("  v="+v);
			                        }
			                        catch (NullPointerException e) {
			                        	System.out.print("  EXCEP");
			                        }
			                        
			                        System.out.println("");
		                        }
		                    }
		                    
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            	
		            }
	            }
	        }
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("");
		// dividr v[0]/v[1]

	}
}
