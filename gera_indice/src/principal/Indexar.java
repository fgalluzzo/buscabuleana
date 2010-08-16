package principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


public class Indexar {
	
	private Properties properties = loadProperties();
	private IndexWriter indexWriter;
	private int corrupted = 0;
	private Map <Integer, Integer> secCount = new TreeMap<Integer, Integer>();	// K=num of sections, V=num of docs
	private String outputFileName = "../bulas_teste/bulas/failed.txt";
	private String bulasFolder = "../bulas_teste/bulas/";
		
	
	public static Properties loadProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("./src/principal/sections.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return properties;
	}
	
	public Indexar() {
		for (int i = 0; i <= properties.keySet().size(); i++) {
			secCount.put(i, 0);
		}
	}
		
	public Map <String, String> findSections(String text, String filename) {
		
		// \x29 = fecha parentesis
		text = text.replaceAll("[\\u2022\\u2018\\u2019\\u201B\\u201C\\u201D\\u201E\\u201F\\xB0\\xBA\"\']", "");	// remove aspas, ∞, bullet,
		text = text.replaceAll("(?m)[\r\n]+(?-m)", "\n");	// padronizar quebras de linhas
		text = text.replaceAll("[ \t]+", " ");	// juntar espaÁos e tabulaÁoes
		text = text.replaceAll("(?m)^[ \t]+(?-m)", "");	// remover espacos no inicio de linha
		text = text.replaceAll("([a-zA-Z]{2})- ?\r?\n", "$1");	// remove separacao de silabas
		text = text.replaceAll("([^.: ]) ?\r?\n", "$1 ");	// remover quebras de linhas na mesma frase
		text = text.replaceAll("([a-zA-Z0-9]+[\\x29]?[.]) ([A-Z][a-zA-Z0-9]+)", "$1\n$2");	// add quebras de linhas em frase distintas
		text = text.replaceAll("([a-z„·‚ÈÍÌıÛÙ˙Á]+[.\\x29]*) ([A-Z√¡¬… Õ”’‘⁄«]+[ :\\-])", "$1\n$2");//add \n em palavras minuscula seguida por maiuscula
		text = text.replaceAll("(?m)^[ \t]+(?-m)", "");	// remover espacos no inicio de linha (denovo mesmo)
		text = text.replaceAll("(?md)^([0-9]+|[ivxIVX]+)[\\x29.-]? +(?-md)", "");	// remove possiveis numeracoes em seÁoes


		TreeMap <Integer, String> map = new TreeMap<Integer, String>();

		// Escreve bula em texto plano (nao parseado)
		FileWriter fw;
		try {
			fw = new FileWriter(filename + ".txt");
			fw.write(text);
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//System.out.println(filename);

		// Encontra posiÁao(oes) de cada seÁ„o na bula
		for (Object section : properties.keySet()) {
			String sectionName = (String) section;

			String regex = properties.getProperty(sectionName);
						
			Matcher m = Pattern.compile(regex, Pattern.MULTILINE).matcher(text.toLowerCase());
			while (m.find()) {
				map.put(m.start(), sectionName);
			}
		}
		
		
		Map<String, String> sections = new HashMap<String, String> ();
		try {
			fw = new FileWriter(filename + ".xml");
			fw.write("<bula>\n");

			int last = 0;
			String curTag = null;
			for (Integer start : map.keySet()) {
				String subs = (String) text.subSequence(last, start);
				fw.write(subs);
				
				if (curTag != null) {
					String s = sections.get(curTag);
					if (s == null) s = "";
					sections.put(curTag, s + "\n" + subs);
				}
				
				String newTag = map.get(start);
				if (!newTag.equals(curTag)) {
					if (curTag != null) fw.write("</" + curTag + ">\n");
					fw.write("\n<" + newTag + ">");
				}
				curTag = newTag;
				
				last = start;
			}
			fw.write(text.substring(last));
			if (curTag != null) fw.write("</" + curTag + ">\n");
			
			fw.write("</bula>\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sections;
	}
	
	public Map <String, String> indexFile(File file) throws IOException {
		File pdf = file;
		FileInputStream fi = new FileInputStream(pdf);
		PDFParser parser = new PDFParser(fi);
		try {
			parser.parse();

			COSDocument cd = parser.getDocument();
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(new PDDocument(cd));
			cd.close();
			
			// Obtem nome e texto das seÁoes da bula
			Map <String, String> fields = findSections(text, bulasFolder + pdf.getName());
			
			// Cria documento do Lucene
			Document d = new Document();
			d.add(new Field("id", pdf.getName(), Field.Store.YES, Field.Index.NO));
			d.add(new Field("name", text, Field.Store.NO, Field.Index.ANALYZED));

			FileWriter fw = new FileWriter(bulasFolder + "ini/" + pdf.getName() + ".ini");
			for (String name : fields.keySet()) {
				fw.write("\n[" + name + "]\n");
				fw.write(fields.get(name));
				fw.write("\n");

				d.add(new Field(name, fields.get(name), Field.Store.NO, Field.Index.ANALYZED));
			}
			fw.close();
			indexWriter.addDocument(d);

			return fields;
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print("\nArquivo corrompido: " + pdf.getName());
			corrupted++;
			//pdf.delete();
			
			return null;
		}
	}

	public void index() {
		try {
			FileWriter fw = new FileWriter(outputFileName);
			File f = new File(bulasFolder);
			File indexf = new File("../indice_bulas");
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();

			indexWriter = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);

			File[] pdfs = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*pdf");
				}
			});

			//Map <String, String> fields = indexFile(new File("../bulas_teste/bulas/246875.pdf"));
			//if (fields.size() < 7) fw.write("293687.pdf  " + fields.size() + "\n");
			
			System.out.println("Existem " + pdfs.length + " arquivos PDFs para analisar"); 

			int i = 1;
			for (File pdf : pdfs) {
				System.out.print("Indexando doc " + (i) + " de " + pdfs.length + ": " + pdf.getName() + " ... ");

				Map<String, String> fields = indexFile(pdf);
				if (fields != null) {
					if (fields.size() < 7) fw.write(pdf.getName() + "  " + fields.size() + "\n");
					secCount.put(fields.size(), secCount.get(fields.size()) + 1);
					
					System.out.println(String.format(" - %d secoes (%4.1f%% concluido)", fields.size(), 100.0 * i / pdfs.length));
				}
				else
					System.out.println(String.format(" - %d de %d (%4.1f%% corrompido)", corrupted, i, 100.0*corrupted / i));

				i++;
				//if (i > 20) break;
			}
			indexWriter.optimize();
			indexWriter.close();
			
			fw.write(String.format("Taxa de corrompimento dos PDFs: %4.1f\n", 100*((float)corrupted) / i));
			fw.write(String.format("Quantidade de secoes e suas respectivas quantidades de bulas:\n"));
			fw.write("secoes\tbulas\n");
			for (Integer key : secCount.keySet()) {
				fw.write(String.format("%d\t%d\n", key, secCount.get(key)));
			}
			fw.close();
			
			
			System.out.println("Concluido. Indice criado!");			
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
