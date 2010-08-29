package bean;

import javax.persistence.*;

@Entity (name="Laboratorio")
@Table (name="laboratorio")
public class LaboratorioBean {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Column
	private String nome;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getId() {
		return id;
	}
	
	
}
