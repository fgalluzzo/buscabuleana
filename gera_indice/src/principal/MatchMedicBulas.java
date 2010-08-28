package principal;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import util.PersistenceFactory;
import dao.BulaDao;
import dao.FarmacoDao;
import dao.LaboratorioDao;
import dao.MedicamentoDao;

import bean.BulaBean;
import bean.FarmacoBean;
import bean.LaboratorioBean;
import bean.MedicamentoBean;

public class MatchMedicBulas {
	
	
	public static class MedicamentoComparator implements Comparator<MedicamentoBean> {
		@Override
		public int compare(MedicamentoBean a, MedicamentoBean b) {
			return a.getNome().compareTo(b.getNome());
		}
	}
	
	public static String analyzeText(String text) {
		
		if (text == null) {
			System.out.println(text);
		}
		 
		text = Normalizer.normalize(text, Form.NFD);
		text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		text = text.toLowerCase();
		
		text = text.replaceAll("[\\p{Punct}	\\r\\n\\t]+", " ");
		text = text.replaceAll("[0-9]+[^\\-]", " ");
		//text = text.replaceAll("([^a-z]mg[^a-z]|[^a-z]ml[^a-z])", " ");

		//System.out.println(text);
		return text;
	}
	

	private EntityManager em;
	private LaboratorioDao labDao;
	private MedicamentoDao medDao;
	private FarmacoDao farmDao;
	private BulaDao bulaDao;
	private Collection<Integer> bulas;
	private Collection<FarmacoBean> farmacos;
	private Collection<LaboratorioBean> labs;
	
	public String extractSectionContents(BulaBean bula, String [] sections) {
		
		String result = "";
		
		for (String section : sections) {
			String s = bula.getSectionContentsByName(section);
			if (s != null) result += s;
		}

		if (result.equals("")) return null;
		else return result;
	}
	
	
	public MatchMedicBulas() {
		em = PersistenceFactory.createEntityManager();
		labDao = new LaboratorioDao(em);
		medDao = new MedicamentoDao(em);
		farmDao = new FarmacoDao(em);
		bulaDao = new BulaDao(em);

		bulas = bulaDao.getAllAsIntList();
		farmacos = farmDao.findAll(FarmacoBean.class);
		labs = labDao.findAll(LaboratorioBean.class);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
		em.close();
	}
	
	public void process() {

		int done = 0, pending = 0, alreadyDone = 0, failed = 0;

		for (Integer bulaId : bulas) {

			try {
				BulaBean bula = bulaDao.findById(BulaBean.class, bulaId);
				if (bula.getMedicamento() == null) {

					String farmacosText = extractSectionContents(bula, new String[] {"composicao", "formaFarmaceutica"} );
					String fulltext = bula.getTexto();

					if (farmacosText != null) {
						farmacosText = analyzeText(farmacosText);
						fulltext = analyzeText(fulltext);

						StringBuilder sb = new StringBuilder();
						sb.append(bula.getCodigo() + "\t");

						// Descobre laboratorio
						List <String> neededLabs = new ArrayList<String>();
						for (LaboratorioBean lab : labs) {
							String nomeLab = analyzeText(lab.getNome());

							if (fulltext.matches(".* " + nomeLab + " .*")) {
								neededLabs.add(nomeLab);
								sb.append(String.format("%s, ", nomeLab));
							}
						}
						sb.append("\t");

						// Descobre farmacos
						List <String> neededFarms = new ArrayList<String>();
						for (FarmacoBean farmaco : farmacos) {
							String nomeFarmaco = analyzeText(farmaco.getNome());

							if (farmacosText.matches(".* " + nomeFarmaco + " .*")) {
								neededFarms.add(nomeFarmaco);
								sb.append(String.format("%s, ", nomeFarmaco));
							}
						}

						// Descobre nome
						List<MedicamentoBean> medicamentos = medDao.findByFarmacoAndLab(neededLabs, neededFarms);
						Set <MedicamentoBean> candidateMeds = new TreeSet<MedicamentoBean>(new MedicamentoComparator());

						for (MedicamentoBean med : medicamentos) {
							String nomeMed = analyzeText(med.getNome());
	
							if (fulltext.matches(".* " + nomeMed + "®? .*")) {
								candidateMeds.add(med);
							}
							for (MedicamentoBean m : candidateMeds)
								sb.append(String.format("%s, ", m.getNome().toUpperCase()));
						}
	
						sb.append("\n");
	
						if (candidateMeds.size() > 0) {
							System.out.println(sb.toString());
							if (candidateMeds.size() == 1) {
								
								// Associa bula ao medicamento no BD
								MedicamentoBean m = candidateMeds.iterator().next();
								EntityTransaction tx = em.getTransaction();
								tx.begin();
								bula.setMedicamento(m);
								m.getBulas().add(bula);
								tx.commit();
								
								// contagem
								done++;								
	
								// TODO remover
								System.out.println((done+pending+alreadyDone+failed) + " / " + bulas.size());
								System.out.println(String.format("done=%d   pending=%d   alreadyDone=%d   failed=%d", done,pending,alreadyDone,failed));
							}
							else {
								// TODO tratar caso em que a bula fala de mais de um remedio
								
								// contagem
								pending++;
							}
						}
						else failed++;
					}
					else {
						//System.out.println("Campo 'composicao' não encontrado: id=" + bulaId + "   cod=" + bula.getCodigo());
						
						failed++;
					}
	
					System.out.flush();
				}
				else alreadyDone++;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println((done+pending+alreadyDone+failed) + " / " + bulas.size());
				System.out.println(String.format("done=%d   pending=%d   alreadyDone=%d   failed=%d", done,pending,alreadyDone,failed));
			}
		}

		System.out.println((done+pending+alreadyDone+failed) + " / " + bulas.size());
		System.out.println(String.format("done=%d   pending=%d   alreadyDone=%d   failed=%d", done,pending,alreadyDone,failed));
	}
}
