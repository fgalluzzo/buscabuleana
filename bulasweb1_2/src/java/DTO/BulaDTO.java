/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DTO;

import bean.BulaBean;


/**
 *
 * @author galluzzo
 */

public class BulaDTO {
    private String textoPesquisa;
    private BulaBean bulaSelecionada;

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
    
    


    
}
