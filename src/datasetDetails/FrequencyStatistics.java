package datasetDetails;

import java.io.*;
import java.util.*;

public class FrequencyStatistics {

	/**
	 * This class is designed to take a consolidatedTokens file and return frequency
	 * statistics of tokens over ALL types. We will consider ten bins in increments of 0.1
	 * (i.e. (0.0, 0.1], (0.1, 0.2]...(0.9,1.0]) and print out the total number of tokens
	 * in the overall file corresponding to each bin.  We will also use the class to compute
	 * document-frequencies of tokens, where a document corresponds to a type.
	 * 
	 * 
	 * @param args
	 */
	
	long[] slots=new long[10];	//record the bin counts (see header-note)
	
	//if type is true, document frequencies will be computed, otherwise all-types frequency
	//statistics will be computed.
	public FrequencyStatistics(String file, boolean type)throws IOException{
		if(!type){
			Scanner in=new Scanner(new FileReader(file));
			while(in.hasNextLine()){
				String line=in.nextLine();
				String[] fields=line.split("\t");
				HashMap<String, Double> tmp=new HashMap<String, Double>(30000);
				double t=0.0;
				for(int i=1; i<fields.length; i+=2){
					t+=Double.parseDouble(fields[i+1]);
					tmp.put(fields[i], Double.parseDouble(fields[i+1]));
				}
				
				for(String k: tmp.keySet()){
					double m=tmp.get(k)/t;
					int index=(int)Math.floor(m*10);
					if(index>10){
						System.out.println("Problem! Faulty index: "+index);
					}else if(index==10)
						index=9;
					slots[index]=slots[index]+1;
				}
			}
			in.close();
		}
		else{
			Scanner in=new Scanner(new FileReader(file));
			int total=0;
			HashMap<String, Double> counts=new HashMap<String, Double>();
			while(in.hasNextLine()){
				String line=in.nextLine();
				total++;
				String[] fields=line.split("\t");
				for(int i=1; i<fields.length; i+=2){
					if(!counts.containsKey(fields[i]))
						counts.put(fields[i], 0.0);
					double q=counts.get(fields[i]);
					q++;
					counts.put(fields[i], q);
					
				}
			}
			System.out.println("Total types: "+total);
			for(String k:counts.keySet()){
				double m=counts.get(k)/total;
				int index=(int)Math.floor(m*10);
				if(index>10){
					System.out.println("Problem! Faulty index: "+index+" of token "+k);
					
					index=9;
				}else if(index==10)
					index=9;
				slots[index]=slots[index]+1;
			}
			in.close();
		}
	}
	
	public void printSlots(){
		double[] intervals={0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		for(int i=0; i<10; i++){
			System.out.println("("+intervals[i]+","+intervals[i+1]+"] : "+slots[i]);
		}
	}
	
	static String dbpediaTokens="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\dbpedia\\consolidatedGlobalTokens";
	static String freebaseTokens="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\freebase\\combinedConsolidatedTokens";
	public static void main(String[] args)throws IOException {
		new FrequencyStatistics(dbpediaTokens, true).printSlots();

	}

}
