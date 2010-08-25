package bean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity (name="SecaoBula")
@Table (name="secao_bula")
public class SecaoBulaBean {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Column (name="nome_curto")
	private String nomeCurto;
	
	@Column
	private String nome;
	
	@OneToMany (mappedBy="secaoBula", fetch=FetchType.LAZY)
	private List<ConteudoSecaoBean> conteudoSecao;
	

	public String getNomeCurto() {
		return nomeCurto;
	}

	public void setNomeCurto(String nomeCurto) {
		this.nomeCurto = nomeCurto;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getId() {
		return id;
	}
	
	public List<ConteudoSecaoBean> getConteudoSecao() {
		return conteudoSecao;
	}
	
	public void setConteudoSecao(List<ConteudoSecaoBean> conteudoSecao) {
		this.conteudoSecao = conteudoSecao;
	}
}
