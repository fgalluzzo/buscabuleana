package principal;

import java.io.File;

public class PopularBanco {

	/**
	 * Gerencia a indexacao completa.
	 * � possivel comentar trechos que j� estiverem no banco/lucene
	 * 
	 * � necessario rodar em etapas pois ocorre OutOfMemoryError.
	 *  
	 * @param args
	 */
	public static void main(String[] args) {

		// PDF -> TXT
		//PdfExtractor extractor = new PdfExtractor(bulasFolder);
		//extractor.extractPdfs();


		// CSV -> BD
		// Tabelas alteradas: farmaco, medicamento, laboratorio, associacao
		//PersistAnvisaList pb = new PersistAnvisaList();
		//String listaPath = "../bulas_teste/anvisa/medicamentos.csv";
		//pb.process(new File(listaPath));


		// TXT -> secoes -> BD e lucene
		// Tabelas alteradas: bula, secao_bula, conteudo_secao_bula
		//String bulasFolder = "../bulas_teste/bulas/";
		//String indexFolder = "../indice_bulas/";
		//Indexar indexar = new Indexar(bulasFolder, indexFolder);
		//indexar.persisteBulas();
		//indexar.indexaBulas();


		// Associar bulas aos medicamentos
		// Tabelas alteradas: bula, medicamento
		MatchMedicBulas mmb = new MatchMedicBulas();
		mmb.process();
	}
}
