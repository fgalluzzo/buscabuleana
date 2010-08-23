package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {

	private File file;
	
	private CsvRowListener listener;

	public CsvReader(File file) {
		super();
		this.file = file;
	}

	public CsvRowListener getListener() {
		return listener;
	}

	public void setListener(CsvRowListener listener) {
		this.listener = listener;
	}
	
	public void process() throws FileNotFoundException {

		BufferedReader in = new BufferedReader(new FileReader(file));
		String linha;
		try {
			if (listener != null) listener.start();
			while ((linha = in.readLine()) != null) {
				String conteudo [] = linha.split(";");
				
				if (listener != null) listener.doRowProcessing(conteudo);
			}
			if (listener != null) listener.finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
