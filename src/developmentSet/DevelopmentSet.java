package developmentSet;

import java.io.*;
import java.util.*;


public class DevelopmentSet {

	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\sameAs\\";
	static String[] tokenizer={"/", ",", ":", ";", "\\(", "\\)", "\\.", 
				"\"", "'","<",">", "_", "-", "#", "\\\\", "\\s+"};		
	
	public static void main(String[] args)throws IOException {
	//	test();
		//printNDuplicates(path+"freebaseDbpediaSameAsAppend", 
			//	path+"500duplicates.txt",500);
	
			testParseJSONMethods(path+"5000NonDuplicates.txt");
		//printNNonDuplicates(path+"500duplicates.txt", 
			//		path+"5000NonDuplicates.txt",5000);

	}
	
	/*a scratchpad tester. We run the duplicates and non-duplicates files through
	 * parseJSON (after splitting) to ensure no parsing errors. Already, we've 
	 * detected a couple of bugs by doing this. Next, we'll print out
	 * the output of parseJSONIntoStringFeatures to see whether the method behaves
	 * as expected.
	 */
	public static void testParseJSONMethods(String pairsFile)throws IOException{
		Scanner in=new Scanner(new FileReader(pairsFile));
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t\\{\t|\t\\}");
		//	System.out.println(line);
			String freebase=fields[1];
		//	System.out.println(freebase);
			String dbpedia=fields[3];
		//	System.out.println(dbpedia);
			ArrayList<HashMap<String, Integer>> db=parseJSONIntoStringFeatures(dbpedia.split("\t"));
			ArrayList<HashMap<String, Integer>> fb=parseJSONIntoStringFeatures(freebase.split("\t"));
			ArrayList<HashSet<String>> preppedDB=prepForAlphaJaccard(db);
			ArrayList<HashSet<String>> preppedFB=prepForAlphaJaccard(fb);
			ArrayList<Double> features=extractJaccardFeatures(preppedDB, preppedFB);
			System.out.println(features);
			break;
		}
		
