import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("bulas_base");
		EntityManager em = emf.createEntityManager();

		JOptionPane.showConfirmDialog(null, "afasf");
		
		em.clear();
		emf.close();
	}

}
