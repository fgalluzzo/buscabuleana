package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.swing.JOptionPane;

import util.PersistenceFactory;
import bean.FarmacoBean;
import bean.MedicamentoBean;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

import controle.RdfControle;
import dao.MedicamentoDao;

public class TdbGenerator {
	

	public static void main(String[] args) throws FileNotFoundException {
		
		// Exemplo de consulta TDB
		if (false) {
			String queryString = JOptionPane.showInputDialog("SPARQL:");
			new TdbGenerator("../bulasweb1_2/web/tdb/", "../bulasweb1_2/web/gate/").searchSparql(queryString);
		}
		
		
		// Para gerar indice TDB
		if (true) {
			TdbGenerator tdbGen = new TdbGenerator("./tdb", "../bulasweb1_2/web/gate/");
			//tdbGen.filterLabels();
			tdbGen.geraTuplasDbPedia();
			tdbGen.geraTuplasBulasWeb();
		}
	}
	
	
	private String bulasWebNS = "http://bulasweb.test/rdf#";
	private MedicamentoDao medDao;
	private EntityManager em;
	private String indexOutDirectory;
	private String inputCsvDirectory;


	public TdbGenerator(String indexOutDirectory, String inputCsvDirectory) {
		this.indexOutDirectory = indexOutDirectory;
		this.inputCsvDirectory = inputCsvDirectory;
	}
	
	
	/**
	 * Exemplo de consulta com TDB
	 * 
	 * @param queryString
	 */
	public List <List<RDFNode>> searchSparql(String queryString) {

		Model model = TDBFactory.createModel(indexOutDirectory);
		return searchSparql(queryString, model);
	}
	
	
	public List <List<RDFNode>> searchSparql(String queryString, Model model) {

		// Create a new query
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		List <List<RDFNode>> res = new ArrayList<List<RDFNode>>();
		for (; results.hasNext(); ) {
			
			QuerySolution sol = results.next();
			
			List<RDFNode> row = new ArrayList<RDFNode>();
			Iterator<String> it = sol.varNames();
			for (; it.hasNext() ; ) {
				RDFNode node = sol.get(it.next());
				String text = node.toString();
				text = text.replaceAll("\"", "&quot;");
				text = text.replaceAll("&", "&lt;");
				text = text.replaceAll("<", "&gt;");
				text = text.replaceAll(">", "&amp;");
				
				row.add(node);
			}
			res.add(row);
		}

		// Important � free up resources used running the query
		qe.close();

		return res;
	}
	
