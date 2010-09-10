package DTO;

import java.util.List;
import util.CarregaConsultas;

public class SparqlDTO {

    private List<String> prefixos;
    private String textoPesquisa;
    private List<List<String>> results;

    public SparqlDTO() {
        prefixos = CarregaConsultas.consultas.getPrefixos();
        textoPesquisa = CarregaConsultas.consultas.getConsultas().get(0);

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

    public void setResults(List<List<String>> results) {
        this.results = results;
    }

    public List<List<String>> getResults() {
        return results;
    }
}
