package datasetDetails;

import java.io.*;
import java.util.*;

public class TestNXParserFreebase {

	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\"
			+ "experiments\\freebase\\";
	static String file=path+"freebase127-100lines.nt";
	
	public static void main(String[] args)throws Exception{
		Scanner in=new Scanner(new FileReader(file));
		int count=0;
		int comments=0;
		while(in.hasNextLine()){
			String line=in.nextLine();
			if(line.substring(0,1).equals("#"))
			{
				comments++;
				continue;
			}
			String[] nodes=line.split("\t");
			if(nodes.length==4){
				System.out.println(nodes[0].toString()+" "+nodes[1].toString()+" "+nodes[2].toString());
				count++;
			}
			
		}
		System.out.println("Number of compatible lines parsed "+count);
		System.out.println("Number of comment lines parsed "+comments);
		
		in.close();
	}
}
