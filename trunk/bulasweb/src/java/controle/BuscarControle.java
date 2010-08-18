/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author galluzzo
 */
public class BuscarControle {

    private List<String> Bulas;

    /** Creates a new instance of BuscarControle */
    public BuscarControle() {
        Bulas = new ArrayList<String>();
       
    }

    public List<String> getBulas() {
        return Bulas;
    }

    public void iniciar(){
        Bulas.add("Remedio 1");
        Bulas.add("Remedio 2");
        Bulas.add("Remedio 3");
        Bulas.add("Remedio 4");
        Bulas.add("Remedio 5");
        Bulas.add("Remedio 6");
    }
}
