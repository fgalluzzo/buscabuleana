package model.bean;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity (name="ConteudoSecao")
@Table (name="conteudo_secao_bula")
public class ConteudoSecaoBean implements Serializable {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@ManyToOne
	@JoinColumn (name="bula_id")
	private BulaBean bula;
		
	@ManyToOne
	@JoinColumn (name="secao_id")
	private SecaoBulaBean secaoBula;

	@Basic (fetch=FetchType.LAZY)	// o texto pode ser muito, inclusive se pegar toda a lista de bulas pelo dao
	@Column
	private String texto;

	
	public int getId() {
		return id;
	}
	
	public String getTexto() {
		return texto;
	}
	
	public void setTexto(String texto) {
		this.texto = texto;
	}
	
	public BulaBean getBula() {
		return bula;
	}
	
	public void setBula(BulaBean bula) {
		this.bula = bula;
	}
	
	public SecaoBulaBean getSecaoBula() {
		return secaoBula;
	}
	
	public void setSecaoBula(SecaoBulaBean secaoBula) {
		this.secaoBula = secaoBula;
	}
}
