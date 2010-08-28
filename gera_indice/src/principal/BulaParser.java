package principal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parseia texto (puro) da bula.
 * 
 * @author expedit
 *
 */
public class BulaParser {
	
	private static final Properties properties = loadProperties();
	
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
	
	public static Properties getProperties() {
		return properties;
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
}
