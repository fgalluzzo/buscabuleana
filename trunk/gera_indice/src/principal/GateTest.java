package principal;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.DocumentContent;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import util.PersistenceFactory;
import bean.BulaBean;
import bean.ConteudoSecaoBean;
import dao.BulaDao;

public class GateTest {
	
	public static void bla(
			gate.Document doc, 
			java.util.Map<java.lang.String, gate.AnnotationSet> bindings, 
			gate.AnnotationSet annotations 
  ) {
		

	
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		//buildXml();
		//runApp();
		
		GateTest gt = new GateTest();
		gt.readCsv();

	}
	
	public static void runApp() {
		try {
			Gate.init();

			String applicationName = "D:\\home\\expedit\\PPGI\\2010.2\\OC\\TrabFinal\\bulas-pos-transf-jape.xgapp";
			CorpusController controller = (CorpusController) PersistenceManager.loadObjectFromFile(new File(applicationName));
			
			Corpus corpus = controller.getCorpus();
			List<String> docs = corpus.getDocumentNames();
			int i = 0;
			for (String name : docs) {
				System.out.println(name + " " + corpus.isDocumentLoaded(i));
				//Document dec = (Document) corpus.get(i);
				//System.out.println(dec.getContent().toString());
				
				i++;
			}
			
			Collection<ProcessingResource> prList = (Collection<ProcessingResource>) controller.getPRs();
			for (ProcessingResource pr : prList) {
				//pr.
				System.out.println(pr.getName());
			}
			controller.execute();
			
			System.out.println(controller.toString());
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void buildXml() {
		EntityManager em = PersistenceFactory.createEntityManager();
		BulaDao bulaDao = new BulaDao(em);
		
		File folder = new File("../bulas_teste/bulas/xml/gate/");
		
		Collection<Integer> bulas = bulaDao.getByLengthAndSectionsAsIntList(2000, 9);
		
		for (Integer bulaId : bulas) {
			BulaBean bula = bulaDao.findById(BulaBean.class, bulaId);
			
			try {
				//FileWriter fw = new FileWriter(new File(folder, bula.getCodigo() + ".xml"));
				
				// usa charset ISO-8859-1
				FileOutputStream fos = new FileOutputStream(new File(folder, bula.getCodigo() + ".xml"));
				OutputStreamWriter fw = new OutputStreamWriter(fos, Charset.forName("ISO-8859-1"));
				///
				
				fw.write("<bula>\n");

				List<ConteudoSecaoBean> sections = bula.getConteudoSecao();
				for (ConteudoSecaoBean section : sections) {
					String text = section.getTexto();
					text = text.replaceAll("\"", "&quot;");
					text = text.replaceAll("&", "&lt;");
					text = text.replaceAll("<", "&gt;");
					text = text.replaceAll(">", "&amp;");
					
					fw.write("<" + section.getSecaoBula().getNomeCurto() + ">");
					fw.write(text);
					fw.write("</" + section.getSecaoBula().getNomeCurto() + ">\n");
				}
				
				fw.write("</bula>\n");
				fw.close();
			}
			catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
		System.out.println(bulas.size());

		em.close();
		PersistenceFactory.getEntityManagerFactory().close();
	}

	
	public void readCsv() {
		
		File f = new File("D:\\home\\expedit\\PPGI\\2010.2\\OC\\gate6b1");
		File [] csvs = f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*csv");
			}
		});
		for (File file : csvs) {
			System.out.println(file.getName());
			this.process(file);
		}
		
	}
	
	public void process(File file) {
		
		CsvReader csvReader = new CsvReader(file);
		csvReader.setListener(new CsvListener(file));
		try {
			csvReader.process();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	class CsvListener implements CsvRowListener {
		
		private File file;
		
		
		public CsvListener(File file) {
			super();
			this.file = file;
		}

		@Override
		public void doRowProcessing(String[] values) {
			StringBuilder sb = new StringBuilder();
			sb.append(file.getName().subSequence(0,6));
			
			sb.append("\t<");
			sb.append(values[0]);
			sb.append(">\t");

			sb.append(values[1]);

			System.out.println(sb.toString());
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

}
