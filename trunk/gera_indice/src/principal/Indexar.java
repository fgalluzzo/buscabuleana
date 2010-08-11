package principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;



public class Indexar {
	
	public void index(){
		try {
			File f = new File("c:\\bulas");
			File indexf = new File("c:\\index\\indice_bulas");
			Directory dir = new SimpleFSDirectory(indexf);
			SimpleAnalyzer analyzer = new SimpleAnalyzer();
		
			IndexWriter writer = new IndexWriter(dir,analyzer, MaxFieldLength.UNLIMITED);
			
			File[] pdfs = f.listFiles();
			for(File pdf : pdfs){
				FileInputStream fi = new FileInputStream(pdf);
				//System.out.println("Indexando doc: "+pdf.getName());
				
				PDFParser parser = new PDFParser(fi);
				try{
					parser.parse();
				
				
				COSDocument cd = parser.getDocument();
				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(new PDDocument(cd));
				
				cd.close();
				Document d = new Document();
				d.add(new Field("id", pdf.getName(), Field.Store.YES, Field.Index.NO));
			    d.add(new Field("name", text, Field.Store.NO, Field.Index.ANALYZED));
			    writer.addDocument(d);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("arquivo corrompido:" +pdf.getName());
					pdf.delete();
					continue;
				}
				
				
				
				
				
			    
				
				//System.out.println("Arquivo indexado!");
			}
			writer.optimize();
			writer.close();
			System.out.println("Indice criado!");
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	
	}
}
