package principal;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class Princ {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Indexar index = new Indexar();
		//index.index();
		a();
	}
	public static void a() {
		
		try {
			File indexf = new File("../indice_bulas");
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();

			IndexSearcher isearcher = new IndexSearcher(dir, true); // read-only=true
			// Parse a simple query that searches for "text":
			QueryParser parser = new QueryParser(Version.LUCENE_30, "aviso", analyzer);
			Query query = parser.parse("idosos");
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;

			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
			  Document hitDoc = isearcher.doc(hits[i].doc);
			  System.out.println(hitDoc.get("id") + ".txt\t" + hits[i].score );
			}
			isearcher.close();
			dir.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
