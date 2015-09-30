package developmentSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.rules.DecisionTable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;





public class Weka {

	
	Instances train;
	static Instances test;
	Classifier classifier;
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\"
			+ "sameAs\\weka-experiments\\";
	//static String noise="poisson";
	public static void main(String[] args)throws Exception{
		ArrayList<ArrayList<Double>> duplicates=DevelopmentSet.extractInstances(
				path+"500duplicates.txt", 
				500, 1.0, true, true);
		ArrayList<ArrayList<Double>> nonDuplicates=DevelopmentSet.extractInstances(
				path+"5000NonDuplicates.txt", 
				5000, 0.0, true, true);
		ArrayList<ArrayList<Double>> train=DevelopmentSet.splitInstances(duplicates, 0, 100);
		//ArrayList<ArrayList<Double>> test=DevelopmentSet.splitInstances(duplicates, 100, 500);
		DevelopmentSet.mergeInstances(train, 
				DevelopmentSet.splitInstances(nonDuplicates, 0, 1000));
	//	DevelopmentSet.mergeInstances(test, 
	//			DevelopmentSet.splitInstances(nonDuplicates, 1000, 5000));
		
		
	
		nonDuplicates=null;
		
		
		//Weka.buildTestInstances(test);
		
		Weka testrf=new Weka(train);
//		testrf.loadModelFromFile(System.getProperty("user.dir")+"\\bin\\developmentSet\\GP-test.model");
		
		//testrf.trainClassifier("decisionTable");
	//	testrf.printMetrics(path+noise+"-decisionTable-16features.csv");
		
		testrf.trainClassifier("GP");
		testrf.printModelToFile(path+"GP.model");
	
		//testrf.printMetrics(path+noise+"-GP-16features.csv");
		
	//	testrf.trainClassifier("linearRegression");
	//	testrf.printMetrics(path+noise+"-linearRegression-16features.csv");
		
	
	
	}

	/**
	 * instances contains an arraylist of double-valued features. The very last
	 * value will always be 1.0 (a duplicate) or 0.0 (a non-duplicate).
	 * @param instances
	 * @throws IOException 
	 */
	public Weka(ArrayList<ArrayList<Double>> instances){
		FastVector attributes=createAttributes(instances.get(0).size());
		train=new Instances("instances", attributes, instances.size());
		for(ArrayList<Double> instance: instances)
			train.add(new Instance(1.0,convertToArray(instance)));
		 train.setClassIndex(train.numAttributes() - 1);
		
	}
	
	public static void buildTestInstances(ArrayList<ArrayList<Double>> instances){
		FastVector attributes=createAttributes(instances.get(0).size());
		test=new Instances("instances", attributes, instances.size());
		for(ArrayList<Double> instance: instances)
			test.add(new Instance(1.0,convertToArray(instance)));
		 test.setClassIndex(test.numAttributes() - 1);
	}
	
	//remember, the last attribute is the classAttribute, which we declare to be numeric
	private static FastVector createAttributes(int numAttributes){
		FastVector att=new FastVector();
		for(int i=1; i<numAttributes; i++)
			att.addElement(new Attribute("feat"+i));
		att.addElement(new Attribute("classAttribute"));
		return att;
	}

	public static double[] convertToArray(ArrayList<Double> instance){
		double[] k=new double[instance.size()];
		for(int i=0; i<instance.size(); i++)
			k[i]=instance.get(i);
		return k;
	}

	public void trainClassifier(String classifierType) throws Exception{
		classifier=null;
		
		if(classifierType.equals("decisionTable"))
			classifier=new DecisionTable();
		else if(classifierType.equals("GP"))
			classifier=new GaussianProcesses();
		else if(classifierType.equals("linearRegression"))
			classifier=new LinearRegression();
		else
			System.out.println("Problems! Classifier not specified correctly");
		classifier.buildClassifier(train);
	}
	
	/*
	 * we've tested this, and so far, no mistakes.
	 */
	@SuppressWarnings("unused")
	private void testRecallPrecisionRandomForest()throws Exception{
		//threshold, recall, precision, f-measure
		//double[] results=new double[4];
		double avgNonDup=0.0;
		double avgDup=0.0;
		int countDup=0;
		int countNonDup=0;
		
		for(int i=0; i<test.numInstances(); i++){
			Instance inst=test.instance(i);
			
			double[] distr=classifier.distributionForInstance(inst);
			if(distr.length!=1)
				System.out.println("distr. length "+distr.length);
			if(inst.classValue()==0.0){
				avgNonDup+=distr[0];
				
				countNonDup++;
			}else if(inst.classValue()==1.0){
				avgDup+=distr[0];
				
				countDup++;
			}
			else{
				System.out.println("Erroneous class value of "+inst.classValue());
			}
		}
		avgNonDup/=countNonDup;
		
		avgDup/=countDup;
		
		System.out.println("Non-dups:\t"+"count:"+countNonDup+"\t"+avgNonDup);
		System.out.println("Dups:\t"+"count:"+countDup+"\t"+avgDup);
	}

	/*
	 * For now, use thresholds from 0.0 to 1.0 in increments of 0.1
	 * prints threshold, recall, precision and f-measure in that order
	 */
	public void printMetrics(String outfile)throws Exception{
		
		double[][] results=new double[10][4];
		
		for(int i=0; i<10; i++)
			results[i][0]=0.1*i;
		
		//true, false positives, negatives
		
		
		for(int j=0; j<10; j++){
			int TP=0;
			int FP=0;
			int FN=0;
		
			for(int i=0; i<test.numInstances(); i++){
				Instance inst=test.instance(i);
				
				double[] distr=classifier.distributionForInstance(inst);
				if(distr.length!=1)
					System.out.println("distr. length "+distr.length);
				
					if(inst.classValue()==0.0){
						if(distr[0]>=results[j][0])
							FP++;
						
						
					}else if(inst.classValue()==1.0){
						if(distr[0]>=results[j][0])
							TP++;
						else
							FN++;
					}
					else{
						System.out.println("Erroneous class value of "+inst.classValue());
					}
			}
			results[j][1]=(1.0*TP)/(TP+FN);
			if(TP+FP==0)
				continue;
			results[j][2]=(1.0*TP)/(TP+FP);
			results[j][3]=2*results[j][2]*results[j][1]/(results[j][2]+results[j][1]);
				
		}
		
		PrintWriter out=new PrintWriter(new File(outfile));
		
		out.println("Threshold,recall,precision,f-measure");
		for(int j=0; j<10; j++)
			out.println(results[j][0]+","+results[j][1]+","+results[j][2]+","+results[j][3]);
		out.close();
		
	}
	
	public void printModelToFile(String outfile)throws IOException{
		ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(outfile));
		out.writeObject(classifier);
		out.flush();
		out.close();
	}
	
	public void loadModelFromFile(String modelfile)throws IOException, ClassNotFoundException{
		ObjectInputStream in=new ObjectInputStream(new FileInputStream(modelfile));
		
		classifier=(Classifier) in.readObject();
		in.close();
	}
}
