import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MakeFiles {

	private char separator=';';
	public MakeFiles(char separator)
	{
		this.separator=separator;
	}
	//construit le fichier 
	public  void makeTheFiles(String path)
	{
		HashMap<String,Integer> dict_user= new HashMap<String,Integer>();
		HashMap<String,Integer> dict_product= new HashMap<String,Integer>();
		BufferedReader br = null;
		BufferedWriter bw_user=null;
		BufferedWriter bw_product=null;
		BufferedWriter bw_agg=null;
		File file_user = new File("lookup_user.csv");
		File file_product = new File("lookup_product.csv");
		File file_agg = new File("agg_ratings.csv");
		
		//si les fichiers n'existent pas on les crée
		if (!file_user.exists()) {
			try {
				file_user.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!file_product.exists()) {
			try {
				file_product.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		int index_user=0;
		int index_product=0;
		CsvParser parser= new CsvParser(this.separator);
		
		try {

			String line;

			br = new BufferedReader(new FileReader(path));
			FileWriter fw_user = new FileWriter(file_user.getAbsoluteFile());
			FileWriter fw_product = new FileWriter(file_product.getAbsoluteFile());
			bw_user = new BufferedWriter(fw_user);
			bw_product = new BufferedWriter(fw_product);
			
			bw_user.write("userId,userIdAsInteger".replace(',', separator)+"\n");
			bw_product.write("itemId,itemIdAsInteger".replace(',', separator)+"\n");
			br.readLine();//on saute le header
			while ((line = br.readLine()) != null) {
				
				ArrayList<String>fields= parser.ParseLine(line);
				
				/*
				 * si le dictionnaire ne contient pas l'élément en clé alors on le rajoute dans 
				 * la hashmap
				 */
				// le fichier est de la forme: userId,itemId,rating,timestamp
				//donc on prend le premier element
				//System.out.println(fields.get(0));
				if(!dict_user.containsKey(fields.get(0)))
				{
					dict_user.put(fields.get(0), index_user);
					String lineToWrite=fields.get(0)+separator+index_user+"\n";
					bw_user.write(lineToWrite);
					index_user++;
					
				}
				if(!dict_product.containsKey(fields.get(1)))
				{
					dict_product.put(fields.get(1), index_product);
					String lineToWrite=fields.get(1)+separator+index_product+"\n";
					bw_product.write(lineToWrite);
					index_product++;
				}
				
				
			}

		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try {
				if (br != null)
					br.close();
				if(bw_user!=null)
					bw_user.close();
				if(bw_product!=null)
					bw_product.close();
			} 
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		}

		//ensuite on construit le fichier 
		
		Timestamp timestampMax = findMaxTimestamp(path);
		
		BufferedReader br2 = null;
		//creation d'un dictionnaire (<userId,productId>, rating)
		HashMap<Pair<Integer,Integer>,Float> mp = new HashMap<Pair<Integer,Integer>,Float>();
		try {

			String line;
			FileWriter fw_agg = new FileWriter(file_agg.getAbsoluteFile());
			bw_agg = new BufferedWriter(fw_agg);
			br2 = new BufferedReader(new FileReader(path));
			br2.readLine();//on saute le header
			while ((line = br2.readLine()) != null) {
				
				ArrayList<String>fields= parser.ParseLine(line);
				
				//les correspondances sont déjà stockées dans les dictionnaires
				//du coup on les réutilise
				int index_user_w=dict_user.get(fields.get(0));
				int index_product_w=dict_product.get(fields.get(1));
				//si la clé n'existe pas on la crée
				if(!mp.containsKey(new Pair<Integer,Integer>(index_user_w,index_product_w)))
				{
					Timestamp timestamp = Timestamp.valueOf(fields.get(3));
					float rating = Float.parseFloat(fields.get(2));
					float new_rating=computeNewRating(timestampMax, timestamp, rating);
					Pair<Integer,Integer> mp_key= new Pair<Integer,Integer>(index_user_w,index_product_w);
					
					mp.put(mp_key, new_rating);
					
					
				
				}
				else
				{
					Timestamp timestamp = Timestamp.valueOf(fields.get(3));
					float rating = Float.parseFloat(fields.get(2));
					float new_rating=computeNewRating(timestampMax, timestamp, rating);
					Pair<Integer,Integer> mp_key= new Pair<Integer,Integer>(index_user_w,index_product_w);
					float old_rating=mp.get(mp_key);
					new_rating+=old_rating;//on fait l'addition
					mp.put(mp_key, new_rating);
					
					
					
				}
				
				
			}
			//on ecrit le fichier ratings_agg.csv
			bw_agg.write("userIdAsInteger,itemIdAsInteger,ratingSum".replace(',',separator)+"\n");
			
			for (Map.Entry <Pair<Integer,Integer>,Float> entry : mp.entrySet())
			{
				Pair<Integer,Integer> key = entry.getKey();
			    Float value = entry.getValue();
			    String lineToWrite = ""+key.getElement0()+separator+key.getElement1();
			    lineToWrite+=separator+""+value+"\n";
			    bw_agg.write(lineToWrite);
			}

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try {
				if (br2 != null)
					br2.close();
				if(bw_agg!=null)
					bw_agg.close();
			} 
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		
	}
	
	//calcul du timestamp maximal
	private Timestamp findMaxTimestamp(String path)
	{
		Timestamp res =new Timestamp(0);
		BufferedReader br = null;
		CsvParser parser= new CsvParser(this.separator);
		
		try {

			String line;

			br = new BufferedReader(new FileReader(path));
			br.readLine();//on saute le header
			while ((line = br.readLine()) != null) {
				ArrayList<String>fields= parser.ParseLine(line);
				//System.out.println(fields.get(3));
				Timestamp timestamp = Timestamp.valueOf( fields.get(3));
				
				if(timestamp.compareTo(res)>0)
					res=timestamp;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;

	}
	// calcul du nouveau score en appliquant la pénalité
	private float computeNewRating(Timestamp timestampMax,Timestamp timestamp,float rating)
	{
		
		float res = timestampMax.getTime() -timestamp.getTime();
		res/=1000f;//on obtient la difference en secondes
		res/=3600f;//on obtient la difference en heures
		res/=24f;//on obtient la difference en jours
		int nb_jour =Math.round(res);
		res= (float) ((Math.pow(0.95,nb_jour)))*rating;
		//comme on applique la pénalité multiplicative 0.95 autant de fois qu'il y a de 
		//jour de décalage c'est comme si on multipliait par 0.95^nb_jour
		return res;
	}
	
	/*
	 * le fichier est lu 3 fois en tout
	 * J'ai utilisé les hashmap car c'est plus rapide pour les recherches
	 */
	
	
}


