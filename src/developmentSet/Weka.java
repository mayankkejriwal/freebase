package developmentSet;

import java.io.IOException;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class Weka {

	
	Instances dataset;
	/**
	 * instances contains an arraylist of double-valued features. The very last
	 * value will always be 1.0 (a duplicate) or 0.0 (a non-duplicate).
	 * @param instances
	 * @throws IOException 
	 */
	public Weka(ArrayList<ArrayList<Double>> instances) throws IOException{
		ArrayList<Attribute> attributes=createAttributes(instances.get(0).size());
		dataset=new Instances("instances", attributes, instances.size());
		for(ArrayList<Double> instance: instances)
			dataset.add(new DenseInstance(1.0,convertToArray(instance)));
		 dataset.setClassIndex(dataset.numAttributes() - 1);
		
	}
	
	//remember, the last attribute is the classAttribute, which we declare to be numeric
	private ArrayList<Attribute> createAttributes(int numAttributes){
		ArrayList<Attribute> att=new ArrayList<Attribute>();
		for(int i=1; i<numAttributes; i++)
			att.add(new Attribute("feat"+i));
		att.add(new Attribute("classAttribute"));
		return att;
	}
	
	public double[] convertToArray(ArrayList<Double> instance){
		double[] k=new double[instance.size()];
		for(int i=0; i<instance.size(); i++)
			k[i]=instance.get(i);
		return k;
	}
	
	static String path="C:\\Users\\Mayank\\SkyDrive\\Documents\\experiments\\sameAs\\";
	public static void main(String[] args)throws IOException{
		ArrayList<ArrayList<Double>> duplicates=DevelopmentSet.extractInstances(path+"500duplicates.txt", 
				100, 1.0);
		ArrayList<ArrayList<Double>> nonDuplicates=DevelopmentSet.extractInstances(path+"5000NonDuplicates.txt", 
				1000, 1.0);
		DevelopmentSet.mergeInstances(duplicates, nonDuplicates);
		nonDuplicates=null;
		System.out.println(duplicates.size());
		new Weka(duplicates);
	
	}
	
}