		in.close();
	}
	
	/**
	 * Send in a duplicates/non-duplicates file, and get a list of instances
	 * Last value in each inner arraylist will be the class-value (1.0 for
	 * duplicates, 0.0 for non-duplicates). Count should either be -1, or
	 * the number of instances that should be extracted
	 * @param pairsFile
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<Double>> extractInstances(String pairsFile, int count, double classValue)throws IOException{
		ArrayList<ArrayList<Double>> result=null;
		if(count!=-1)
			result=new ArrayList<ArrayList<Double>>(count);
		else{
			count=Integer.MAX_VALUE;
			result=new ArrayList<ArrayList<Double>>();
		}
		Scanner in=new Scanner(new FileReader(pairsFile));
		int n=0;
		while(in.hasNextLine() && n<count){
			String line=in.nextLine();
			String[] fields=line.split("\t\\{\t|\t\\}");
		
			ArrayList<Double> instance=
					(extractJaccardFeatures(
						prepForAlphaJaccard(
							parseJSONIntoStringFeatures(fields[3].split("\t"))), 
						prepForAlphaJaccard(
							parseJSONIntoStringFeatures(fields[1].split("\t")))));
			instance.add(classValue);
			result.add(instance);
			n++;
		}
		
		in.close();
		return result;
	}
	
	//instances of b will be added to a. Make sure to set b to null after calling this.
	public static void mergeInstances(ArrayList<ArrayList<Double>> a, ArrayList<ArrayList<Double>> b){
		for(ArrayList<Double> k: b)
			a.add(k);
	}
	
	public static ArrayList<Double> extractJaccardFeatures(ArrayList<HashSet<String>> preppedDB, ArrayList<HashSet<String>> preppedFB){
		ArrayList<Double> features=new ArrayList<Double>();
		for(int i=0; i<preppedDB.size(); i++)
			for(int j=0; j<preppedFB.size(); j++){
				features.add(similarities.Jaccard.computeJaccard(preppedDB.get(i), preppedFB.get(j)));
			}
		return features;
	}
	
	public static ArrayList<HashSet<String>> prepForAlphaJaccard(ArrayList<HashMap<String,Integer>> list){
		ArrayList<HashSet<String>> preppedList=new ArrayList<HashSet<String>>();
		for(HashMap<String,Integer> map:list){
			Set<String> keys=map.keySet();
			HashSet<String> tmp=new HashSet<String>();
			for(String key:keys)
				if(isAlphabeticOnly(key))
					tmp.add(key);
			preppedList.add(tmp);
		}
		return preppedList;
	}
	//we've made this foolproof and tested it.
	private static boolean isAlphabeticOnly(String key){
		boolean result=true;
		String lc=key.toLowerCase();
		String uc=key.toUpperCase();
		for(int i=0; i<key.length(); i++)
			if(lc.charAt(i)==(uc.charAt(i)))
				return false;
		return result;
	}
	
	
	/**
	 * ArrayList will contain exactly k hashmaps. Each map is a token together with
	 * a count of  how many times it occurs in all property contexts (multiple occurrences within
	 * the same property context are only counted once).
	 * 
	 * It's a good idea to call parseJSON first to ensure there are no unusual errors.
	 * 
	 * First hashmap contains property tokens (P)
	 * Second hashmap contains tokens in literals (L)
	 * Third hashmap contains non-property and non-literal tokens (O).
	 * Fourth hashmap contains the union of the three hashmaps (with summation over
	 * common elements) (A)
	 */
	public static ArrayList<HashMap<String, Integer>> parseJSONIntoStringFeatures(String[] json){
		ArrayList<HashMap<String, Integer>> result=new ArrayList<HashMap<String, Integer>>(3);
		result.add(new HashMap<String,Integer>());
		result.add(new HashMap<String,Integer>());
		result.add(new HashMap<String,Integer>());
		
		for(String propValues: json){
			String[] attributes=propValues.split("\":\\[");
			if(attributes.length!=2)
				{
				System.out.println("Incorrect parsing");
				System.out.println(propValues);
				continue;
				}
			//first, let's deal with P tokens
			String prop=attributes[0];
			prop=prop.replaceAll("\"", "");
			for(String t: tokenizer)
				prop=prop.replaceAll(t, " ").trim();
			String[] propTokens=prop.split(" ");
			HashSet<String> propTokensSet=new HashSet<String>();
			for(String propToken: propTokens)
				propTokensSet.add(propToken);
			for(String propToken: propTokensSet)
			{
				if(!result.get(0).containsKey(propToken))
					result.get(0).put(propToken, 0);
				int g=result.get(0).get(propToken);
				result.get(0).put(propToken, g+1);
			}
			
			//now let's deal with L and O tokens
			String values=attributes[1];
			
			
			
			values=values.replaceAll("\\]","");
			String[] list=values.split("\", \"");
			for(int j=0; j<list.length; j++){
				//is it a literal?
				boolean literal=false;
				if(list[j].length()>1&&list[j].substring(0, 1).equals("\"")&&
						list[j].substring(list[j].length()-1, list[j].length()).equals("\""))
					literal=true;
				
				for(String t: tokenizer)
					list[j]=list[j].replaceAll(t, " ").trim();
				
				String[] tokens=list[j].split(" ");
				HashSet<String> tokensSet=new HashSet<String>();
				for(String token: tokens)
					tokensSet.add(token);
				if(literal){
					for(String token: tokensSet)
					{
						if(!result.get(1).containsKey(token))
							result.get(1).put(token, 0);
						int g=result.get(1).get(token);
						result.get(1).put(token, g+1);
					}
				}
				else{
					for(String token: tokensSet)
					{
						if(!result.get(2).containsKey(token))
							result.get(2).put(token, 0);
						int g=result.get(2).get(token);
						result.get(2).put(token, g+1);
					}
				}
			}
		}
		//finally, we'll deal with A tokens. Note that this has not been added yet.
		HashMap<String,Integer> tmp=new HashMap<String,Integer>();
		for(int i=0; i<result.size(); i++){
			HashMap<String,Integer> k=result.get(i);
			for(String key: k.keySet()){
				if(!tmp.containsKey(key))
					tmp.put(key, 0);
				int num=tmp.get(key);
				tmp.put(key, num+k.get(key));
			}
		}
		result.add(tmp);
		return result;
	}
	
	/**
	 * Code is derived mostly from typeTokenStatistics.ExtractGlobalTokenStatistics
	 * and ConsolidateGlobalTokens. We've supplemented the tokenizer. Remember to
	 * split each value in HashSet<String> by space when tokenizing (after returning
	 * from this function)
	 * @param json
	 * @return
	 */
	public static HashMap<String, HashSet<String>> parseJSON(String[] json){
		HashMap<String, HashSet<String>> result=new HashMap<String, HashSet<String>>();
		for(String propValues: json){
			String[] attributes=propValues.split("\":\\[");
			if(attributes.length!=2)
				{
				System.out.println("Incorrect parsing");
				System.out.println(propValues);
				continue;
				}
			String prop=attributes[0];
			prop=prop.replaceAll("\"", "");
			String values=attributes[1];
			if(result.containsKey(prop))
			{
				System.out.println("property repeats");
				System.out.println(propValues);
				continue;
				}
			result.put(prop, new HashSet<String>());
			
			if(!values.contains("]"))
			{
				System.out.println("Square bracket missing");
				System.out.println(propValues);
				continue;
				}
			values=values.replaceAll("\\]","");
			String[] list=values.split("\", \"");
			for(int j=0; j<list.length; j++){
				for(String t: tokenizer)
					list[j]=list[j].replaceAll(t, " ").trim();
				result.get(prop).add(list[j]);
			}
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private static void test()throws IOException{
		String duplicatesFile=path+"500duplicates.txt";
		Scanner in=new Scanner(new FileReader(duplicatesFile));
		String line=null;
		if(in.hasNextLine())
			line=in.nextLine();
		
		in.close();
		
		//testing on the line
		System.out.println(line);
		String[] fields=line.split("\\{|\\}");
		System.out.println(fields.length);
		System.out.println(fields[0]);
		System.out.println(fields[1]);
		System.out.println(fields[2]);
	}
	
	/*
	 * send the duplicates file into this, but make sure to do extensive testing first.
	 */
	public static void printNNonDuplicates(String duplicatesFile, String outfile, int n)throws IOException{
		ArrayList<String> dbpedia=new ArrayList<String>(500);
		ArrayList<String> freebase=new ArrayList<String>(500);
		//dbpedia entities referring to freebase entities
		HashMap<String, HashSet<String>> dbpediaIndex=new HashMap<String, HashSet<String>>();
		Scanner in=new Scanner(new FileReader(duplicatesFile));
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t\\{\t|\t\\}\t");
			if(fields.length!=4){
				System.out.println(line);
				System.out.println(fields.length);
				continue;
			}
			String f=fields[1].trim();
			freebase.add(f);
			String d=fields[3].trim();
			dbpedia.add(d);
			if(!dbpediaIndex.containsKey(d))
				dbpediaIndex.put(d, new HashSet<String>());
			dbpediaIndex.get(d).add(f);
			
		}
		in.close();
		
		int count=0;
		PrintWriter out=new PrintWriter(new File(outfile));
		Random r=new Random(4);	//don't make this non-deterministic for now.
		long i=-1;
		while(count<n){
			int k1=r.nextInt(dbpedia.size());
			int k2=r.nextInt(freebase.size());
			if(dbpediaIndex.get(dbpedia.get(k1)).contains(freebase.get(k2)))
				continue;
			out.println(Long.toString(i)+"x\tfreebase-instance\t{\t"+freebase.get(k2)+
					"\t}\tdbpedia-instance\t{\t"+dbpedia.get(k1)+"\t}");
			i--;
			count++;
		}
		out.close();
		
	}
	
	
	
	public static void printNDuplicates(String infile, String outfile, int n)throws IOException{
		Scanner in=new Scanner(new FileReader(infile));
		PrintWriter out=new PrintWriter(new File(outfile));
		int count=0;
		while(in.hasNextLine()&&count<n){
			out.println(in.nextLine());
			count++;
		}
		in.close();
		out.close();
	}

}
