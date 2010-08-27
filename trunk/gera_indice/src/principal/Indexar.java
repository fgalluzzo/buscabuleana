package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import util.PersistenceFactory;
import bean.BulaBean;
import bean.ConteudoSecaoBean;
import bean.SecaoBulaBean;
import dao.BulaDao;
import dao.SecaoBulaDao;


public class Indexar {

	private IndexWriter indexWriter;
	private int corrupted = 0;
	private Map <Integer, Integer> secCount = new TreeMap<Integer, Integer>();	// K=num of sections, V=num of docs
	private String bulasFolder = null;
	private String indexFolder = null;
	
	public Indexar(String bulasFolder, String indexFolder) {
		this.bulasFolder = bulasFolder;
		this.indexFolder = indexFolder;

		for (int i = 0; i <= BulaParser.getProperties().keySet().size(); i++) {
			secCount.put(i, 0);
		}
	}

	
	public String extractTextFromPdf(File pdfFile) throws FileNotFoundException, IOException {
		String text = null;
		try {
			FileInputStream fi = new FileInputStream(pdfFile);
			PDFParser parser = new PDFParser(fi);
			parser.parse();

			COSDocument cd = parser.getDocument();
			PDDocument pd = new PDDocument(cd);
			
			PDFTextStripper stripper = new PDFTextStripper();
			text = stripper.getText(pd);
			
			pd.close();
			cd.close();
			fi.close();
			
			return text;
			
		} catch (FileNotFoundException e) {
			throw e;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			if (text == null || "".equals(text)) throw e;
			return text;
		}
	}
	
	public void extractPdfs() {

		File bulasf = new File(bulasFolder);
		File [] pdfs = bulasf.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*pdf");
			}
		});
		
		String txtDirectory = bulasFolder + "\\plain\\";
		File dir = new File(txtDirectory);
		dir.mkdir();

		System.out.println("Existem " + pdfs.length + " arquivos PDFs para extrair"); 

		int count = 1;
		int corrupted = 0;
		for (File pdf : pdfs) {

			try {
				System.out.println(String.format("Extracting from %s (%d / %d %d %%)",
						pdf.getName(), count, pdfs.length, 100 * count / pdfs.length));
				String text = extractTextFromPdf(pdf);

				// Escreve bula em texto plano (nao parseado)
				FileWriter fw;
				try {
					String txtFileName = pdf.getName() + ".txt";
					System.out.println("Writing " + txtFileName + "...");
					fw = new FileWriter(txtDirectory + txtFileName);
					fw.write(text);
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch (Exception e) {
				System.out.print("\nArquivo corrompido: " + pdf.getName());
				corrupted++;
				//pdf.delete();
			}
			
			count++;
		}

		System.err.println(String.format("Concluido. Taxa de corrompimento dos PDFs: %4.1f", 100*((float)corrupted) / count));
	}
	
	
	
	public Map <String, String> indexBula(File file) throws IOException {
		
		BulaParser bulaParser = new BulaParser();
		EntityManager em = PersistenceFactory.getEntityManager();
		SecaoBulaDao secaoBulaDao = new SecaoBulaDao(em);
		BulaDao bulaDao = new BulaDao(em);
		
		String code = file.getName().replaceAll("^([0-9]+)[.]txt$", "$1");
		
		BulaBean bula = bulaDao.getByCodigo(code);
		if (bula != null) return null;
		
		String text = getFileContents(file);

		// Obtem nome e texto das seçoes da bula
		Map <String, String> fields = bulaParser.findSections(text, bulasFolder + file.getName());
		
		// Cria documento do Lucene
		Document d = new Document();
		d.add(new Field("code", code, Field.Store.YES, Field.Index.NO));
		d.add(new Field("text", text, Field.Store.NO, Field.Index.ANALYZED));
		
		// Bula Bean
		bula = new BulaBean();
		bula.setTexto(text);
		bula.setCodigo(code);
		List<ConteudoSecaoBean> secoesBean = new ArrayList<ConteudoSecaoBean>();
		
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			
			for (String name : fields.keySet()) {
				
				// Pega Secao de bula 
				SecaoBulaBean secaoBulaBean = secaoBulaDao.findByShortName(name);
				if (secaoBulaBean == null) {
					secaoBulaBean = new SecaoBulaBean();
					secaoBulaBean.setNomeCurto(name);
					secaoBulaBean.setNome(name);
					em.persist(secaoBulaBean);
				}
				
				ConteudoSecaoBean secao = new ConteudoSecaoBean();
				secao.setBula(bula);
				secao.setTexto(fields.get(name));
				secao.setSecaoBula(secaoBulaBean);
				secoesBean.add(secao);

				d.add(new Field(name, fields.get(name), Field.Store.NO, Field.Index.ANALYZED));
			}
			
			bula.setConteudoSecao(secoesBean);
			em.persist(bula);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}
		
		indexWriter.addDocument(d);

		// Write INI to disk
//			FileWriter fw = new FileWriter(bulasFolder + "ini/" + pdf.getName() + ".ini");
//			for (String name : fields.keySet()) {
//				fw.write("\n[" + name + "]\n");
//				fw.write(fields.get(name));
//				fw.write("\n");
//			}
//			fw.close();

		return fields;
	}

	private String getFileContents(File file) throws FileNotFoundException {

		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line = null;
		StringBuilder sb = new StringBuilder((int) file.length() + 16); 
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
	}


	public void index(final String filePattern) {
		try {
			File bulasf = new File(bulasFolder + "/plain/");
			File indexf = new File(indexFolder);
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();

			indexWriter = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);

			File[] txtFiles = bulasf.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*txt");
				}
			});

			
			System.out.println("Existem " + txtFiles.length + " arquivos de texto de bula para analisar"); 

			int i = 1;
			for (File file : txtFiles) {
				System.out.print("Indexando doc " + (i) + " de " + txtFiles.length + ": " + file.getName() + " ... ");

				Map<String, String> fields = indexBula(file);
				if (fields != null) {
//					if (fields.size() < 7) System.err.println(pdf.getName() + "  " + fields.size());
					secCount.put(fields.size(), secCount.get(fields.size()) + 1);
//
//					System.out.print(String.format(" - %d secoes (%4.1f%% concluido)", fields.size(), 100.0 * i / pdfs.length));
				}
//				else
//					System.out.print(String.format(" - %d de %d (%4.1f%% corrompido)", corrupted, i, 100.0*corrupted / i));

				i++;
				//if (i > 20) break;
				System.out.println("");
			}
			indexWriter.optimize();
			indexWriter.close();

			System.err.println(String.format("Taxa de corrompimento dos PDFs: %4.1f", 100*((float)corrupted) / i));
			System.err.println(String.format("Quantidade de secoes e suas respectivas quantidades de bulas:"));
			System.err.println("secoes\tbulas");
			for (Integer key : secCount.keySet()) {
				System.err.println(String.format("%d\t%d", key, secCount.get(key)));
			}


			System.out.println("Concluido. Indice criado e bulas persistidas!");			
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
	}
}
