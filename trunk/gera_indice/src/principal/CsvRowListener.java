package principal;


public interface CsvRowListener {

	public void start();
	
	public void doRowProcessing(String [] values);
	
	public void finish();
}
