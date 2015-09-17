package datasetDetails;

import java.io.*;
import java.util.*;

/**
 * I want to check if it's true that not every reducer-emitted output gets written
 * to a new line. See file snippet experiments/testTypeOutputNewline.txt
 * @author Mayank
 *
 */
public class TestTypeOutput {
	
	static String file="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\testTypeOutputNewline.txt";
	
	public static void main(String[] args)throws IOException{
		Scanner in=new Scanner(new FileReader(file));
		in.useDelimiter("\t");
		while(in.hasNext()){
			System.out.println(in.next());
		}
		
		in.close();
	}
}
