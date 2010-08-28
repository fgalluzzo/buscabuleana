package principal;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import util.PersistenceFactory;
import bean.FarmacoBean;
import bean.LaboratorioBean;
import bean.MedicamentoBean;
import dao.FarmacoDao;
import dao.LaboratorioDao;
import dao.MedicamentoDao;

public class PersistAnvisaList implements CsvRowListener {
	
	private EntityManager em;
	private boolean firstLine = true;
	private LaboratorioDao labDao;
	private MedicamentoDao medDao;
	private FarmacoDao frmDao;
	
	public PersistAnvisaList() {
		super();
		this.em = PersistenceFactory.createEntityManager();
		labDao = new LaboratorioDao(em);
		medDao = new MedicamentoDao(em);
		frmDao = new FarmacoDao(em);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
		em.close();
	}
	
	
	public void process(File file) {
		
		CsvReader csvReader = new CsvReader(file);
		csvReader.setListener(this);
		try {
			csvReader.process();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * Normaliza nome de farmaco para evitar tuplas duplicadas.
	 * @param farmaco
	 * @return
	 * @throws Exception
	 */
	public String normalizaNomeFarmaco(String farmaco) throws Exception {

		// Cria nova string com nome do fármaco normalizado e sem acentos 
		String farmaco2 = Normalizer.normalize(farmaco, Form.NFD);
		farmaco2 = farmaco2.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		
		if (farmaco.length() != farmaco2.length())
			throw new Exception("incorrect diacritial mark removal: " + farmaco + " vs. " + farmaco2);
	
		// Trata formato da string do farmaco
		String suffixes = "[ae]to|[ai][cd][ao]|exo|possomal|ina|giro";
		
		if (farmaco2.toLowerCase().matches("((dimeticona|simeticona)[\\p{Punct} ]+){2}")) {
			farmaco = "Dimeticona/Simeticona";
		}
		else if (farmaco2.toLowerCase().equals("agar-agar")) {
			farmaco = "Ágar-Ágar";
		}
		else if ("tetracis (2-metoxi-isobutil-isonitrila)".equals(farmaco2.toLowerCase())) {
			farmaco = "Tetracis (2-metoxi-isobutil-isonitrila)";
		}
		else if (farmaco2.toLowerCase().matches("^([a-z0-9]+-)?[0-9a-z]+, [0-9a-z]+")) {	// Acetilsalicílico, ácido => Ácido Acetilsalicílico
			
			farmaco = farmaco.replaceAll("^(.+), (.+)", "$2 $1");
		}
		else if (farmaco2.toLowerCase().matches("^([a-z0-9]-)?[0-9a-z ]+ \\(([0-9a-z ]+(" + suffixes + "))+\\)")) {	// Amiodarona (cloridrato) => Cloridrato de Amiodarona 

			Matcher m = Pattern.compile("^(.+) \\((.+)\\)").matcher(farmaco);
			if (m.find()) farmaco = m.group(2) + " de " + m.group(1);	// apenas um modo diferente do anterior
		}
		else if (farmaco2.toLowerCase().matches("([a-z0-9]+-)?[0-9a-z /]+[.]?")) {
			// ok, do nothing...
		}
		else {
			throw new Exception("unexpected farmaco pattern: " + farmaco);
		}

		// Monta nome de farmaco padronizado (cloridrato de ..., ácido ..., etc)
		String [] words = farmaco.split("\\s");
		farmaco = "";
		for (String s : words) {
			if (s.length() > 2) farmaco += s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + " ";
			else farmaco += s.toLowerCase() + " ";
		}
		
		return farmaco.trim();
	}

	@Override
	public void doRowProcessing(String[] values) {

		if (!firstLine) {
			try {
				String farmacos[] = values[0].split(" *[&\\+] *");
				String labName = values[1];
				String nomeMed = values[2].toUpperCase();
				String concentracao = values[3];
				String forma = values[4];

				// Obtem lista de nomes dos farmacos normalizados
				Set <String> farmacoSet = new TreeSet<String>();	// sem duplicatas
				List <String> farmacoList = new ArrayList<String>();	// permite duplicatas
				for (String farmaco : farmacos) {
					farmaco = normalizaNomeFarmaco(farmaco);
					farmacoSet.add(farmaco);
					farmacoList.add(farmaco);
				}

				// Gera string com associacao de farmacos (permite duplicatas)
				Iterator<String> it = farmacoList.iterator();
				StringBuilder assocTextBuf = new StringBuilder();
				assocTextBuf.append(it.next());
				while (it.hasNext()) {
					assocTextBuf.append(" + ");
					assocTextBuf.append(it.next());
				}
				String assocText = assocTextBuf.toString();


				// Obtem beans dos farmacos (sem duplicatas)
				List<FarmacoBean> farmacoBeanList = new ArrayList<FarmacoBean>();
				for (String farmaco : farmacoSet) {		
					FarmacoBean f = frmDao.findByName(farmaco);
					if (f == null) {
						f = new FarmacoBean();
						f.setNome(farmaco);
					}
					farmacoBeanList.add(f);
				}


				// Procura medicamento
				MedicamentoBean md = medDao.findByNameAssocExactly(nomeMed, assocText);
				if (md == null) {

					// Apenas informa se ja existe medicamento com mesmo nome
					List<MedicamentoBean> mds = medDao.findByName(nomeMed);
					if (mds != null && mds.size() > 0)
						System.out.println("Nome Duplicado: " + nomeMed);

					// Procure laboratorio
					LaboratorioBean laboratorio = labDao.findByName(labName);
					if (laboratorio == null) {
						laboratorio = new LaboratorioBean();
						laboratorio.setNome(labName);
					}

					// Inicializa bean de medicamento
					md = new MedicamentoBean();
					md.setNome(nomeMed);
					md.setAssociacao(assocText);
					md.setFarmacos(farmacoBeanList);
					md.setLaboratorio(laboratorio);

					// Imprime farmacos
					System.out.println(nomeMed + ": " + assocText);
					
					// Persiste
					EntityTransaction tx = em.getTransaction();
					tx.begin();
					em.persist(laboratorio);
					em.persist(md);
					tx.commit();
				}
				else {
					// TODO Tratar concentracao e forma
					System.out.println("Medicamento Igual: " + md.getNome() + " --> " + md.getAssociacao());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}

		firstLine = false;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

}
