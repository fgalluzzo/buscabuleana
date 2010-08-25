package bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.*;


@Entity (name="Medicamento")
@Table (name="medicamento")
public class MedicamentoBean implements Serializable {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;	
	
	@Column
	private String nome;
	
	@Column
	private String associacao;
	
	@ManyToOne
	@JoinColumn (name="lab_detentor_id")
	private LaboratorioBean laboratorio;

	@ManyToMany (cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable (name="associacao",
			joinColumns=@JoinColumn(name="medicamento_id"),
			inverseJoinColumns=@JoinColumn(name="farmaco_id"))
	private List <FarmacoBean> farmacos= new ArrayList<FarmacoBean>();


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

	public List<FarmacoBean> getFarmacos() {
		return farmacos;
	}

	public void setFarmacos(List<FarmacoBean> farmacos) {
		this.farmacos = farmacos;
	}
	
	public String getAssociacao() {
		return associacao;
	}
	
	public void setAssociacao(String associacao) {
		this.associacao = associacao;
	}
}
