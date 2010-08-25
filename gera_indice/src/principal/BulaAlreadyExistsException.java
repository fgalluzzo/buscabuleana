package principal;

public class BulaAlreadyExistsException extends Exception {
	
	String codigo;

	public BulaAlreadyExistsException(String codigo) {
		this.codigo = codigo;
	}
}
