package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.tdb.TDBFactory;

import controle.RdfControle;
import dao.FarmacoDao;
import dao.MedicamentoDao;

public class TdbGenerator {
	

	public static void main(String[] args) throws FileNotFoundException {
		
		// Exemplo de consulta TDB
		//String queryString = JOptionPane.showInputDialog("SPARQL:");
		//new TdbGenerator("../bulasweb1_2/web/tdb/", "../bulasweb1_2/web/gate/").searchSparql(queryString);
		
		
		// Para gerar indice TDB
		TdbGenerator tdbGen = new TdbGenerator("/tdb", "../bulasweb1_2/web/gate/");
		//tdbGen.geraTuplasDbPedia();
		tdbGen.geraTuplasBulasWeb();
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

		}

		// Important – free up resources used running the query
		qe.close();

		return res;
	}
	
	
	
	/**
	 * Insere N-Triplas labels_pt da DBPedia no indice de bulas
	 * 
	 * TODO Filtrar para inserir apenas tuplas relevantes (Farmacos, Sintomas, Doenças, etc).
	 *  
	 * @throws FileNotFoundException
	 */
	public void geraTuplasDbPedia() throws FileNotFoundException {
		
		Model model = TDBFactory.createModel(indexOutDirectory);
		
		InputStream in = new FileInputStream(new File("labels_pt.nt"));
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
		
		//TODO keep existing ... if (model.contains(arg0, arg1))

		// Criação do recurso medicamento
		Resource medicamentoRes = model.createResource(mdURI);

		// Criação das propriedades a partir do banco
		medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "nome"), med.getNome());
		medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "laboratorio"), med.getLaboratorio().getNome());
		
		// Cria bag de farmacos
		List<FarmacoBean> farms = med.getFarmacos();
		Bag farmBag = model.createBag();
		for (FarmacoBean farm : farms) {
			farmBag.add(model.createResource(bulasWebNS + farm.getNome()));
		}
		medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "farmaco"), farmBag);
		
		// Cria bag de remedios que interagem com ele
		List<MedicamentoBean> medInt = medDao.findInteracaoMedicamentosa(med.getNome());
		Bag interacaoBag = model.createBag();
		for (int i = 0; i < medInt.size(); i++) {
			interacaoBag.add(model.createResource(bulasWebNS + medInt.get(i).getNome()));
		}
		medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "interacao_medicamentosa"), interacaoBag);

		
		// Criação das propriedades a partir do csv
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
					String[] dados = linha.split(",");
					
					Property property = null;
					String propriedade = dados[0].replaceAll("\"", "");
					String object = dados[1].replaceAll("\"", "");
					object = object.substring(0, 1).toUpperCase() + object.substring(1).toLowerCase();
					
					//if (propriedade.matches("^.*(sintoma|doenca)$")) {

					String queryString = String.format(
							"SELECT ?a WHERE { ?a <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@pt . }", object);
						
						List <List<RDFNode>> res = this.searchSparql(queryString, model);
						if (res.size() > 0) object = res.get(0).get(0).toString();
					//}
					//else {//nome,farmaco
						
					//}

					
					if (propriedade.matches("^interage_com_nome")) {
						medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "interage_com"), object);
					}
					else if (propriedade.matches("^interage_com_farmaco")) {
						medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "interage_com"), object);
					}
					else if (propriedade.matches("^indicacao_para_.*")) {
						medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "indicado_para"), object);
					}
					else if (propriedade.matches("^contra_indicado_para_.*")) {
						medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "contra_indicado_para"), object);
					}
					else if (propriedade.matches("^provoca_reacao_.*")) {
						medicamentoRes.addProperty(new PropertyImpl(bulasWebNS, "provoca_reacao"), object);
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

}
