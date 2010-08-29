package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

import util.PersistenceFactory;
import bean.BulaBean;
import bean.ConteudoSecaoBean;
import bean.SecaoBulaBean;
import dao.BulaDao;
import dao.SecaoBulaDao;

public class Indexar {

	private String bulasFolder = null;
	private String indexFolder = null;

	// K=num of sections, V=num of docs
	private Map<Integer, Integer> secCount = new TreeMap<Integer, Integer>();

	private EntityManager em;
	private SecaoBulaDao secaoBulaDao;
	private BulaDao bulaDao;

	public Indexar(String bulasFolder, String indexFolder) {
		this.bulasFolder = bulasFolder;
		this.indexFolder = indexFolder;

		// JPA: EM e DAOs
		em = PersistenceFactory.createEntityManager();
		secaoBulaDao = new SecaoBulaDao(em);
		bulaDao = new BulaDao(em);

		for (int i = 0; i <= BulaParser.getProperties().keySet().size(); i++) {
			secCount.put(i, 0);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();

		em.close();
	}

	private String getFileContents(File file) throws FileNotFoundException {

		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = null;
		StringBuilder sb = new StringBuilder((int) file.length() + 16);
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

	private Map<String, String> persisteBula(File file) throws IOException {

		// Obtem bula a partir do codigo
		String code = file.getName().replaceAll("^([0-9]+)[.]pdf[.]txt$", "$1");
		BulaBean bula = bulaDao.getByCodigo(code);
		if (bula != null) return null;	// util para quando esta continuando um processo interrompido

		String text = getFileContents(file);

		// Obtem nome e texto das seçoes da bula
		BulaParser bulaParser = new BulaParser();
		Map<String, String> fields = bulaParser.findSections(text, bulasFolder + file.getName());

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
				SecaoBulaBean secaoBulaBean = secaoBulaDao
						.findByShortName(name);
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
			}

			bula.setConteudoSecao(secoesBean);
			em.persist(bula);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}

		return fields;
	}

	
	public void indexaBulas() {
		
		try {
			File indexf = new File(indexFolder);
			Directory dir = new SimpleFSDirectory(indexf);

			SimpleAnalyzer analyzer = new SimpleAnalyzer();
			IndexWriter indexWriter = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);


			Collection<Integer> ids = bulaDao.getAllAsIntList();
			for (int id: ids) {

				BulaBean bula = bulaDao.findById(BulaBean.class, id);
				if (bula != null) {			
					String code = bula.getCodigo();
					String text = bula.getTexto();
					
					// Cria documento do Lucene
					Document d = new Document();
					d.add(new Field("code", code, Field.Store.YES, Field.Index.NO));// id
					d.add(new Field("text", text, Field.Store.NO, Field.Index.ANALYZED));// name
					
					for (ConteudoSecaoBean section : bula.getConteudoSecao()) {
						d.add(new Field(section.getSecaoBula().getNomeCurto(), section.getTexto(),
								Field.Store.NO, Field.Index.ANALYZED));
					}
			
					indexWriter.addDocument(d);
				}
			}

			indexWriter.optimize();
			indexWriter.close();
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
	
	
	/**
	 * 
	 */
	public void persisteBulas() {
		try {
			// Obtem lista dos arquivos texto
			File bulasf = new File(bulasFolder + "/plain/");
			File[] txtFiles = bulasf.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*txt");
				}
			});

			System.out.println("Existem " + txtFiles.length
					+ " arquivos de texto de bula para analisar");

			int count = 1;
			for (File file : txtFiles) {
				System.out.print("Analisando doc " + (count) + " de "
						+ txtFiles.length + ": " + file.getName() + " ... ");

				Map<String, String> fields = persisteBula(file);
				if (fields != null) {
					secCount.put(fields.size(), secCount.get(fields.size()) + 1);
					System.out.print(String.format(
							"%d secoes (%4.1f%% concluido)", fields.size(),
							100.0 * count / txtFiles.length));
				} else {
					System.out.print("já persistido anteriormente.");
				}

				count++;
				System.out.println("");
			}

			System.out.println("Concluido. Indice criado e bulas persistidas!\n");

			System.out.println("Quantidade de secoes e suas respectivas quantidades de bulas:");
			System.out.println("secoes\tbulas");
			for (Integer key : secCount.keySet()) {
				System.out.println(String.format("%d\t%d", key, secCount.get(key)));
			}

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
