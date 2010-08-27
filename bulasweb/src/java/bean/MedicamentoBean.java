package bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


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

	
	@OneToOne (mappedBy="medicamento")
	private BulaBean bula;
	

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
	
	public BulaBean getBula() {
		return bula;
	}
	
	public void setBula(BulaBean bula) {
		this.bula = bula;
	}

}
