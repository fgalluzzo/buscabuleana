package DTO;

import controle.SparqlControle;
import controle.SparqlControle.Cell;
import controle.SparqlControle.Row;

import java.util.ArrayList;
import java.util.List;
import util.CarregaConsultas;

public class SparqlDTO {

    private List<String> prefixos;
    private List<String> consultaExplo;
    private String textoPesquisa;
    private List<List<Cell>> results;
    private List<String> headers;

    public SparqlDTO() {
        prefixos = CarregaConsultas.consultas.getPrefixos();
        consultaExplo = CarregaConsultas.consultas.getConsultas();
        textoPesquisa = consultaExplo.get(0);
        
        headers = new ArrayList<String>();
        headers.add("a");
        headers.add("b");

        /* "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
        +"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
        +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        +"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
        +"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
        +"PREFIX dbp: <http://dbpedia.org/resource/>\n"
        +"PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
        +"PREFIX dbpedia: <http://dbpedia.org/>\n"
        +"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
        +"PREFIX bw: <http://bulasweb.test/rdf#>\n"
        +"SELECT * WHERE {\n  ?a rdfs:label ?b .\nFILTER regex(str(?b), \"^F[ale]\", \"i\").  \n}\n";*/
    }
    
    public List<String> getHeaders() {
		return headers;
	}
    
    public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

    public List<String> getPrefixos() {
        return prefixos;
    }

    public void setPrefixos(List<String> prefixos) {
        this.prefixos = prefixos;
    }

    

    public void setTextoPesquisa(String textoPesquisa) {
        this.textoPesquisa = textoPesquisa;
    }

    public String getTextoPesquisa() {
        return textoPesquisa;
    }

    public List<List<Cell>> getResults() {
        return results;
    }

    public void setResults(List<List<Cell>> res) {
        this.results = res;
    }
   
    public List<String> getConsultaExplo() {
		return consultaExplo;
	}
    public void setConsultaExplo(List<String> consultaExplo) {
		this.consultaExplo = consultaExplo;
	}
}
