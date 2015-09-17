package datasetDetails;

import java.io.*;
import java.util.*;

public class TypeAnalysis {
	
	/**
	 * The goal of this file is to analyze the data we collect from type-matching
	 * experiments. As a first run, we will analyze the data gathered from using
	 * Jaccard on global tokens of types.
	 */
	
	
	//key is a freebase-type, the corresponding set contains matching dbpedia types
	HashMap<String, HashSet<String>> freebaseMap;
	int total=0;
	
	
	public TypeAnalysis(String goldFile)throws IOException{
		Scanner in=new Scanner(new FileReader(goldFile));
		freebaseMap=new HashMap<String, HashSet<String>>(5000);
		while(in.hasNextLine()){
			String[] fields=in.nextLine().toLowerCase().split("\t");
			if(!freebaseMap.containsKey(fields[0]))
				freebaseMap.put(fields[0], new HashSet<String>());
			freebaseMap.get(fields[0]).add(fields[1]);
			total++;
		}
		
		in.close();
	}
	
	/*
	 * Recall that infile was of the form:
	 * <freebase-type>[tab]<dbpedia-type>[tab]<double-score>
	 * The data written in the outfile is of form:
	 * recall,precision,f-measure 
	 * (in decimals, not percentage). There is no header either. 
	 * The multiplicative factor is needed mainly because of low scores in tf-idf. The
	 * current usage is: 100 for the jaccard evalulations, 1000 for tf-norm1 and 10,000 for tf-norm2
	 */
	public void analyze(String infile, String outfile, int multiplicativeFactor)throws IOException{
		double max=max(infile);
		int capacity=(int)Math.floor(max*multiplicativeFactor);
		
		int[] TP=new int[capacity+1];
		int[] FP=new int[capacity+1];
		
		Scanner in=new Scanner(new FileReader(infile));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			int index=(int)Math.floor(Double.parseDouble(fields[3])*multiplicativeFactor);
			if(freebaseMap.containsKey(fields[1])){
				if(freebaseMap.get(fields[1]).contains(fields[2]))
					TP[index]++;
				else
					FP[index]++;
			}else
				FP[index]++;
			
		}
		in.close();
		
