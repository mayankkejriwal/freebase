package sameAsAnalysis;

import java.io.*;
import java.util.*;

public class Analysis {
/**
 * The goal of this class is to get a precision-recall curve for the
 * MatcherOn100000{duplicatesZero,NonDuplicatesZero}.txt in the root folder
 * 
 */
	
	static String root="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\sameAs\\experimental-data\\full-blocking-classification\\";
	static String duplicatesFile=root+"MatcherOn100000duplicatesZero.txt";
	static String nonDuplicatesFile=root+"MatcherOn100000NonDuplicatesZero.txt";
	
	/*
	 * We're keeping this simple and hacky, since it has a singular purpose.
	 * We'll read in the two files, and over a certain threshold range, find
	 * {True,False}{Positives,Negatives}, and print them out on the console. We then
	 * copy/paste and analyze the results in root\analysis.xlsx
	 */
	public static void main(String[] args)throws IOException{
		ArrayList<Double> thresholds=buildThresholdRange();
		ArrayList<Integer> TPs=new ArrayList<Integer>();
		ArrayList<Integer> FPs=new ArrayList<Integer>();
		ArrayList<Integer> TNs=new ArrayList<Integer>();
		ArrayList<Integer> FNs=new ArrayList<Integer>();
		for(int i=0; i<thresholds.size(); i++){
			TPs.add(0);
			FPs.add(0);
			TNs.add(0);
			FNs.add(0);
		}
		
		Scanner in=new Scanner(new FileReader(duplicatesFile));
		while(in.hasNextLine()){
			double score=Double.parseDouble(in.nextLine().split("\t")[2]);
			for(int i=0; i<thresholds.size(); i++)
				if(score>=thresholds.get(i))
					TPs.set(i, TPs.get(i)+1);
				else
					FNs.set(i, FNs.get(i)+1);
		}
		in.close();
		
		in=new Scanner(new FileReader(nonDuplicatesFile));
		while(in.hasNextLine()){
			double score=Double.parseDouble(in.nextLine().split("\t")[2]);
			for(int i=0; i<thresholds.size(); i++)
				if(score<thresholds.get(i))
					TNs.set(i, TNs.get(i)+1);
				else
					FPs.set(i, FPs.get(i)+1);
		}
		in.close();
		
		System.out.println("threshold\tTP\tTN\tFP\tFN");
		for(int i=0; i<thresholds.size(); i++){
			System.out.println(thresholds.get(i)+"\t"+TPs.get(i)+"\t"+TNs.get(i)+"\t"+FPs.get(i)+"\t"+FNs.get(i));
		}
		
	}

	public static ArrayList<Double> buildThresholdRange(){
		ArrayList<Double> result=new ArrayList<Double>();
		for(double k=0.0; k<=1.0; k+=0.01)
			result.add(k);
		return result;
	}

	public static ArrayList<Double> buildThresholdRange(double start, double end, double increment){
		ArrayList<Double> result=new ArrayList<Double>();
		for(double k=start; k<=end; k+=increment)
			result.add(k);
		return result;
	}
}
