package datasetDetails;

import java.io.*;
import java.util.*;

public class Curate {

	
	public static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\type-experiments\\experimental-data\\"; 
	static String file=path+"f-db-jaccard-ranked.txt";
	
	public static void main(String[] args)throws IOException {
		//testDbpediaDelimiter(15);
		//testReplace();
		//testCombineAndBuildDS();
		//combineConsolidatedFiles();
		//countTypes();
		//checkCounts();
		//printFirstLastID(file);
		
		//debugCuratedInstanceTypes();
		//printN(417);
		printNumLines();
		//curateTypesFile();

	}
	
	
	
	public static void debugCuratedInstanceTypes()throws IOException{
		Scanner in=new Scanner(new FileReader(file));
		String check="><";
		while(in.hasNextLine()){
			String line=in.nextLine();
			if(line.contains(check))
				System.out.println(line);
		}
		in.close();
		
	}
	
	//meant for those files (for example f-db-*.txt or curated_freebase_links)
	//will print out id in the first and last lines. Treat with caution, interpretations
	//could vary. Will also print out the number of lines in the file, for reference.
	public static void printFirstLastID(String file)throws IOException{
		Scanner in=new Scanner(new FileReader(file));
		String firstID=new String();
		String lastID=new String();
		if(in.hasNextLine())
			firstID=in.nextLine().split("\t")[0];
		long count=1;
		while(in.hasNextLine()){
			String line=in.nextLine();
			count++;
			lastID=line.split("\t")[0];
		}
		
		in.close();
		System.out.println("Lines in file: "+count);
		System.out.println("First ID in file: "+firstID);
		System.out.println("Last ID in file: "+lastID);
	}
	
	/**
	 * Meant to test combine and buildDataStructure since we are using them in
	 * a MapReduce job.
	 */
	public static void testCombineAndBuildDS(){
		String f="type\tstring1\t2\tstring2\t4\tstring3\t6";
		String g="type\tstring1\t7\tstring5\t4\tstring3\t9";
		String h="type\tstring1\t7";
		printHashMap(buildDataStructure(f.split("\t")));
		ArrayList<String> k=new ArrayList<String>();
		k.add(f);
		k.add(g);
		k.add(h);
		System.out.println(combine(k));
		//System.out.println(combine(buildDataStructure(f.split("\t")),null));
	}
	
	/*
	 * meant for a consolidated global tokens file. Prints out the number of unique types
	 * and lines in the file.
	 */
	public static void countTypes() throws FileNotFoundException{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		HashSet<String> types=new HashSet<String>();
		while(in.hasNextLine()){
			String type=in.nextLine().split("\t")[0];
			//if(type.contains("dbpedia.org/ontology"))
			types.add(type);
			count++;
		}
		in.close();
		System.out.println("Num. lines in file "+count);
		System.out.println("Num. types in file "+types.size());
	}
	
	/*
	 * meant for more than one consolidated global tokens file. Prints out the number of unique types
	 * among all the files.
	 */
	public static void countTypesAmongFiles() throws FileNotFoundException{
		ArrayList<HashSet<String>> types=new ArrayList<HashSet<String>>();
		for(int i=1; i<=5; i++){
		Scanner in=new Scanner(new FileReader(file+i));
		HashSet<String> tmp=new HashSet<String>();
		while(in.hasNextLine()){
			tmp.add(in.nextLine().split("\t")[0]);
			
		}
		in.close();
		System.out.println("Num. types in file "+i+" :"+tmp.size());
		types.add(tmp);
		}
		System.out.println(union(types).size());
		
	}
	
	//combine two consolidatedGlobalTokens files into one. Note that number of tokens
	//could now be more than 30,000 per type. 
	public static void combineConsolidatedFiles()throws FileNotFoundException{
		String file1=file+"1";
		String file2=file+"1";
		String outfile=path+"consolidatedCombined12";
		HashMap<String, HashMap<String,String>> dict=new HashMap<String, HashMap<String,String>>(4000);
		Scanner in=new Scanner(new FileReader(file1));
		while(in.hasNextLine()){
			String[] t=in.nextLine().split("\t");
			dict.put(t[0], buildDataStructure(t));
		}
		in.close();
		in=new Scanner(new FileReader(file2));
		
		PrintWriter out=new PrintWriter(new File(outfile));
		while(in.hasNextLine()){
			String[] t=in.nextLine().split("\t");
			if(dict.containsKey(t[0])){
				out.println(t[0]+"\t"+combine(dict.get(t[0]),buildDataStructure(t)));
				dict.remove(t[0]);
			}
			else
				
				out.println(t[0]+"\t"+combine(buildDataStructure(t), null));
			
				
		}
		in.close();
		for(String k: dict.keySet()){
			out.println(k+"\t"+combine(dict.get(k), null));
		}
		out.close();
	}
	
	/*
	 * Built the appropriate data structure from the tab-delimited array. Remember
	 * that the first element (a type) should be ignored.
	 */
	public static HashMap<String, String> buildDataStructure(String[] tokens){
		HashMap<String, String> result=new HashMap<String, String>(30000);
		for(int i=1; i<tokens.length; i+=2){
			result.put(tokens[i], tokens[i+1]);
		}
		
		return result;
	}
	
	//sets must contain at least one set, otherwise null pointer exception
	public static <T> HashSet<T> union(ArrayList<HashSet<T>> sets){
		HashSet<T> big=new HashSet<T>(sets.get(0));
		for(int i=1; i<sets.size(); i++){
			HashSet<T> tmp=sets.get(i);
			for(T t: tmp)
				big.add(t);
		}
		return big;
	}
	