		updateArray(TP);
		updateArray(FP);
		PrintWriter out=new PrintWriter(new File(outfile));
		for(int i=0; i<TP.length; i++){
			double recall=(1.0*TP[i])/total;
			double precision=(1.0*TP[i])/(TP[i]+FP[i]);
			double fm=0.0;
			if(recall+precision>0.0)
				fm=2*precision*recall/(precision+recall);
			out.println(recall+","+precision+","+fm);
		}
		out.close();
	}
	
	private void updateArray(int[] array){
		for(int i=array.length-1; i>0; i--)
			array[i-1]=array[i-1]+array[i];
	}
	
	
	public static void printTypeStatistics(String curatedTypesFile, String outfile)throws IOException{
		
		Scanner in=new Scanner(new FileReader(curatedTypesFile));
		HashMap<String, Long> typesCount=new HashMap<String, Long>(700);
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			if(!typesCount.containsKey(fields[2]))
				typesCount.put(fields[2], (long)0);
			long p=typesCount.get(fields[2]);
			typesCount.put(fields[2], p+1);
		}
		in.close();
		
		PrintWriter out=new PrintWriter(new File(outfile));
		long total=0;
		for(String type: typesCount.keySet()){
			long q=typesCount.get(type);
			total+=q;
			out.println(type+"\t"+q);
		}
		out.println("TOTAL\t"+total);
		out.close();
		
		
	}

	public static double max(String infile)throws IOException{
		double max=-1.0;
		Scanner in=new Scanner(new FileReader(infile));
		while(in.hasNextLine()){
			String line=in.nextLine();
			try{
			double score=Double.parseDouble(line.split("\t")[3]);
			if(score>max)
				max=score;
			}catch(Exception e){System.out.println("offending line: "+line); continue;}
		}
		
		in.close();
		System.out.println("max score found: "+max);
		return max;
	}
	
	/*
	 * Prints all the lines in input file to outfile, but with TOTAL at the end
	 */
	public static void appendTotalToFreebaseTypesStatisticsFile(String freebaseTSFile, String outfile)throws IOException{
		long total=0;
		PrintWriter out=new PrintWriter(new File(outfile));
		Scanner in=new Scanner(new FileReader(freebaseTSFile));
		while(in.hasNextLine()){
			String line=in.nextLine();
			total+=(Long.parseLong(line.split("\t")[1]));
			out.println(line);
		}
		
		in.close();
		out.println("TOTAL\t"+total);
		out.close();
		
	}
	
	public static void printDbpediaTypeStatistics(String curatedTypesFile)throws IOException{
		
		Scanner in=new Scanner(new FileReader(curatedTypesFile));
		HashMap<String, Long> typesCount=new HashMap<String, Long>(700);
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			if(!typesCount.containsKey(fields[2]))
				typesCount.put(fields[2], (long)0);
			long p=typesCount.get(fields[2]);
			typesCount.put(fields[2], p+1);
		}
		in.close();
		
		
		long total=0;
		long dbpediaOrgTotal=0;
		int count=0;
		for(String type: typesCount.keySet()){
			long q=typesCount.get(type);
			total+=q;
			if(type.contains("dbpedia.org/ontology")){
				dbpediaOrgTotal+=q;
				count++;
			}
			
		}
		System.out.println("Total type-covered entities\t"+total);
		System.out.println("Total covered entities by dbpedia.org/ontology types:\t"+dbpediaOrgTotal);
		System.out.println("Total number of types\t"+typesCount.keySet().size());
		System.out.println("Total number of dbpedia.org/ontology types\t"+count);
	}
	
	/*take one of the f-db-* files and convert to ranked list of the form
	*
	*<idy>[tab]<freebase-type>[tab]<dbpedia-type>[tab]<rank>
	*where rank is an integer. Ties are broken arbitrarily.
	*
	*Should only be used with one of the withZeros file, where pairs are exhaustively
	*published. We do a mod 417 to switch types.
	*/
	public static void convertToRankedList(String scoreFile, String rankedFile)
	throws IOException{
		HashMap<Double, HashSet<String>> scoreList=new HashMap<Double, HashSet<String>>(700);
		Scanner in=new Scanner(new FileReader(scoreFile));
		PrintWriter out=new PrintWriter(new File(rankedFile));
		String freebase=null;
		while(in.hasNextLine()){
			String[] line=in.nextLine().split("\t");
			int id=Integer.parseInt(line[0].substring(0, line[0].length()-1));
			if(id%417==1 && id!=1){
				ArrayList<Double> list=new ArrayList<Double>(scoreList.keySet());
				Collections.sort(list);
				int rank=417;
				for(double score: list){
					for(String d: scoreList.get(score)){
						String[] l=d.split("\t");
						out.println(l[0]+"\t"+freebase+"\t"+l[1]+"\t"+rank);
						rank--;
					}
				}
				if(rank!=0)
					System.out.println("Error! Fewer than 417 lines written");
				scoreList=new HashMap<Double, HashSet<String>>(450);
			}
			double score=Double.parseDouble(line[3]);
			if(!scoreList.containsKey(score))
				scoreList.put(score, new HashSet<String>());
			scoreList.get(score).add(line[0]+"\t"+line[2]);
			freebase=line[1];
		}
		in.close();
		ArrayList<Double> list=new ArrayList<Double>(scoreList.keySet());
		Collections.sort(list);
		int rank=417;
		for(double score: list){
			for(String d: scoreList.get(score)){
				String[] l=d.split("\t");
				out.println(l[0]+"\t"+freebase+"\t"+l[1]+"\t"+rank);
				rank--;
			}
		}
		if(rank!=0)
			System.out.println("Error! Fewer than 417 lines written");
		scoreList=new HashMap<Double, HashSet<String>>(450);
		out.close();
	}
	
	public static void main(String[] args)throws IOException{
		//printDbpediaTypeStatistics(Jaccard.path+"dbpedia\\curated_instance-types");
		convertToRankedList(Curate.path+"f-db-Logtf-norm2-WithZeros.txt",
				Curate.path+"f-db-Logtf-norm2-ranked.txt");
		
		//max(Jaccard.path+"f-db-gjaccard-norm1-WithZeros.txt");
		/*
		TypeAnalysis gold=new TypeAnalysis(Jaccard.path+"type-experiments\\typeGoldStandard-dedup");
		gold.analyze(Jaccard.path+"type-experiments\\f-db-Logtf-norm2.txt", 
				Jaccard.path+"type-experiments\\f-db-Logtf-norm2-analysis.csv",100);*/
	}

}
