package similarities;

import java.io.*;
import java.util.*;

public class Jaccard {

	/**
	 * We will use this class to compute jaccard similarity between types-token files
	 * of dbpedia and freebase.
	 */
	
	
	HashMap<String, HashSet<String>> tokens;
	
	//we cull dbpedia types that do not contain dbpedia.org/ontology
	//note that 417/618 types are dbpedia.org/ontology. The file1 should be the dbpedia
	//consolidatedGlobalTokens file. The boolean is to enforce that.
	public Jaccard(String file1, boolean dbpedia)throws IOException{
		if(!dbpedia)
			return;
		Scanner in=new Scanner(new FileReader(file1));
		tokens=new HashMap<String, HashSet<String>>();
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t");
			if(!fields[0].contains("dbpedia.org/ontology"))
				continue;
			HashSet<String> tmp=new HashSet<String>(30000);
			for(int i=1; i<fields.length; i+=2){
				tmp.add(fields[i]);
			}
			tokens.put(fields[0], tmp);
		}
		
		in.close();
	}
	
	//file2 should be the freebase consolidatedGlobalTokens file
	//outfile will contain tab-separated fields of the form:
	//freebase-type dbpedia-type jaccard-score
	//Note that jaccard-score will always be strictly above threshold.
	public void printSimilarityFile(String file2, String outfile, double threshold)throws IOException{
		Scanner in=new Scanner(new FileReader(file2));
		PrintWriter out=new PrintWriter(new File(outfile));
		ArrayList<String> list=new ArrayList<String>(tokens.keySet());
		Collections.sort(list);
		int counter=1;
		//int count=0;
		while(in.hasNextLine() //&& count<10
				){
			
			String line=in.nextLine();
			String[] fields=line.split("\t");
			String freebaseType=fields[0];
			
			HashSet<String> tmp=new HashSet<String>(30000);
			for(int i=1; i<fields.length; i+=2){
				tmp.add(fields[i]);
			}
			fields=null;
			for(String dbpediaType: list){
				double score=computeJaccard(tokens.get(dbpediaType), tmp);
				if(score>threshold)
					out.println((counter+"y")+"\t"+freebaseType+"\t"+dbpediaType+"\t"+score);
				counter++;
			}
			tmp=null;
		//	count++;
			
		}
		in.close();
		out.close();
	}
	
	public static <T>int unionCardinality(Set<T> set1, Set<T> set2){
		int result=set1.size();
		for(T t: set2)
			if(!set1.contains(t))
				result++;
		return result;
	}
	
	public static <T>int intersectionCardinality(Set<T> set1, Set<T> set2){
		int result=0;
		for(T t: set1)
			if(set2.contains(t))
				result++;
		return result;
	}
	
	public static double computeJaccard(Set<String> set1, Set<String> set2){
		
		
		int union=unionCardinality(set1, set2);
		if(union==0){
		//	System.out.println("Problems: union is 0");
			return -1.0;
		}
		int intersection=intersectionCardinality(set1, set2);
		
		return 1.0*intersection/union;
	}
	
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\";
	
	static String dbpediaFile=path+"dbpedia\\consolidatedGlobalTokens";
	static String freebaseFile=path+"freebase\\combinedConsolidatedTokens";
	static String outfile=path+"type-experiments\\f-db-jaccard-withZeros.txt";
	
	public static void main(String[] args)throws IOException{
		long time=(System.currentTimeMillis());
		Jaccard test=new Jaccard(dbpediaFile, true);
		test.printSimilarityFile(freebaseFile, outfile, -1.0);
		double t=(1.0*(System.currentTimeMillis()-time))/60000;
		System.out.println("Minutes elapsed: "+t);
		
		
	}
	
	public static void test(){
		HashSet<String> set1=new HashSet<String>();
		HashSet<String> set2=new HashSet<String>();
		
		set1.add("FRANCISCO");
		set1.add("MISSION");
		set1.add("SAN");
		//set1.add("USA");
		
		set2.add("FRANCISCO");
		set2.add("MISSION");
		//set2.add("FRAN");
		set2.add("USA");
		
		System.out.println(computeJaccard(set1, set2));
	}
}
