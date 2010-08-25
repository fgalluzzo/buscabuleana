package bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity (name="Farmaco")
@Table (name="farmaco")
public class FarmacoBean {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Column
	private String nome;
	
	@ManyToMany (mappedBy="farmacos")
	private List <MedicamentoBean> medicamentos = new ArrayList<MedicamentoBean>();

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getId() {
		return id;
	}
	
	public void setMedicamentos(List<MedicamentoBean> medicamentos) {
		this.medicamentos = medicamentos;
	}
	
	public List<MedicamentoBean> getMedicamentos() {
		return medicamentos;
	}
	
	@Override
	public String toString() {
		return nome;
	}
}
