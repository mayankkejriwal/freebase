package typeAnalysis;


import java.io.*;
import java.util.*;

public class TypeSameAsDiagnostics {
/**
 * Meant to parse, analyze and do other operations on the files in type-sameAs-diagnostics
 * @param args
 */
	
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\type-sameAs-diagnostics\\"; 

	
	public static void main(String[] args) throws IOException{
		printExampleDiagnosticsFile(path+"consolidatedTypesSubjects", 
				path+"diagnosticsExamplesTypesSubjects");

	}
	
	/*
	 * See diagnosticsTypesSubjects for an example of what this file will produce
	 */
	public static void printDiagnosticsFile(String file, String outfile)throws IOException{
		HashMap<String, Long> counts=new HashMap<String, Long>();
		Scanner in=new Scanner(new FileReader(file));
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t");
			ArrayList<String> tmp=new ArrayList<String>();
			for(int i=3; i<fields.length; i++)
				tmp.add(fields[i]);
			Collections.sort(tmp);
			String category=new String("");
			for(int i=0; i<tmp.size()-1; i++)
				category+=(tmp.get(i)+"\t");
			category+=tmp.get(tmp.size()-1);
			if(!counts.containsKey(category))
				counts.put(category, (long) 0);
			long f=counts.get(category);
			f++;
			counts.put(category, f);
			
		}
		
		in.close();
		long total=0;
		PrintWriter out=new PrintWriter(new File(outfile));
		for(String key: counts.keySet()){
			out.println(key+"\t"+counts.get(key));
			total+=counts.get(key);
		}
		out.println("TOTAL\t"+total);
		out.close();
		
		
	}
	
	/*
	 * See diagnosticsExamplesTypesSubjects for an example of what this file will produce
	 */
	public static void printExampleDiagnosticsFile(String file, String outfile)throws IOException{
		HashMap<String, HashSet<String>> examples=new HashMap<String, HashSet<String>>();
		HashSet<String> forbiddenCategories=new HashSet<String>();
		
		Scanner in=new Scanner(new FileReader(file));
		while(in.hasNextLine()&&forbiddenCategories.size()<17){
			
			String line=in.nextLine();
			String[] fields=line.split("\t");
			ArrayList<String> tmp=new ArrayList<String>();
			for(int i=3; i<fields.length; i++)
				tmp.add(fields[i]);
			Collections.sort(tmp);
			String category=new String("");
			for(int i=0; i<tmp.size()-1; i++)
				category+=(tmp.get(i)+"\t");
			category+=tmp.get(tmp.size()-1);
			
			if(forbiddenCategories.contains(category))
				continue;
			
			String pair=fields[0]+"\t"+fields[1]+"\t"+fields[2];
			if(!examples.containsKey(category))
				examples.put(category,new HashSet<String>());
			examples.get(category).add(pair);
			if(examples.get(category).size()==10)
				forbiddenCategories.add(category);
				
			
			
			
			
		}
		
		in.close();
		
		PrintWriter out=new PrintWriter(new File(outfile));
		for(String key: examples.keySet()){
			out.println(key);
			for(String ex: examples.get(key))
				out.println(ex);
			out.println();
		}
		
		out.close();
		
		
	}

}
