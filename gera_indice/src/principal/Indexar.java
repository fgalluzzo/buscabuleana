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
	
	Properties properties = loadProperties();
	
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
		
	public Map <String, String> findSections(String text, String filename) {
		
		text = text.replaceAll("[\\xb0\\xba\"\']", "");	// remove aspas, º
		text = text.replaceAll("[ \t]+", " ");	// juntar espaçoes e tabulaçoes
		text = text.replaceAll("(?m)^[ \t]+", "");	// remover espacos no inicio de linha
		text = text.replaceAll("([a-zA-Z]{2})- ?\r?\n", "$1");	// remove separacao de silabas
		text = text.replaceAll("([^A-Z.: ]) ?\r?\n", "$1 ");	// remover quebras de linhas na mesma frase
		text = text.replaceAll("(?m)^[ \t]+", "");	// remover espacos no inicio de linha (denovo mesmo)
		text = text.replaceAll("([0-9][0-9.-]+[0-9]|[ivxIVX]+)[\\x29.-]? +", "");	// remove possiveis numeracoes em seçoes
		
		TreeMap <Integer, String> map = new TreeMap<Integer, String>();

		// Encontra posiçao(oes) de cada seção na bula
		for (Object section : properties.keySet()) {
			String sectionName = (String) section;
		
			String regex = properties.getProperty(sectionName);
			System.out.println(sectionName + ": " + regex);
			
			Matcher m = Pattern.compile(regex, Pattern.MULTILINE).matcher(text.toLowerCase());
			while (m.find()) {
				map.put(m.start(), sectionName);
			}
		}
		
		
		Map<String, String> sections = new HashMap<String, String> ();
		try {
			FileWriter fw = new FileWriter(filename + ".xml");
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

	public void index() {
		try {
			File f = new File("../bulas_teste");
			File indexf = new File("../indice_bulas");
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();

			IndexWriter indexWriter = new IndexWriter(dir, analyzer,
					MaxFieldLength.UNLIMITED);

			File[] pdfs = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*pdf");
				}
			});
			
			int i = 0;
			for (File pdf : pdfs) {
				System.out.print("Indexando doc: " + pdf.getName() + " ... ");
				
				FileInputStream fi = new FileInputStream(pdf);
				PDFParser parser = new PDFParser(fi);
				try {
					parser.parse();

					COSDocument cd = parser.getDocument();
					PDFTextStripper stripper = new PDFTextStripper();
					String text = stripper.getText(new PDDocument(cd));
					cd.close();
					
					// Escreve bula em texto plano (nao parseado)
					FileWriter fw = new FileWriter("../bulas_teste/" + pdf.getName() + ".txt");
					fw.write(text.toLowerCase());
					fw.close();
					
					// Obtem nome e texto das seçoes da bula
					Map<String, String> fields = findSections(text, "../bulas_teste/" + pdf.getName());
					
					// Cria documento do Lucene
					Document d = new Document();
					d.add(new Field("id", pdf.getName(), Field.Store.YES, Field.Index.NO));
					d.add(new Field("name", text, Field.Store.NO, Field.Index.ANALYZED));

					fw = new FileWriter("../bulas_teste/" + pdf.getName() + ".ini");
					for (String name : fields.keySet()) {
						fw.write("\n[" + name + "]\n");
						fw.write(fields.get(name));
						fw.write("\n");

						d.add(new Field(name, fields.get(name), Field.Store.NO, Field.Index.ANALYZED));
					}
					fw.close();
					indexWriter.addDocument(d);
					
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("\nArquivo corrompido:" + pdf.getName());
					//pdf.delete();
					continue;
				}

				i++;
				System.out.println("Ok (" + ((100 * i) / pdfs.length) + "%)");
				//if (i > 20) break;
			}
			indexWriter.optimize();
			indexWriter.close();
			System.out.println("Indice criado!");
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
