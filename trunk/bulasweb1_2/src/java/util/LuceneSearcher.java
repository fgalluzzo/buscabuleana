package util;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import DTO.BulaDTO;
import bean.BulaBean;
import bean.ConteudoSecaoBean;
import bean.SecaoBulaBean;
import controle.BuscarControle.SingleResult;

public class LuceneSearcher {

	private final String INDICE = CarregaCfg.config.getIndice();

	private IndexSearcher is;
	private SimpleAnalyzer analyzer;
	private Directory dir;
	private IndexReader ir;

	public LuceneSearcher() throws CorruptIndexException, IOException  {

		File f = new File(INDICE);
		dir = FSDirectory.open(f);
		ir = IndexReader.open(dir);
		is = new IndexSearcher(ir);
		analyzer = new SimpleAnalyzer();
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	public ScoreDoc[] searchInField(String text, String field) throws ParseException, IOException {
		QueryParser parser = new QueryParser(Version.LUCENE_20, field, analyzer);
		Query query = parser.parse(text);
		TopDocs topDocs = is.search(query, 50);
		ScoreDoc[] docs = topDocs.scoreDocs;
		return docs;
	}

	public ScoreDoc[] searchFullText(String text) throws ParseException, IOException {

		ScoreDoc[] docs = searchInField(text, "text");
		return docs;
	}
	
	public Document getDocument(int id) throws CorruptIndexException, IOException {
		return is.doc(id);
	}
}
