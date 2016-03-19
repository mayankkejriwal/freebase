package evalsAndStatistics;

import java.io.*;
import java.util.*;

public class ComputeRR {
/**
 * 
 */
	
	HashMap<String, Long> freebase;
	HashMap<String, Long> dbpedia;
	
	
	/*
	 * Be careful about excluding the 'TOTAL' line. 
	 * if dborg is true, will only consider those dbpedia types that have dbpedia.org/ontology
	 */
	public ComputeRR(String freebaseTypesStatistics, String dbpediaTypesStatistics, 
			boolean dborg)throws IOException{
		freebase=new HashMap<String, Long>(5000);
		dbpedia=new HashMap<String, Long>(700);
		Scanner in=new Scanner(new FileReader(freebaseTypesStatistics));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			if(fields[0].equals("TOTAL"))
				break;
			else
				freebase.put(fields[0], Long.parseLong(fields[1]));
		}
		in.close();
		
		in=new Scanner(new FileReader(dbpediaTypesStatistics));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			if(fields[0].equals("TOTAL"))
				break;
			else if(dborg){
				if(fields[0].contains("dbpedia.org/ontology"))
				dbpedia.put(fields[0], Long.parseLong(fields[1]));
			}else{
				dbpedia.put(fields[0], Long.parseLong(fields[1]));
			}
		}
		in.close();
	}
	/*
	 * scoreFile is typically an f-db-* file. We allow taking multiple thresholds
	 * to avoid multiple runs. Note that in reality, this will only print the number
	 * of pairs generated (non-unique). The RR will have to be computed manually. See analysis files.
	 */
	public void printScoreRRs(String scoreFile, double[] thresholds)throws IOException{
		long[] pairs=new long[thresholds.length];
		Scanner in=new Scanner(new FileReader(scoreFile));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			double score=Double.parseDouble(fields[3]);
			for(int i=0; i<thresholds.length; i++){
				if(score<=thresholds[i])
					continue;
				long f=freebase.get(fields[1]);
				long d=dbpedia.get(fields[2]);
				pairs[i]+=(f*d);
			}
		}
		in.close();
		for(int i=0; i<thresholds.length; i++)
			System.out.println(pairs[i]);
			//System.out.println("Threshold\t"+thresholds[i]+"\tPairs Generated\t"+pairs[i]);
		
	}
	/*
	 * rankFile is typically an f-db-* file. We allow taking multiple ranks
	 * to avoid multiple runs. Note that in reality, this will only print the number
	 * of pairs generated (non-unique). The RR will have to be computed manually. See analysis files.
	 */
	public void printRankRRs(String rankFile, int[] ranks)throws IOException{
		long[] pairs=new long[ranks.length];
		Scanner in=new Scanner(new FileReader(rankFile));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			int rank=Integer.parseInt(fields[3]);
			for(int i=0; i<ranks.length; i++){
				if(rank>ranks[i])
					continue;
				long f=freebase.get(fields[1]);
				long d=dbpedia.get(fields[2]);
				pairs[i]+=(f*d);
			}
		}
		in.close();
		for(int i=0; i<ranks.length; i++)
			System.out.println(pairs[i]);
			//System.out.println("Rank\t"+ranks[i]+"\tPairs Generated\t"+pairs[i]);
		
	}
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\type-experiments\\"; 
	
	public static void main(String[] args)throws IOException{
		ComputeRR r=new ComputeRR(path+"experimental-data\\freebaseTypeStatistics.txt",
				path+"experimental-data\\dbpediaTypeStatistics.txt",
				true);
		
	/*	int[] ranks={1,2,3,5,10,50,100,200};
		r.printRankRRs(path+"derived-data-files\\score-rank-files\\"
				+ "f-db-Logtf-norm2-ranked.txt", ranks);
		*/
		
		double[] thresholds={0.0,0.05,0.1,0.15,0.2,0.3};
		r.printScoreRRs(path+"derived-data-files\\score-rank-files\\"
				+ "f-db-jaccard-WithZeros.txt", thresholds);
	}
}