	/**
	 * Procura nodes que possuei label especificado
	 * @param label
	 * @param model
	 * @param lang  pt, en, fr, ...
	 * @return retorna uma lista lista que poder contem zero ou mais elementos (RDFNode's)
	 */
	public List <RDFNode> findNodesByLabel(String label, Model model, String lang) {
		String queryString = String.format(
				"SELECT ?a WHERE { ?a <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@%s . }", label, lang);
		
		List <List<RDFNode>> res = this.searchSparql(queryString, model);
		//if (res.size() == 0)  return null;
		List <RDFNode> ret = new ArrayList<RDFNode>();
		for (List <RDFNode> node : res) ret.add(node.get(0));
		return ret;
	}
	
	
	/**
	 * ATEN��O: este metodo retorna UM e SEMPRE UM node, criando um literal se necessario
	 * 
	 * @param label
	 * @param model
	 * @param lang
	 * @return
	 */
	public RDFNode findNodeByLabel(String label, Model model, String lang) {
		String queryString = String.format(
				"SELECT ?a WHERE { ?a <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@%s . }", label, lang);
		
		List <List<RDFNode>> res = this.searchSparql(queryString, model);
		if (res.size() > 0) {
			return res.get(0).get(0);
		}
		else return model.createLiteral(label, lang);
	}
	
	
	public void filterLabels() {
		// Preenche conjunto de labels das listas .lst
		HashSet <String> labels = new HashSet<String>();
		
		File dir = new File("./lst/");
		File [] lists = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*lst");
			}
		});
		for (File file : lists) {
			
				try {
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader in = new BufferedReader(isr);  

					while (in.ready()) {
						labels.add(in.readLine().toLowerCase());
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		System.out.println(labels.size());

		this.filterLabels(new File("labels_pt.nt"), new File("labels_pt.small.nt"), labels);
	}
	
	/**
	 * Filtra arquivo de N-Triplas mantendo apenas aquelas que coincidem com labels
	 * 
	 * 
	 * @param inputFile arquivo .nt de entrada 
	 * @param outputFile arquivo .nt de saida
	 * @param labels lista de labels que devem ser mantidos
	 * 
	 * Complexidade esperada: O(N), N = tamanho do arquivo de entrada
	 */
	public void filterLabels(File inputFile, File outputFile, Set <String> labels) {

		try {
			long passed = 0;
			long linesRead = 0;
			long faults = 0;
			long unuseful = 0;
			long filesize = inputFile.length();
			long bytesRead = 0;
			long startTime = System.currentTimeMillis();
			long lastTime = System.currentTimeMillis();
			
			String lineRegex = "^<[^>\"]*> <[^>\"]*> \"(.+)\"@pt .$";
			Pattern p = Pattern.compile(lineRegex);
			

			FileWriter fw = new FileWriter(outputFile);

			FileReader fr = new FileReader(inputFile);
			BufferedReader in = new BufferedReader(fr);


			while (in.ready()) {	// O(L), L = numero de linhas
				String linha = in.readLine();	// O(C[l]), C[l] = numero de colunas na linha l
				bytesRead += linha.length();
				linesRead++;

				Matcher m = p.matcher(linha);
				if (m.matches()) {	// O(C[l])
					String label = m.group(1).toLowerCase();
					String label2 = "";

					// Decodifica uXXXX para char
					int lastIndex = 0;
					int index = label.indexOf("\\u");
					while (index >= 0) {
						char c = (char) Integer.parseInt(label.substring(index+2, index+6), 16);
						label2 += label.subSequence(lastIndex, index) + "" + c;

						lastIndex = index+6;
						index = label.indexOf("\\u", lastIndex);
					}
					if ("".equals(label2))label2 = label;
					else label2 += label.substring(lastIndex); 

					
					if (labels.contains(label2)) {	// O(1), se lables for hash
						fw.write(linha);
						fw.write('\n');
						passed++;
					}
					else unuseful++;
				}
				else {
					System.out.println(String.format("\"%s\" n�o casa com express�o... repassando para sa�da.", linha));
					fw.write(linha);
					fw.write('\n');
					faults++;
				}

				long curTime = System.currentTimeMillis();
				if (curTime - lastTime > 10*1000) {	//progresso a cada 10s
					lastTime = System.currentTimeMillis();
					System.out.println(String.format("%4.2f%% concluido (%d de %d)", bytesRead/(float)filesize, bytesRead, filesize));
					fw.flush();
				}
			}
			
			long curTime = System.currentTimeMillis();
			System.out.println(String.format("passed=%d\tfaults=%d\tlinesRead=%d\tunuseful=%d\ttime=%5.1f s\n",
					passed, faults, linesRead, unuseful, ((float)(curTime - startTime))/1000.0));
			
			fw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Insere N-Triplas no indice de bulas. Utiliza labels_pt.small.nt oriundo da DBPedia porem filtrado 
	 *   
	 * @throws FileNotFoundException
	 */
	public void geraTuplasDbPedia() throws FileNotFoundException {
		
		Model model = TDBFactory.createModel(indexOutDirectory);
		
		InputStream in = new FileInputStream(new File("labels_pt.small.nt"));
		model.read(in,null,"N-TRIPLE"); // null base URI, since model URIs are absolute
		model.commit();
		
		model.close();
	}

	public void geraTuplasBulasWeb() {
		em = PersistenceFactory.createEntityManager();
		medDao = new MedicamentoDao(em);

		
		Model model = TDBFactory.createModel(indexOutDirectory);
		model.setNsPrefix("bw", bulasWebNS);

		int i = 0;
		Collection<MedicamentoBean> meds = medDao.findAll(MedicamentoBean.class);
		for (MedicamentoBean med : meds) {
			
			System.out.println(i+"/"+meds.size()+"\t");

			geraNtriple(med, model, bulasWebNS);
			model.commit();
			
			i++;
			//if (i>10)break;
		}

		try {
			model.write(new FileWriter("./bulas.nt"), "N-TRIPLE");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.close();
		em.close();
	}

	
	/**
	 * 
	 * TODO que faltam tupla M possui_farmaco F. Algo mais?
	 * 
	 * @param med
	 * @param model
	 * @param bulasWebNS
	 */
	public void geraNtriple(MedicamentoBean med, Model model, String bulasWebNS) {

		System.out.print(med.getNome() + ": ");

		String mdURI = bulasWebNS + med.getNome();
		String lang = "pt";
		RDFNode node = null;
		
		// Propriedades (acho melhor n�o usar PropertyImpl)
		Property nomeProperty = model.createProperty(bulasWebNS, "nome");
		Property laboratorioProperty = model.createProperty(bulasWebNS, "laboratorio");
		Property farmacoProperty = model.createProperty(bulasWebNS, "farmaco");
		//Property interacao_medicamentosaProperty = model.createProperty(bulasWebNS, "interacao_medicamentosa");
		
		Property indicadoProperty = model.createProperty(bulasWebNS, "indicado");
		Property contra_indicadoProperty = model.createProperty(bulasWebNS, "contra_indicado");
		Property interageProperty = model.createProperty(bulasWebNS, "interage");
		Property reacaoProperty = model.createProperty(bulasWebNS, "reacao");

		
		// Cria��o do recurso medicamento
		Resource medicamentoRes = model.createResource(mdURI);

				
		// Cria��o das propriedades a partir do banco
		medicamentoRes.addProperty(nomeProperty, med.getNome());
		
		node = this.findNodeByLabel(med.getLaboratorio().getNome(), model, lang);
		medicamentoRes.addProperty(laboratorioProperty, node);
		
		// Cria bag de farmacos
		List<FarmacoBean> farms = med.getFarmacos();
		for (FarmacoBean farm : farms) {
			node = this.findNodeByLabel(farm.getNome(), model, lang);
			medicamentoRes.addProperty(farmacoProperty, node);
		}
		
		
		// Cria bag de remedios que interagem com ele (talvez devesse juntar com interage do CSV)
		List<MedicamentoBean> medInt = medDao.findInteracaoMedicamentosa(med.getNome());
		for (int i = 0; i < medInt.size(); i++) {
			node = this.findNodeByLabel(medInt.get(i).getNome(), model, lang);
			medicamentoRes.addProperty(interageProperty, node);
		}
		
		
		
		// Cria��o das propriedades a partir do csv
		if (med.getBulas() != null && med.getBulas().size() > 0) {
			
			String bulaCodigo = med.getBulas().get(0).getCodigo();
			File csv = new File(inputCsvDirectory + "/" + bulaCodigo + ".csv");
			System.out.print(csv.getName());
			
			FileReader fr;
			try {
				fr = new FileReader(csv);
				BufferedReader in = new BufferedReader(fr);
				while (in.ready()) {
					
					String linha = in.readLine();
					String [] dados = linha.split(",");
					
					// Obtem nomes da propriedade e do objeto e procura o recurso associado ao objeto
					String propriedade = dados[0].replaceAll("\"", "");
					String object = dados[1].replaceAll("\"", "");
					object = capitalize(object);
		
					node = this.findNodeByLabel(object, model, lang);

					
					// Adiciona a (propriedade,object) ao modelo 
					if (propriedade.matches("^interage_com_nome")) {
						medicamentoRes.addProperty(interageProperty, node);
					}
					else if (propriedade.matches("^interage_com_farmaco")) {
						medicamentoRes.addProperty(interageProperty, node);
					}
					else if (propriedade.matches("^indicacao_para_.*")) {
						medicamentoRes.addProperty(indicadoProperty, node);
					}
					else if (propriedade.matches("^contra_indicado_para_.*")) {
						medicamentoRes.addProperty(contra_indicadoProperty, node);
					}
					else if (propriedade.matches("^provoca_reacao_.*")) {
						medicamentoRes.addProperty(reacaoProperty, node);
					}
					else {
						System.out.print("Falha! propriedade=" + propriedade);
					}
				}

			} catch (FileNotFoundException ex) {
				Logger.getLogger(RdfControle.class.getName()).log(Level.SEVERE, null,
						ex);
			} catch (IOException ex) {
				Logger.getLogger(RdfControle.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}

		System.out.println("");
	}


	/**
	 * TODO
	 * 
	 * @param text
	 * @return
	 */
	private String capitalize(String text) {
		
		HashSet <String> set = new HashSet<String>();
		set.add("para");
		set.add("por");
		//set.add("para");//...
		
		String [] words = text.split("\\s");
		text = "";
		for (String s : words) {
			if (s.length() > 2 && !set.contains(s.toLowerCase())) text += s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + " ";
			else text += s.toLowerCase() + " ";
		}

		return text.trim();
	}

}
