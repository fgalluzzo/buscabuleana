/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 *
 * @author galluzzo
 */
@Entity (name="Bula")
@Table (name="bula")
public class BulaBean {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn (name = "medicamento_fk", referencedColumnName = "id",
			insertable = true, updatable = true) 
	private MedicamentoBean medicamento;
	
	@Basic (fetch=FetchType.LAZY)	// o texto pode ser muito, inclusive se pegar toda a lista de bulas pelo dao
	@Column
    private String texto;

	@Column
    private String codigo;
	
	
	@OneToMany (mappedBy="bula", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<ConteudoSecaoBean> conteudoSecao = new ArrayList<ConteudoSecaoBean>();


	/** Creates a new instance of BulaBean */
    public BulaBean() {
    }
    
    public int getId() {
		return id;
	}

    public MedicamentoBean getMedicamento() {
		return medicamento;
	}
    
    public void setMedicamento(MedicamentoBean medicamento) {
		this.medicamento = medicamento;
	}
    
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
    
    public String getCodigo() {
		return codigo;
	}

    public List<ConteudoSecaoBean> getConteudoSecao() {
		return conteudoSecao;
	}

	public void setConteudoSecao(List<ConteudoSecaoBean> conteudoSecao) {
		this.conteudoSecao = conteudoSecao;
	}

	
	/**
	 * Retorna texto da secao especificada.
	 * 
	 * @param name
	 * @return
	 */
	public String getSectionContentsByName(String name) {
		for (ConteudoSecaoBean csb : conteudoSecao) {
			if (csb.getSecaoBula().getNomeCurto().equals(name))
				return csb.getTexto();
		}
		return null;
	}
	
	public ConteudoSecaoBean getSectionByName(String name) {
		for (ConteudoSecaoBean csb : conteudoSecao) {
			if (csb.getSecaoBula().getNomeCurto().equals(name))
				return csb;
		}
		return null;
	}
}
