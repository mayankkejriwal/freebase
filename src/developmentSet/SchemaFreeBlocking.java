package developmentSet;

import java.io.*;
import java.util.*;

public class SchemaFreeBlocking {

	/**
	 * For development, assume everything fits in memory. At present, this is designed for
	 * dbpedia-freebase. If using for census make necessary changes (don't consider the
	 * subject field; also, maybe we should consider non-alpha words. These considerations
	 * may not be relevant, since we're using the TSG mapper for the blocker (for census)
	 */
	int purgeThreshold;	//allow only up to this many entities/dataset, but don't discard if it
	//exceeds that
	
	//we'll use one of the four 'token-sets' generated in DevelopmentSet; to determine
	//which one, run evaluations, using the index. we'll also only consider alpha-words.
	
	HashMap<String, HashSet<String>> blocks1;
	HashMap<String, HashSet<String>> blocks2;
	HashMap<String, HashSet<String>> candidateSet;
	long candidateSetSize=0;
	
	int numInstances1=0;
	int numInstances2=0;
	
	
	static String file1=DevelopmentSet.path+"freebase.txt";
	static String file2=DevelopmentSet.path+"dbpedia.txt";
	static String gold=DevelopmentSet.path+"gold.txt";
	
	public static void main(String[] args)throws IOException{
		writeMetricsToFile(DevelopmentSet.path+"schema-free-blocking\\index-1.csv",1);
		writeMetricsToFile(DevelopmentSet.path+"schema-free-blocking\\index-2.csv",2);
		writeMetricsToFile(DevelopmentSet.path+"schema-free-blocking\\index-3.csv",3);
	}
	
	/*
	 * The files are instance files; at present index must be between 0 and 3 (inclusive)
	 */
	public SchemaFreeBlocking(String file1, String file2, int index, int purgeThreshold)throws IOException{
		this.purgeThreshold=purgeThreshold;
		blocks1=new HashMap<String, HashSet<String>>();
		Scanner in=new Scanner(new FileReader(file1));
		while(in.hasNextLine()){
			numInstances1++;
			String l=in.nextLine();
			String[] line=l.split("\t");
			HashSet<String> bkvs=DevelopmentSet.retainAlphaTokens(
					DevelopmentSet.parseJSONIntoStringFeatures(line).get(index));
			for(String bkv: bkvs){
				if(!blocks1.containsKey(bkv))
					blocks1.put(bkv, new HashSet<String>());
				if(blocks1.get(bkv).size()<=purgeThreshold)
					blocks1.get(bkv).add(DevelopmentSet.extractSubjectFromInstance(l));
			}
		}
		in.close();
		
		blocks2=new HashMap<String, HashSet<String>>();
		in=new Scanner(new FileReader(file2));
		while(in.hasNextLine()){
			numInstances2++;
			String l=in.nextLine();
			String[] line=l.split("\t");
			HashSet<String> bkvs=DevelopmentSet.retainAlphaTokens(
					DevelopmentSet.parseJSONIntoStringFeatures(line).get(index));
			for(String bkv: bkvs){
				if(!blocks1.containsKey(bkv))
					continue;
				if(!blocks2.containsKey(bkv))
					blocks2.put(bkv, new HashSet<String>());
				if(blocks2.get(bkv).size()<=purgeThreshold)
					blocks2.get(bkv).add(DevelopmentSet.extractSubjectFromInstance(l));
			}
		}
		in.close();
		
		Set<String> tmp=new HashSet<String>(blocks1.keySet());
		for(String k: tmp)
			if(!blocks2.containsKey(k))
				blocks1.remove(k);
		
		buildPairs();
	}
	/*
	 * Build candidate set, and also update candidateSetSize
	 */
	private void buildPairs(){
		candidateSet=new HashMap<String, HashSet<String>>();
		for(String k: blocks1.keySet()){
			HashSet<String> b1=blocks1.get(k);
			HashSet<String> b2=blocks2.get(k);
			for(String entity: b1){
				if(!candidateSet.containsKey(entity))
					candidateSet.put(entity, new HashSet<String>());
				for(String entity2: b2)
					candidateSet.get(entity).add(entity2);
					
				
			}
		}
		
		for(String k: candidateSet.keySet())
			candidateSetSize+=(candidateSet.get(k).size());
	}
	
	/*
	 * Will return RR, PC and F-measure. We don't really need the goldFile to compute RR
	 */
	public double[] calculateMetrics(String goldFile)throws IOException{
		double[] results=new double[3];
		int total=0;
		int correct=0;
		Scanner in=new Scanner(new FileReader(goldFile));
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split("\t");
			total++;
			if(candidateSet.containsKey(fields[0]))
				if(candidateSet.get(fields[0]).contains(fields[1]))
					correct++;
		}
		in.close();
		results[0]=1.0-(1.0*candidateSetSize/(numInstances1*numInstances2));
		results[1]=1.0*correct/total;
		if(results[1]+results[0]>0.0)
			results[2]=2*results[1]*results[0]/(results[1]+results[0]);
		return results;
	}
	
	public static void writeMetricsToFile(String csvFile, int index)throws IOException{
		PrintWriter out=new PrintWriter(new File(csvFile));
		out.println("purgeThreshold,RR,PC,FM");
		for(int PT=5; PT<=100; PT+=5){
			System.out.println("Current Purge Threshold is "+PT);
			SchemaFreeBlocking test=new SchemaFreeBlocking(file1, file2, index, PT);
			double[] results=test.calculateMetrics(gold);
			out.println(test.purgeThreshold+","+results[0]+","+results[1]+","+results[2]);
		}
		out.close();
	}
}
