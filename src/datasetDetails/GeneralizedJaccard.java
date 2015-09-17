package datasetDetails;

import java.io.*;
import java.util.*;

public class GeneralizedJaccard {

	/**
	 * We will use this class to compute generalized jaccard similarity between types-token files
	 * of dbpedia and freebase. We may also use this class to compute TF scores.
	 */
	
	
	HashMap<String, HashMap<String, Double>> tokens;
	
	
	//we cull dbpedia types that do not contain dbpedia.org/ontology
	//note that 417/618 types are dbpedia.org/ontology. The file1 should be the dbpedia
	//consolidatedGlobalTokens file. The boolean is to enforce that.
	
	public GeneralizedJaccard(String file1, boolean dbpedia)throws IOException{
		if(!dbpedia)
			return;
		
		Scanner in=new Scanner(new FileReader(file1));
		tokens=new HashMap<String, HashMap<String, Double>>();
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t");
			if(!fields[0].contains("dbpedia.org/ontology"))
				continue;
			HashMap<String, Double> tmp=new HashMap<String, Double>(30000);
			double t=0.0;
			
			
			
			for(int i=1; i<fields.length; i+=2){
				t+=Double.parseDouble(fields[i+1]);
				tmp.put(fields[i], Double.parseDouble(fields[i+1]));
			}
			
			
			
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/t);
			tokens.put(fields[0], tmp);
		}
		
		in.close();
	}
	
	//file2 should be the freebase consolidatedGlobalTokens file
	//outfile will contain tab-separated fields of the form:
	//freebase-type dbpedia-type jaccard-score
	//Note that generalized jaccard-score will always be strictly above threshold.
	public void printSimilarityFile(String file2, String outfile, double threshold)throws IOException{
		/*
		 * normalize tokens using norm1
		 */
		HashSet<String> types=new HashSet<String>(tokens.keySet());
		for(String type: types){
			HashMap<String, Double> tmp=tokens.get(type);
			double t=0.0;
			for(String m: tmp.keySet())
				t+=tmp.get(m);
			
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/t);
			
			tokens.put(type, tmp);
		}
		
		Scanner in=new Scanner(new FileReader(file2));
		PrintWriter out=new PrintWriter(new File(outfile));
		ArrayList<String> list=new ArrayList<String>(tokens.keySet());
		Collections.sort(list);
		int counter=1;
	
		while(in.hasNextLine()//&&count<10
				){
			
			String line=in.nextLine();
			String[] fields=line.split("\t");
			String freebaseType=fields[0];
			
			HashMap<String,Double> tmp=new HashMap<String,Double>(30000);
			double t=0.0;
			
			
			
			for(int i=1; i<fields.length; i+=2){
				t+=Double.parseDouble(fields[i+1]);
				tmp.put(fields[i], Double.parseDouble(fields[i+1]));
			}
			
			
			
			
			
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/t);
			
			fields=null;
			for(String dbpediaType: list){
				double score=computeJaccard(tokens.get(dbpediaType), tmp);
				if(score>threshold)
					out.println((counter+"y")+"\t"+freebaseType+"\t"+dbpediaType+"\t"+score);
				counter++;
			}
			tmp=null;
			//count++;
			
		}
		in.close();
		out.close();
	}
	
	//file2 should be the freebase consolidatedGlobalTokens file
	//outfile will contain tab-separated fields of the form:
	//freebase-type dbpedia-type jaccard-score
	//Note that generalized jaccard-score will always be strictly above threshold.
	public void printTFSimilarityFile(String file2, String outfile, double threshold)throws IOException{
		
		/*
		 * normalize tokens using norm2
		 */
		HashSet<String> types=new HashSet<String>(tokens.keySet());
		for(String type: types){
			HashMap<String, Double> tmp=tokens.get(type);
			double t=0.0;
			for(String m: tmp.keySet())
				t+=Math.pow(tmp.get(m),2);
			
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/Math.sqrt(t));
			
			tokens.put(type, tmp);
		}
		
		Scanner in=new Scanner(new FileReader(file2));
		PrintWriter out=new PrintWriter(new File(outfile));
		ArrayList<String> list=new ArrayList<String>(tokens.keySet());
		Collections.sort(list);
		int counter=1;
	//	int count=0;
		while(in.hasNextLine()//&&count<10
				){
			
			String line=in.nextLine();
			String[] fields=line.split("\t");
			String freebaseType=fields[0];
			
			HashMap<String,Double> tmp=new HashMap<String,Double>(30000);
			double t=0.0;
			
					for(int i=1; i<fields.length; i+=2){
						t+=Math.pow(Double.parseDouble(fields[i+1]),2);
						tmp.put(fields[i], Double.parseDouble(fields[i+1]));
					}
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/Math.sqrt(t));
			
			fields=null;
			for(String dbpediaType: list){
				double score=computeTF(tokens.get(dbpediaType), tmp);
				if(score>threshold)
					out.println((counter+"y")+"\t"+freebaseType+"\t"+dbpediaType+"\t"+score);
				counter++;
			}
			tmp=null;
			//count++;
			
		}
		in.close();
		out.close();
	}

	//file2 should be the freebase consolidatedGlobalTokens file
	//outfile will contain tab-separated fields of the form:
	//freebase-type dbpedia-type jaccard-score
	//Note that generalized jaccard-score will always be strictly above threshold.
	public void printLogTFSimilarityFile(String file2, String outfile, double threshold)throws IOException{
		
		/*
		 * normalize tokens using norm2
		 */
		HashSet<String> types=new HashSet<String>(tokens.keySet());
		for(String type: types){
			HashMap<String, Double> tmp=tokens.get(type);
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			double t=0.0;
			for(String m: set){
				double f=Math.log(tmp.get(m)+1.0);
				t+=Math.pow(f,2);
				tmp.put(m, f);
			}
			
			
			for(String k: set)
				tmp.put(k, tmp.get(k)/Math.sqrt(t));
			
			tokens.put(type, tmp);
		}
		
		Scanner in=new Scanner(new FileReader(file2));
		PrintWriter out=new PrintWriter(new File(outfile));
		ArrayList<String> list=new ArrayList<String>(tokens.keySet());
		Collections.sort(list);
		int counter=1;
	//	int count=0;
		while(in.hasNextLine()//&&count<10
				){
			
			String line=in.nextLine();
			String[] fields=line.split("\t");
			String freebaseType=fields[0];
			
			HashMap<String,Double> tmp=new HashMap<String,Double>(30000);
			double t=0.0;
			
					for(int i=1; i<fields.length; i+=2){
						t+=Math.pow(Double.parseDouble(fields[i+1]),2);
						tmp.put(fields[i], Double.parseDouble(fields[i+1]));
					}
			HashSet<String> set=new HashSet<String>(tmp.keySet());
			for(String k: set)
				tmp.put(k, tmp.get(k)/Math.sqrt(t));
			
			fields=null;
			for(String dbpediaType: list){
				double score=computeTF(tokens.get(dbpediaType), tmp);
				if(score>threshold)
					out.println((counter+"y")+"\t"+freebaseType+"\t"+dbpediaType+"\t"+score);
				counter++;
			}
			tmp=null;
			//count++;
			
		}
		in.close();
		out.close();
	}

	public static double computeJaccard(HashMap<String,Double> map1, HashMap<String,Double> map2){
		
		
		double union=generalizedUnion(map1, map2);
		if(union==0.0){
			System.out.println("Problems: union is 0");
			return -1.0;
		}
		double intersection=generalizedIntersection(map1, map2);
		
		return intersection/union;
	}

	public static double generalizedUnion(HashMap<String,Double> map1, HashMap<String,Double> map2){
		double result=0.0;
		for(String t: map1.keySet())
			if(!map2.containsKey(t))
				result+=map1.get(t);
			else{
				double k=map2.get(t);
				if(k<map1.get(t))
					result+=map1.get(t);
				else
					result+=k;
			}
		return result;
	}
	
	public static double generalizedIntersection(HashMap<String,Double> map1, HashMap<String,Double> map2){
		double result=0.0;
		for(String t: map1.keySet())
			if(map2.containsKey(t))
				{
				double k=map2.get(t);
				if(k>map1.get(t))
					result+=map1.get(t);
				else
					result+=k;
			}
		return result;
	}
	
	public static double computeTF(HashMap<String,Double> map1, HashMap<String,Double> map2){
		
		double result=0.0;
		
		for(String k:map1.keySet()){
			if(map2.containsKey(k))
				result+=(map1.get(k)*map2.get(k));
		}
		
		return result;
		
	}
	
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\";
	static String dbpediaFile=path+"dbpedia\\consolidatedGlobalTokens";
	static String freebaseFile=path+"freebase\\combinedConsolidatedTokens";
	static String outfile=path+"type-experiments\\f-db-gjaccard-norm1-WithZeros.txt";
	
	public static void main(String[] args)throws IOException{
		long time=(System.currentTimeMillis());
		GeneralizedJaccard test=new GeneralizedJaccard(dbpediaFile, true);
		test.printSimilarityFile(freebaseFile, outfile, -1.0);
		double t=(1.0*(System.currentTimeMillis()-time))/60000;
		System.out.println("Minutes elapsed: "+t);
		
		
		
	}
}
