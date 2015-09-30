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
		buildGoldFile(path+"freebase.txt",path+"dbpedia.txt",path+"gold.txt");
			//splitDuplicatesFileIntoInstanceFiles(path+"weka-experiments\\500duplicates.txt");
		//printNNonDuplicates(path+"500duplicates.txt", 
			//		path+"5000NonDuplicates.txt",5000);

	}
	
	
	/*
	 * Note that the 'instance' does not have curly brackets and the subject is expected
	 * to be the first field
	 * 
	 * surrounding quotes will be discarded
	 */
	public static String extractSubjectFromInstance(String instance){
		String subject=instance.split("\t")[0];
		String[] fields=subject.split("\":\\[\"");
		if(!fields[0].substring(1, fields[0].length()).equals("subject")){
			System.out.println("Error! Expected subject but got "+fields[0].substring(1, fields[0].length()));
			return null;
		}
		else return fields[1].substring(0, fields[1].length()-2);
			
	}
	
	/*
	 * Instances in the freebase and dbpedia files are expected to be in sameAs order
	 * We will write out exactly two tab-delimited subjects (freebase[\t]dbpedia) to goldFile.
	 * No surrounding quotes.
	 */
	public static void buildGoldFile(String freebaseInstances, String dbpediaInstances, String goldFile)throws IOException{
		Scanner freebase=new Scanner(new FileReader(freebaseInstances));
		Scanner dbpedia=new Scanner(new FileReader(dbpediaInstances));
		PrintWriter out=new PrintWriter(new File(goldFile));
		while(freebase.hasNextLine()&& dbpedia.hasNextLine()){
			out.println(extractSubjectFromInstance(freebase.nextLine())+"\t"+extractSubjectFromInstance(dbpedia.nextLine()));
		}
		
		freebase.close();
		dbpedia.close();
		out.close();
	}
	
	/*
	 * Specifically for dbpedia and freebase. Take the sameAs file, and split into a
	 * dbpedia and freebase file, so we can test blocking. Make sure to build a gold-standard
	 * file also. Will use the static 'path' so be careful about that.
	 * 
	 * Curly brackets will not be included in each instance. You can read each line (in an
	 * instance file), split by tab and then send onto parseJSON
	 */

	public static void splitDuplicatesFileIntoInstanceFiles(String duplicatesFile)throws IOException{
		Scanner in=new Scanner(new FileReader(duplicatesFile));
		PrintWriter f_out=new PrintWriter(new File(path+"freebase.txt"));
		PrintWriter d_out=new PrintWriter(new File(path+"dbpedia.txt"));
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t\\{\t|\t\\}");
			String freebase=fields[1];
			f_out.println(freebase);
			String dbpedia=fields[3];
			d_out.println(dbpedia);
		}
		in.close();
		f_out.close();
		d_out.close();
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
	 * As for the boolean values, put both true for non-census (e.g. freebase and
	 * dbpedia) and both false for census.
	 * @param pairsFile
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<Double>> extractInstances(String pairsFile, 
			int count, double classValue, 
			boolean includeSubject, boolean includeAlphaPreprocess)throws IOException{
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
			ArrayList<Double> instance=null;
			String[] d3=fields[3].split("\t");
			String[] d1=fields[1].split("\t");
			String[] c3=null;
			String[] c1=null;
			if(!includeSubject)
			{
				c3=new String[d3.length-1];
				for(int i=1; i<d3.length; i++)
					c3[i-1]=d3[i];
				
				c1=new String[d1.length-1];
				for(int i=1; i<d1.length; i++)
					c1[i-1]=d1[i];
				
			}else{
				c3=d3;
				c1=d1;
			}
			if(includeAlphaPreprocess)
				instance=
					(extractJaccardFeatures(
						prepForAlphaJaccard(
							parseJSONIntoStringFeatures(c3)), 
						prepForAlphaJaccard(
							parseJSONIntoStringFeatures(c1))));
			else instance=
					(extractJaccardFeatures(
							prepForNonAlphaJaccard(
								parseJSONIntoStringFeatures(c3)), 
							prepForNonAlphaJaccard(
								parseJSONIntoStringFeatures(c1))));
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
	
	//instances of a between the specified range [lower-upper) will be returned in a new arraylist
		public static ArrayList<ArrayList<Double>> splitInstances(ArrayList<ArrayList<Double>> a, int lower, int upper){
			
			ArrayList<ArrayList<Double>> result=new ArrayList<ArrayList<Double>>();
			for(int i=lower; i<upper; i++){
				result.add(a.get(i));
				
			}
			return result;
		}
	
	public static ArrayList<Double> extractJaccardFeatures(ArrayList<HashSet<String>> preppedDB, ArrayList<HashSet<String>> preppedFB){
		ArrayList<Double> features=new ArrayList<Double>();
		for(int i=0; i<preppedDB.size(); i++)
			for(int j=0; j<preppedFB.size(); j++){
				features.add(similarities.Jaccard.computeJaccard(preppedDB.get(i), preppedFB.get(j)));
			}
		return features;
	}
	
	
	/*
	 * We're ignoring the integer part. 
	 */
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
	
	/*
	 * We're ignoring the integer part. 
	 */
	public static HashSet<String> prepForAlphaJaccard(HashMap<String,Integer> map){
		HashSet<String> tmp=new HashSet<String>();
		
			for(String key:map.keySet())
				if(isAlphabeticOnly(key))
					tmp.add(key);
			
		return tmp;
	}


	/*
	 * We're ignoring the integer part. 
	 */
	public static ArrayList<HashSet<String>> prepForNonAlphaJaccard(ArrayList<HashMap<String,Integer>> list){
		ArrayList<HashSet<String>> preppedList=new ArrayList<HashSet<String>>();
		for(HashMap<String,Integer> map:list){
			Set<String> keys=map.keySet();
			HashSet<String> tmp=new HashSet<String>();
			for(String key:keys)
				
					tmp.add(key);
			preppedList.add(tmp);
		}
		return preppedList;
	}


	/*
	 * We're ignoring the integer part. 
	 */
	public static HashSet<String> retainAlphaTokens(HashMap<String,Integer> map){
		HashSet<String> tmp=new HashSet<String>();
		
			Set<String> keys=map.keySet();
			
			for(String key:keys)
				if(isAlphabeticOnly(key))
					tmp.add(key);
			
		
		return tmp;
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
