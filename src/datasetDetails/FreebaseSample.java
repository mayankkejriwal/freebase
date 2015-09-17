package datasetDetails;

import java.io.*;
import java.util.*;

public class FreebaseSample {

	
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\freebase\\";
	static String infile=path+"freebase1JSON.txt";
	static String outfile=path+"freebase1JSON-1000lines.nt";
	public static void main(String[] args)throws IOException {
		Scanner in=new Scanner(new FileReader(infile));
		PrintWriter out=new PrintWriter(new File(outfile));
		int count=0;
		while(in.hasNextLine()&& count<1000){
			out.println(in.nextLine());
			out.println();
			count++;
		}
		in.close();
		out.close();
	}

}
