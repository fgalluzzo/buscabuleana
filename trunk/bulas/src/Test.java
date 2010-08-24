import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;

import bulas.bean.FarmacoBean;
import bulas.dao.FarmacoDao;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("bulas_base");
		EntityManager em = emf.createEntityManager();

		JOptionPane.showConfirmDialog(null, "afasf");
		
		FarmacoDao farmDao = new FarmacoDao(em);
		Collection<FarmacoBean> fms = farmDao.findAll(FarmacoBean.class);
		for (FarmacoBean f : fms) System.out.println(f.getNome());
		
		em.close();
		emf.close();
	}

}
