/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bean;

import java.io.Serializable;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;



/**
 *
 * @author galluzzo
 */
@ManagedBean(name="UsuarioBean")
@RequestScoped
@Entity(name="Usuario")
@Table(name="usuario")
public class UsuarioBean implements Serializable {

    
    private int id;
    private String usuario;
    private String senha;
    private String nome;
    private Date dtNascimento;


    @Column(name="dtnascimento")    
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getDtNascimento() {
        return dtNascimento;
    }
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public int getId() {
        return id;
    }
    @Column(name="nome")
    public String getNome() {
        return nome;
    }
    @Column(name="password")
    public String getSenha() {
        return senha;
    }
    @Column(name="login")
    public String getUsuario() {
        return usuario;
    }
    
    public void setDtNascimento(Date dtNascimento) {
        this.dtNascimento = dtNascimento;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    
}
