package datasetDetails;


import java.util.*;
import java.io.*;

public class CreateJSON {

	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\dbpedia\\"; 
	static String file=path+"infobox_property_definitions_en.nt";
	
	HashMap<String, HashSet<String>> mapOutput;
	HashSet<String> reduceOutput;
	
	public static void main(String[] args){
		CreateJSON obj=new CreateJSON();
		obj.mapper(file);
		obj.reducer();
	}
	
	public void mapper(String file){
		mapOutput=new HashMap<String,HashSet<String>>();
		Scanner in=null;
		try{
			in=new Scanner(new FileReader(file));
			while(in.hasNextLine()){
				String line=in.nextLine();
				if(line.trim().length()==0||line.substring(0,1).equals("#"))
					continue;
				
			}
		}catch(Exception e){e.printStackTrace();}finally{in.close();}
	}
	
	public void reducer(){
		
	}
	
}
