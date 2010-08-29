/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DTO;

import bean.MedicamentoBean;

/**
 *
 * @author galluzzo
 */

public class MedicamentoDTO {

    private MedicamentoBean medicamento;

    public MedicamentoBean getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(MedicamentoBean medicamento) {
        this.medicamento = medicamento;
    }

    public void testar(){
        MedicamentoBean m = new MedicamentoBean();


        m = this.medicamento;
    }
    
}
