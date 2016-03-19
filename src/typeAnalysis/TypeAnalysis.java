package typeAnalysis;

import java.io.*;
import java.util.*;

import datasetDetails.Curate;

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
	 * <idy>[tab]<freebase-type>[tab]<dbpedia-type>[tab]<double-score>
	 * The data written in the outfile is of form:
	 * recall,precision,f-measure 
	 * (in decimals, not percentage). There is no header either. 
	 * The multiplicative factor is needed mainly because of low scores in tf-idf. The
	 * default is 100.
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
	
	/**
	 * This function is very similar to printRankedCSVFile except that it also uses a csvFile
	 * (originally generated by printRankedCSVFile) and labeled by us. It's to sync
	 * the various ground-truths so we don't inadvertently end up being inconsistent.
	 * 
	 * 
	 * @param rankedFile The original ranked file of the kind printed out by convertToRankedList
	 * @param csvFile The csv file from which we are performing the derivation 
	 * @param derivedCsvFile The output file. It will have the same format as csvFile, but will contain M's and P's
	 * @param n The number of ranked dbpedia results to print per freebase type
	 * @throws IOException
	 
	public static void printDerivedRankedCSVFile(String rankedFile, String csvFile,String derivedCsvFile, int n)throws IOException{
		ArrayList<String> ordering=new ArrayList<String>(5000);
		HashMap<String, HashSet<String>> map=new HashMap<String, HashSet<String>>();	//this map has unusual semantics, beware!
		int count=100;	//since we only labeled 100 samples, we don't need to record every freebase type in the map
		boolean breached=false;
		
		
		Scanner in=new Scanner(new FileReader(csvFile));
		
		while(in.hasNextLine()){
			String[] fields=in.nextLine().split(",");
			ordering.add(fields[0]);
			count--;
			if(breached)
				continue;
			HashSet<String> tmp=new HashSet<String>();
			for(int i=2; i<=fields.length-2; i+=2)
				if(fields[i+1].length()!=0)
					tmp.add(fields[i]);
			if((tmp.size()==0&&fields[1].equals("P"))||
					(tmp.size()>0&&!fields[1].equals("P")))
				
					System.out.println("Error! P missing from "+fields[0]);
			else
				map.put(fields[0], tmp);
			if(count==0)
				breached=true;
		}
		in.close();
		
		
		//second phase
		HashMap<String, ArrayList<String>> freebaseDbpedia=new HashMap<String, ArrayList<String>>();
		in=new Scanner(new FileReader(rankedFile));
		
		
		while(in.hasNextLine()){
			String[] line=in.nextLine().split("\t");
			String[] dbfields=line[2].split("/");
			String[] fbfields=line[1].split("/");
			line[1]=fbfields[fbfields.length-1];
			line[2]=dbfields[dbfields.length-1];
			line[1]=line[1].substring(0, line[1].length()-1);
			line[2]=line[2].substring(0, line[2].length()-1);
					
			if(!freebaseDbpedia.containsKey(line[1])){
				ArrayList<String> k=new ArrayList<String>(n);
				for(int i=0; i<n; i++)
					k.add(new String(""));
				freebaseDbpedia.put(line[1], k);
			}
			int rank=Integer.parseInt(line[3]);
			if(rank>n)
				continue;
			
			String previous=freebaseDbpedia.get(line[1]).set(rank-1, line[2]);
			if(!previous.equals(""))
				System.out.println("Error! Duplicate rank "+rank+" for freebase entry"
						+line[1]+" and dbpedia entry "+previous);
			
				
		}
		
		in.close();
		PrintWriter out=new PrintWriter(new File(derivedCsvFile));
		for(String freebase: ordering){
			String res=freebase+",";
			if(map.containsKey(freebase))
				res+="P";
			res+=",";
			for(String dbpedia: freebaseDbpedia.get(freebase)){
				if(map.containsKey(freebase))
					if(map.get(freebase).contains(dbpedia)){
						res+=(dbpedia+",M,");
					}
				res+=(dbpedia+",,");
			}
			out.println(res.substring(0, res.length()-2));
		}
		out.close();
		
	}*/
	
	/*
	 * Take the ranked file generated by convertToRankedList, and then produces
	 * a comma-delimited csv that contains  up to 2*n+1 columns. The goal is to 
	 * produce an IR style ranking, where the first column contains the freebase
	 * type, and the next ten columns (with empty columns between them) contain
	 * (in ranked order) the top n dbpedia types. The empty columns are in place
	 * because we will label the files and use the labels in analysis.
	 * 
	 * Because of the difficulty of labeling long strings, we've done some
	 * preprocessing to make it easier. See printed file/code for details.
	 */
	public static void printRankedCSVFile(String rankedFile, String csvFile, int n)throws
	IOException{
		HashMap<String, ArrayList<String>> freebaseDbpedia=new HashMap<String, ArrayList<String>>();
		Scanner in=new Scanner(new FileReader(rankedFile));
		
		
		while(in.hasNextLine()){
			String[] line=in.nextLine().split("\t");
			String[] dbfields=line[2].split("/");
			String[] fbfields=line[1].split("/");
			line[1]=fbfields[fbfields.length-1];
			line[2]=dbfields[dbfields.length-1];
			line[1]=line[1].substring(0, line[1].length()-1);
			line[2]=line[2].substring(0, line[2].length()-1);
					
			if(!freebaseDbpedia.containsKey(line[1])){
				ArrayList<String> k=new ArrayList<String>(n);
				for(int i=0; i<n; i++)
					k.add(new String(""));
				freebaseDbpedia.put(line[1], k);
			}
			int rank=Integer.parseInt(line[3]);
			if(rank>n)
				continue;
			
			String previous=freebaseDbpedia.get(line[1]).set(rank-1, line[2]);
			if(!previous.equals(""))
				System.out.println("Error! Duplicate rank "+rank+" for freebase entry"
						+line[1]+" and dbpedia entry "+previous);
			
				
		}
		
		in.close();
		PrintWriter out=new PrintWriter(new File(csvFile));
		for(String freebase: freebaseDbpedia.keySet()){
			String res=freebase+",,";
			for(String dbpedia: freebaseDbpedia.get(freebase))
				res+=(dbpedia+",,");
			out.println(res.substring(0, res.length()-2));
		}
		out.close();
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
		/*printDbpediaTypeStatistics(Jaccard.path+"dbpedia\\curated_instance-types");
		convertToRankedList(Curate.path+"f-db-Logtf-norm2-WithZeros.txt",
				Curate.path+"f-db-Logtf-norm2-ranked.txt");*/
		printRankedCSVFile(Curate.path+"f-db-gjaccard-norm1-ranked.txt",
				Curate.path+"gjaccard-top10.csv",10);
		
		//max(Jaccard.path+"f-db-gjaccard-norm1-WithZeros.txt");
		/*
		TypeAnalysis gold=new TypeAnalysis(Jaccard.path+"type-experiments\\typeGoldStandard-dedup");
		gold.analyze(Jaccard.path+"type-experiments\\f-db-Logtf-norm2.txt", 
				Jaccard.path+"type-experiments\\f-db-Logtf-norm2-analysis.csv",100);*/
	}

}
