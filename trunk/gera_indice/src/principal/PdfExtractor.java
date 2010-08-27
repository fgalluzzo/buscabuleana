package principal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PdfExtractor {
	
	private String bulasFolder = null;
	
	public PdfExtractor(String bulasFolder) {
		this.bulasFolder = bulasFolder;
	}
	
	public String extractTextFromPdf(File pdfFile) throws FileNotFoundException, IOException {
		String text = null;
		try {
			FileInputStream fi = new FileInputStream(pdfFile);
			PDFParser parser = new PDFParser(fi);
			parser.parse();

			COSDocument cd = parser.getDocument();
			PDDocument pd = new PDDocument(cd);
			
			PDFTextStripper stripper = new PDFTextStripper();
			text = stripper.getText(pd);
			
			pd.close();
			cd.close();
			fi.close();
			
			return text;
			
		} catch (FileNotFoundException e) {
			throw e;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			if (text == null || "".equals(text)) throw e;
			return text;
		}
	}
	
	public void extractPdfs() {

		File bulasf = new File(bulasFolder);
		File [] pdfs = bulasf.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*pdf");
			}
		});
		
		String txtDirectory = bulasFolder + "\\plain\\";
		File dir = new File(txtDirectory);
		dir.mkdir();

		System.out.println("Existem " + pdfs.length + " arquivos PDFs para extrair"); 

		int count = 1;
		int corrupted = 0;
		for (File pdf : pdfs) {

			try {
				System.out.println(String.format("Extracting from %s (%d / %d %d %%)",
						pdf.getName(), count, pdfs.length, 100 * count / pdfs.length));
				String text = extractTextFromPdf(pdf);

				// Escreve bula em texto plano (nao parseado)
				FileWriter fw;
				try {
					String txtFileName = pdf.getName() + ".txt";
					System.out.println("Writing " + txtFileName + "...");
					fw = new FileWriter(txtDirectory + txtFileName);
					fw.write(text);
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch (Exception e) {
				System.out.print("\nArquivo corrompido: " + pdf.getName());
				corrupted++;
				//pdf.delete();
			}
			
			count++;
		}

		System.err.println(String.format("Concluido. Taxa de corrompimento dos PDFs: %4.1f", 100*((float)corrupted) / count));
	}
}
