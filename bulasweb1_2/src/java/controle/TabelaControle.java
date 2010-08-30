package controle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import DTO.TabelaDTO.RowData;
import bean.BulaBean;
import bean.MedicamentoBean;
import dao.BulaDao;
import dao.MedicamentoDao;

public class TabelaControle {
	
	private final SymptomFieldConfig config = CarregaCfg.loadSymptomFieldConfig();

	
	private EntityManager em = PersistenceFactory.createEntityManager();	
	private BulaDao bd = new BulaDao(em);
	private MedicamentoDao md = new MedicamentoDao(em); 
	
	
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
        
        Set <MedicamentoBean> medicamentos1 = new TreeSet<MedicamentoBean>(
        		new Comparator<MedicamentoBean>() {
					@Override
					public int compare(MedicamentoBean o1, MedicamentoBean o2) {
						// TODO Auto-generated method stub
						int r = o1.getNome().compareTo(o2.getNome());
						if (r==0) return new Integer(o1.getId()).compareTo(o2.getId());
						return r;
					}
				}
        	);
        
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
				                        
				                        // guarda medicamento
				                        medicamentos1.add(medb);
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
		
		
		List <RowData> results = new ArrayList<RowData>();
		List <MedicamentoBean> medicamentos = new ArrayList<MedicamentoBean>();
		
		for (String key : rows.keySet()) {
			Map <String, Float []> row = rows.get(key);

			RowData rowData = new RowData();
			ColumnData columnData = new ColumnData();
			columnData.setNome(key);
			columnData.setBackgroundColor("gray");
			rowData.setFirstColumn(columnData);
			rowData.setOtherColumns(new ColumnData[medicamentos1.size()]);
			
			int i = 0;
			for (MedicamentoBean medicBean : medicamentos1) {
				String medic = medicBean.getNome();

				columnData = new ColumnData();
				columnData.setNome("");

				if (row.containsKey(medic)) {

					Float [] v = row.get(medic);
					float weight = v[0] / v[1];

					columnData.setWeight(weight);

					// estes valores ainda devem ser conferidos
					if (weight < 0.25) columnData.setBackgroundColor("red");
					else if (weight < 0.9) columnData.setBackgroundColor("yellow");
					else columnData.setBackgroundColor("green");
				}
				else {
					columnData.setWeight(0.0f);
					columnData.setBackgroundColor("white");
				}
				
				rowData.getOtherColumns()[i] = columnData;
				medicamentos.add(medicBean);

				i++;
			}
			
			results.add(rowData);
		}

		tb.setMedicamentos(medicamentos);
		tb.setResults(results);
	}
}
