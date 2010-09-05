package principal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import util.PersistenceFactory;
import bean.BulaBean;
import bean.ConteudoSecaoBean;
import dao.BulaDao;

public class GateTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		EntityManager em = PersistenceFactory.createEntityManager();
		BulaDao bulaDao = new BulaDao(em);
		
		File folder = new File("../bulas_teste/bulas/xml/gate/");
		
		Collection<Integer> bulas = bulaDao.getByLengthAndSectionsAsIntList(2000, 9);
		
		for (Integer bulaId : bulas) {
			BulaBean bula = bulaDao.findById(BulaBean.class, bulaId);
			
			try {
				FileWriter fw = new FileWriter(new File(folder, bula.getCodigo() + ".xml"));
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

}
