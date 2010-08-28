/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DTO;

import bean.MedicamentoBean;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author galluzzo
 */
@ManagedBean(name="MedicamentoDTO")
@RequestScoped
public class MedicamentoDTO {

    private MedicamentoBean medicamento;

    public MedicamentoBean getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(MedicamentoBean medicamento) {
        this.medicamento = medicamento;
    }

    
}
