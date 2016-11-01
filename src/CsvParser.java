import java.util.ArrayList;
import java.util.regex.Pattern;




public class CsvParser {

	private char separator;
	private final String regex = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
	
	public CsvParser(char separator)
	{
		this.separator=separator;
	}
	public ArrayList<String>ParseLine(String line)
	{
		ArrayList<String> res =new ArrayList<String>();
		//on decoupe la ligne selon les mots qui correspondent au pattern
		String [] splitted = line.split(regex.replace(',', separator));
		for (String s:splitted)
		{
			res.add(s);
			//System.out.println(s);
		}
		
		
		return res;
	}
}
