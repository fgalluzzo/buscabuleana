/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;


/**
 *
 * @author galluzzo
 */
@ManagedBean(name="BulaBean")
@RequestScoped
public class BulaBean {

    private String nomeArquivo;
    private String indicacao;
    private String texto;
    /** Creates a new instance of BulaBean */
    public BulaBean() {
    }

    public String getIndicacao() {
        return indicacao;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getTexto() {
        return texto;
    }

    public void setIndicacao(String indicacao) {
        this.indicacao = indicacao;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
    
}
