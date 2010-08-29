/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DTO;

import bean.BulaBean;
import bean.ConteudoSecaoBean;


/**
 *
 * @author galluzzo
 */

public class BulaDTO {
    private String textoPesquisa;
    private BulaBean bulaSelecionada;
    private ConteudoSecaoBean secaoBula;

    public String getTextoPesquisa() {
        return textoPesquisa;
    }

    public void setTextoPesquisa(String textoPesquisa) {
        this.textoPesquisa = textoPesquisa;
    }

    public BulaBean getBulaSelecionada() {
        return bulaSelecionada;
    }

    public void setBulaSelecionada(BulaBean bulaSelecionada) {
        this.bulaSelecionada = bulaSelecionada;
    }
    
    public ConteudoSecaoBean getSecaoBula() {
			return secaoBula;
		}
    
    public void setSecaoBula(ConteudoSecaoBean secaoBula) {
			this.secaoBula = secaoBula;
		}


    
}
