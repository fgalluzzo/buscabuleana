/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controle;

import DTO.MedicamentoDTO;
import bean.MedicamentoBean;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;


/**
 *
 * @author galluzzo
 */
public class RdfControle {

    /** Creates a new instance of RdfControle */
    public RdfControle() {
    }
    public void geraRDF(){
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueExpression expression = app.getExpressionFactory().createValueExpression(context.getELContext(),
                String.format("#{%s}", "MedicamentoDTO"), Object.class);
        MedicamentoDTO md = (MedicamentoDTO) expression.getValue(context.getELContext());
        String bulasWebNS = "http://bulasweb.test/rdf#";
        String mdURI =bulasWebNS +md.getMedicamento().getNome();
        ServletContext sc = (ServletContext) context.getExternalContext().getContext();

        
        
        Model model = ModelFactory.createDefaultModel();

        //Criação do recurso medicamento
        Resource medicamento = model.createResource(mdURI);

        //Criação das propriedades a partir do banco
        medicamento.addProperty(new PropertyImpl(bulasWebNS,"nome"),md.getMedicamento().getNome());
        medicamento.addProperty(new PropertyImpl(bulasWebNS,"laboratorio"),md.getMedicamento().getLaboratorio().getNome());
        InteracaoControle ic = new InteracaoControle();
        List<MedicamentoBean> medInt = ic.getMedicamentosInteracao();
        Bag InteracaoMedic = model.createBag();
        for(int i =0;i<medInt.size();i++){
            InteracaoMedic.add(model.createResource(bulasWebNS+medInt.get(i).getNome()));
        }
        
        medicamento.addProperty(new PropertyImpl(bulasWebNS, "interacao_medicamentosa"), InteracaoMedic);
        


        //Criação das propriedades a partir do csv
        String dir = sc.getRealPath("")+"/";
        File dirGate = new File(dir+"gate");
        String bula = md.getMedicamento().getBulas().get(0).getCodigo();
        //for(File csv : dirGate.listFiles()){
        File csv = new File(dirGate+"/"+bula+".csv");
            FileReader fr;
            try {
                fr = new FileReader(csv);
                BufferedReader in = new BufferedReader(fr);
                while(in.ready()){
                    String linha = in.readLine();
                    String[] dados = linha.split(",");
                    medicamento.addProperty(new PropertyImpl(bulasWebNS,dados[0].replaceAll("\"","")),dados[1].replaceAll("\"",""));

                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(RdfControle.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(RdfControle.class.getName()).log(Level.SEVERE, null, ex);
            }



        //gerando o modelo        
        model.setNsPrefix("bw", bulasWebNS);
        File arquivoRDF = new File(dir+"rdf/"+md.getMedicamento().getNome()+".xml");
        FileWriter writer;
        try {
            writer = new FileWriter(arquivoRDF);
            model.write(writer);
            System.out.println(arquivoRDF.getCanonicalPath());

            context.getExternalContext().redirect("/bulasweb1_2/rdf/"+arquivoRDF.getName());
        } catch (IOException ex) {
            Logger.getLogger(RdfControle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }


}
