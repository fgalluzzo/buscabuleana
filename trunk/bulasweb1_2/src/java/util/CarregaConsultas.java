/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 *
 * @author galluzzo
 */
public class CarregaConsultas {

    public static ConsultasSparql consultas = loadConsultas();
    public static ConsultasSparql loadConsultas(){
        FacesContext context = FacesContext.getCurrentInstance();

        ServletContext sc = (ServletContext) context.getExternalContext().getContext();

        String dir = sc.getRealPath("")+"/";
        XStream xs = new XStream(new DomDriver());
        xs.alias("consultasSPARQL", ConsultasSparql.class);
        xs.aliasType("consulta", String.class);
        xs.aliasType("prefixo", String.class);
        File file = new File(dir+"/consultas.xml");
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CarregaCfg.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (ConsultasSparql) xs.fromXML(fi);
    }
}
