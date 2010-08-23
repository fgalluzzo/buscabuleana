package principal;

import java.io.File;
import java.io.FileNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import bulas.bean.LaboratorioBean;
import bulas.bean.MedicamentoBean;
import bulas.dao.LaboratorioDao;
import bulas.dao.MedicamentoDao;

public class PopularBanco implements CsvRowListener {
	
	private EntityManager em;
	private boolean firstLine = true;
	private LaboratorioDao labDao;
	private MedicamentoDao medDao;
	
	public PopularBanco(EntityManager em) {
		super();
		this.em = em;
		labDao = new LaboratorioDao(em);
		medDao = new MedicamentoDao(em);
	}
	
	public void processa(File file) {
		
		CsvReader csvReader = new CsvReader(file);
		csvReader.setListener(this);
		try {
			csvReader.process();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 
	Aciclovir;glaxosmithkline;ZOVIRAX;0,03g/g;pomd oft
	Aciclovir;glaxosmithkline;ZOVIRAX;200mg;comp
	Aciclovir;glaxosmithkline;ZOVIRAX;250mg;po liof solç inj
	Aciclovir;glaxosmithkline;ZOVIRAX;50mg/g;crem derm

	 */
	@Override
	public void doRowProcessing(String[] values) {
				
		if (!firstLine) {
			String farmaco = values[0];
			String labName = values[1];
			String nomeMed = values[2];
			String concentracao = values[3];
			String forma = values[4];

			// Procura por lab e medicamento existes
			MedicamentoBean md = medDao.findByName(nomeMed);
			LaboratorioBean laboratorio = labDao.findByName(labName);

			if (md == null) {
				md = new MedicamentoBean();
				md.setNome(nomeMed);

				if (laboratorio == null) {
					laboratorio = new LaboratorioBean();
					laboratorio.setNome(labName);
				}
				md.setLaboratorio(laboratorio);

				System.out.println("Farmaco: " + values[0]);

				EntityTransaction tx = em.getTransaction();
				tx.begin();
				em.persist(laboratorio);
				em.persist(md);
				tx.commit();
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
	
	
	public static void main(String[] args) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("bulas_base");
		EntityManager em = emf.createEntityManager();

		PopularBanco pb = new PopularBanco(em);
		pb.processa(new File("../bulas_teste/anvisa/medicamentos.csv"));
		
		em.clear();
		emf.close();
	}

}
