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
import bean.MedicamentoBean;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.tdb.TDBFactory;

import controle.InteracaoControle;
import controle.RdfControle;
import dao.MedicamentoDao;

public class TdbGenerator {
	

	public static void main(String[] args) throws FileNotFoundException {
		
		// Exemplo de consulta TDB
		String queryString = JOptionPane.showInputDialog("SPARQL:");
		new TdbGenerator("../bulasweb1_2/web/tdb").searchSparql(queryString);
		
		
		// Para gerar indice TDB
		//TdbGenerator tdbGen = new TdbGenerator("../bulasweb1_2/web/tdb");
		//tdbGen.geraTuplasDbPedia();
		//tdbGen.geraTuplasBulasWeb();
	}
	
	

	private MedicamentoDao medDao;
	private EntityManager em;
	private String directory;


	public TdbGenerator(String directory) {
		this.directory = directory;
	}
	
	
	/**
	 * Exemplo de consulta com TDB
	 * 
	 * @param queryString
	 */
	public void searchSparql(String queryString) {

		Model model = TDBFactory.createModel(directory);

		// Create a new query
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		for (; results.hasNext(); ) {
			
			QuerySolution sol = results.next();
			
			Iterator<String> it = sol.varNames();
			for (; it.hasNext() ; ) {
				RDFNode node = sol.get(it.next());
				String text = node.toString();
				text = text.replaceAll("\"", "&quot;");
				text = text.replaceAll("&", "&lt;");
				text = text.replaceAll("<", "&gt;");
				text = text.replaceAll(">", "&amp;");
				
				System.out.print(node.toString()+"\t");

			}
			System.out.println("");
		}

		// Important � free up resources used running the query
		qe.close();
	}
	
	
	
	/**
	 * Insere N-Triplas labels_pt da DBPedia no indice de bulas
	 * 
	 * TODO Filtrar para inserir apenas tuplas relevantes (Farmacos, Sintomas, Doen�as, etc).
	 *  
	 * @throws FileNotFoundException
	 */
	public void geraTuplasDbPedia() throws FileNotFoundException {
		
		Model model = TDBFactory.createModel(directory);
		
		InputStream in = new FileInputStream(new File("../bulasweb1_2/web/ntriple/labels_pt.nt"));
		model.read(in,null,"N-TRIPLE"); // null base URI, since model URIs are absolute
		model.commit();
		
		model.close();
	}

	public void geraTuplasBulasWeb() {
		em = PersistenceFactory.createEntityManager();
		medDao = new MedicamentoDao(em);

		
		Model model = TDBFactory.createModel(directory);

		String bulasWebNS = "http://bulasweb.test/rdf#";
		model.setNsPrefix("bw", bulasWebNS);

		Collection<MedicamentoBean> meds = medDao.findAll(MedicamentoBean.class);
		for (MedicamentoBean med : meds) {

			geraNtriple(med, model, bulasWebNS);
			model.commit();
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
		
		String dir = "../bulasweb1_2/web/";

		String mdURI = bulasWebNS + med.getNome();

		// Criação do recurso medicamento
		Resource medicamento = model.createResource(mdURI);

		// Criação das propriedades a partir do banco
		medicamento.addProperty(new PropertyImpl(bulasWebNS, "nome"), med.getNome());
		medicamento.addProperty(new PropertyImpl(bulasWebNS, "laboratorio"), med.getLaboratorio().getNome());
		List<MedicamentoBean> medInt = medDao.findInteracaoMedicamentosa(med.getNome());
		Bag InteracaoMedic = model.createBag();
		for (int i = 0; i < medInt.size(); i++) {
			InteracaoMedic.add(model.createResource(bulasWebNS
					+ medInt.get(i).getNome()));
		}

		medicamento.addProperty(new PropertyImpl(bulasWebNS, "interacao_medicamentosa"), InteracaoMedic);

		// Criação das propriedades a partir do csv
		File dirGate = new File(dir + "gate");
		if (med.getBulas() != null && med.getBulas().size() > 0) {
			String bula = med.getBulas().get(0).getCodigo();
			File csv = new File(dirGate + "/" + bula + ".csv");
			System.out.print(csv.getName());
			FileReader fr;
			try {
				fr = new FileReader(csv);
				BufferedReader in = new BufferedReader(fr);
				while (in.ready()) {
					String linha = in.readLine();
					String[] dados = linha.split(",");
					medicamento.addProperty(
							new PropertyImpl(
									bulasWebNS, dados[0].replaceAll("\"", "")),
							dados[1].replaceAll("\"", "")
					);
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
