package principal;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import util.PersistenceFactory;
import bean.BulaBean;
import bean.FarmacoBean;
import bean.LaboratorioBean;
import bean.MedicamentoBean;
import dao.BulaDao;
import dao.FarmacoDao;
import dao.LaboratorioDao;
import dao.MedicamentoDao;

public class Princ {
	
	public static String analyzeText(String text) {
		 
		text = Normalizer.normalize(text, Form.NFD);
		text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		text = text.toLowerCase();
		
		text = text.replaceAll("[\\p{Punct}	\\r\\n\\t]+", " ");
		text = text.replaceAll("[0-9]+[^\\-]", " ");
		//text = text.replaceAll("([^a-z]mg[^a-z]|[^a-z]ml[^a-z])", " ");

		//System.out.println(text);
		return text;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EntityManager em = PersistenceFactory.getEntityManager();
		LaboratorioDao labDao = new LaboratorioDao(em);
		MedicamentoDao medDao = new MedicamentoDao(em);
		FarmacoDao farmDao = new FarmacoDao(em);
		BulaDao bulaDao = new BulaDao(em);

		Collection<Integer> bulas = bulaDao.getAllAsIntList();
		Collection<FarmacoBean> farmacos = farmDao.findAll(FarmacoBean.class);
		Collection<LaboratorioBean> labs = labDao.findAll(LaboratorioBean.class);
		
		int done = 0, pending = 0, alreadyDone = 0, failed = 0;
		
		for (Integer bulaId : bulas) {

			BulaBean bula = bulaDao.findById(BulaBean.class, bulaId);
			if (bula.getMedicamento() == null) {
			
				String composicao = bula.getSectionContentsByRegex("composicao");
				String laboratorio = bula.getSectionContentsByRegex("rodape");
				String fulltext = bula.getTexto();
				
				StringBuilder sb = new StringBuilder();

				if (composicao != null && laboratorio != null) {
					composicao = analyzeText(composicao);
					laboratorio = analyzeText(laboratorio);
					fulltext = analyzeText(fulltext);

					sb.append(bula.getCodigo() + "\t");

					// Descobre laboratorio
					List <String> neededLabs = new ArrayList<String>();
					for (LaboratorioBean lab : labs) {
						String nomeLab = analyzeText(lab.getNome());

						if (laboratorio.matches(".* " + nomeLab + " .*")) {
							neededLabs.add(nomeLab);
							sb.append(String.format("%s, ", nomeLab));
						}
					}
					sb.append("\t");
					
					// Descobre farmacos
					List <String> neededFarms = new ArrayList<String>();
					for (FarmacoBean farmaco : farmacos) {
						String nomeFarmaco = analyzeText(farmaco.getNome());
						
						if (composicao.matches(".* " + nomeFarmaco + " .*")) {
							neededFarms.add(nomeFarmaco);
							sb.append(String.format("%s, ", nomeFarmaco));
						}
					}

					// Descobre nome
					List<MedicamentoBean> medicamentos = medDao.findByFarmacoAndLab(neededLabs, neededFarms);

					Set <MedicamentoBean> candidateMeds = new TreeSet<MedicamentoBean>(
							new Comparator<MedicamentoBean>() {
								@Override
								public int compare(MedicamentoBean a, MedicamentoBean b) {
									return a.getNome().compareTo(b.getNome());
								}
					});

					for (MedicamentoBean med : medicamentos) {
						String nomeMed = analyzeText(med.getNome());

						if (fulltext.matches(".* " + nomeMed + " .*")) {
							candidateMeds.add(med);
							sb.append(String.format("%s, ", nomeMed.toUpperCase()));
						}
					}

					sb.append("\n");

					if (candidateMeds.size() > 0) {
						System.out.println(sb.toString());
						if (candidateMeds.size() == 1) {
							done++;

							// TODO colocar no BD

							// TODO remover
							System.out.println((done+pending+alreadyDone+failed) + " / " + bulas.size());
							System.out.println(String.format("done=%d   pending=%d   alreadyDone=%d   failed=%d", done,pending,alreadyDone,failed));
						}
						else pending++;
					}
					else failed++;
				}
				else failed++;

				System.out.flush();
			}
			else alreadyDone++;
		}

		System.out.println((done+pending+alreadyDone+failed) + " / " + bulas.size());
		System.out.println(String.format("done=%d   pending=%d   alreadyDone=%d   failed=%d", done,pending,alreadyDone,failed));

		em.close();
	}



	public static void search() {

		try {
			File indexf = new File("../indice_bulas");
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();

			IndexSearcher isearcher = new IndexSearcher(dir, true); // read-only=true
			// Parse a simple query that searches for "text":
			QueryParser parser = new QueryParser(Version.LUCENE_30, "aviso", analyzer);
			Query query = parser.parse("idosos");
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;

			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
			  Document hitDoc = isearcher.doc(hits[i].doc);
			  System.out.println(hitDoc.get("id") + ".txt\t" + hits[i].score );
			}
			isearcher.close();
			dir.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
