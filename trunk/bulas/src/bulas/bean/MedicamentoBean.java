package bulas.bean;

import java.io.Serializable;

import javax.persistence.*;


@Entity (name="Medicamento")
@Table (name="medicamento")
public class MedicamentoBean implements Serializable {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;	
	
	@Column
	private String nome;
	
	@ManyToOne
	@JoinColumn (name="lab_detentor_id")
	private LaboratorioBean laboratorio;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public LaboratorioBean getLaboratorio() {
		return laboratorio;
	}

	public void setLaboratorio(LaboratorioBean laboratorio) {
		this.laboratorio = laboratorio;
	}

	public int getId() {
		return id;
	}

}