	public static void checkTypeCounts()throws FileNotFoundException{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		int total=0;
		while(in.hasNextLine()){
			String line=in.nextLine();
			String[] fields=line.split("\t");
			if(fields[0].contains("dbpedia.org/ontology"))
				count++;
			total++;
		}
		in.close();
		System.out.println("Total types: "+total);
		System.out.println("dbpedia.org/ontology types: "+count);
	}
	
	public static void printN(int n) throws FileNotFoundException{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		while(in.hasNextLine()&& count<n){
			String line=in.nextLine();
			//if(line.trim().length()==0||line.substring(0, 1).equals("#"))
		//		continue;
			System.out.println(line);
			//System.out.println(line.replaceAll("/m.", "/m/"));
			//System.out.println();
			
			//System.out.println(line.split("> ")[0]+">");
			//System.out.println(line.split("> ")[2]+">");
			count++;
		}
		in.close();
	}
	
	//print for first n lines
	public static void testDbpediaDelimiter(int n)throws IOException{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		while(in.hasNextLine() && count<n){
			if(in.nextLine().substring(0,1).equals("#"))
				continue;
			String[] fields=in.nextLine().split("> ");
			if(fields.length!=3&&fields.length!=4)
				continue;
			System.out.print(fields.length+"\t");
			printArray(fields);
			count++;
		}
		
		in.close();
	}
	
	public static void testReplace(){
		String f="'''<3542345>";
		f=f.replaceAll("'", "");
		f=f.replaceAll("<", "");
		f=f.replaceAll(">", "");
		try{
			int k=Integer.parseInt(f);
			System.out.println(k);
		}catch(NumberFormatException e){
			
		}
		System.out.println(f);
	}
	/**
	 * We'll take the freebase_links.nt file, then print out another file that:
	 * (1) Only contains lower-case elements
	 * (2) contains three tab-separated fields of the form:
	 * <idx>[tab]<freebase instance>[tab]<dbpedia instance>
	 * and where idx is a positive integer appended with x.
	 * @throws IOException
	 */
	public static void curateLinksFile()throws IOException{
		Scanner in=new Scanner(new FileReader(path+"freebase_links.nt"));
		PrintWriter out=new PrintWriter(new File(path+"curated_freebase_links"));
		int count=1;
		while(in.hasNextLine()){
			
			String line=(in.nextLine()).toLowerCase();
			if(line.trim().length()==0||line.substring(0, 1).equals("#"))
				continue;
			String[] fields=line.split("> ");
			String freebase=fields[2]+">";
			String dbpedia=fields[0]+">";
			out.println((count+"x")+"\t"+freebase+"\t"+dbpedia);
			count++;
		}
		in.close();
		out.close();
	}
	
	/**
	 * We'll take the instance-types_en.nt file, then print out another file that:
	 * (1) Only contains lower-case elements
	 * (2) contains three tab-separated fields of the form:
	 * <idx>[tab]<freebase instance>[tab]<dbpedia instance>
	 * and where idx is a positive integer appended with x.
	 * @throws IOException
	 */
	public static void curateTypesFile()throws IOException{
		Scanner in=new Scanner(new FileReader(path+"instance-types_en.nt"));
		PrintWriter out=new PrintWriter(new File(path+"curated_instance-types"));
		
		while(in.hasNextLine()){
			
			String line=(in.nextLine()).toLowerCase();
			if(line.trim().length()==0||line.substring(0, 1).equals("#"))
				continue;
			String[] fields=line.split("> ");
			for(int i=0; i<=2; i++)
				fields[i]=fields[i]+">";
			out.println(fields[0]+"\t"+fields[1]+"\t"+fields[2]);
			
		}
		in.close();
		out.close();
	}
	public static <T,P> void printHashMap(HashMap<T, P> map){
		for(T t: map.keySet()){
			System.out.println(t.toString()+"\t"+map.get(t).toString());
		}
	}
	
	private static void printArray(String[] fields){
		for(String field: fields)
			System.out.println(field);
		System.out.println();
	}
	public static HashMap<String,String> combine(HashMap<String, String> a, HashMap<String, String> b){
		HashMap<String, String> result=new HashMap<String, String>();
		for(String k: a.keySet()){
			
			if(b.containsKey(k)){
				long num=Long.parseLong(a.get(k))+Long.parseLong(b.get(k));
				result.put(k, Long.toString(num));
			}
			else result.put(k, a.get(k));
		}
		
		for(String k: b.keySet()){
			if(!a.containsKey(k))
				
				result.put(k, b.get(k));
			
		}
		return result;
	}
	public static HashMap<String,String> combine(ArrayList<String> list){
		if(list.size()<=1)
			return buildDataStructure(list.get(0).split("\t"));
		
		HashMap<String,String> result=combine(buildDataStructure(list.get(0).split("\t")),buildDataStructure(list.get(1).split("\t")));
		for(int i=2; i<list.size(); i++){
			result=combine(result,buildDataStructure(list.get(i).split("\t")));
		}
		
		
		return result;
	}

	/**
	 * Prints the number of lines in the file
	 * 
	 * @throws FileNotFoundException
	 */
	public static void printNumLines() throws FileNotFoundException{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		while(in.hasNextLine()){
			in.nextLine();
			count++;
		}
		in.close();
		System.out.println("Total number of lines: "+count);
	}

}
