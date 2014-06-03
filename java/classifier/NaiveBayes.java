package classifier;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaiveBayes extends Classifier<String>{
	private Map<String,ClassStats> stats = new HashMap<String,ClassStats>();
	private Set<String> featureSet = new HashSet<String>();
	private Set<String> classSet = new HashSet<String>();
	
	private ParseNaiveFeatures featureParser;
	private Pattern dataPattern; 
	private int dataGroup;
	private int labelGroup;

	int total_examples = 0;
   
	class ClassStats {
		int num_features = 0;
		int num_examples = 0;
		Map<String, Double> counter = new HashMap<String, Double>();
       
		public double computeLikelihood(List<String> features, int featureSet_size) {
			double prob = 0;
			double offset = Math.log(num_features + featureSet_size);
			for (String feature : features) {
				prob -= offset;
				if (counter.containsKey(feature))
					prob += Math.log(counter.get(feature) + 1);
			}
			return prob;
		}
	}
	
	/**********************************************************************************************
	* Reset
	**********************************************************************************************/
	@Override
	protected void reset() {
		stats.clear();
		featureSet.clear();
		classSet.clear();
		total_examples = 0;
	}
	
	/**********************************************************************************************
	* Constructors
	**********************************************************************************************/
	public NaiveBayes(ParseNaiveFeatures fp, Pattern dPattern, int dGroup, int lGroup) {
		super();
		featureParser = fp;
		dataPattern = dPattern;
		dataGroup = dGroup;
		labelGroup = lGroup;
	}
	public NaiveBayes(PrintStream o, ParseNaiveFeatures fp, Pattern dPattern, int dGroup, int lGroup) {
		super(o);
		featureParser = fp;
		dataPattern = dPattern;
		dataGroup = dGroup;
		labelGroup = lGroup;
	}
	
	/**********************************************************************************************
	* Training
	**********************************************************************************************/
	public void trainLine(String line, boolean verbose) {
		String data = parseData(line);
		
		if (data == null)
			return;
		
		List<String> features = featureParser.parse(data);
		String klass = parseLabel(line);
		
		classSet.add(klass);
       	ClassStats class_stat;
       	if (stats.containsKey(klass))
       		class_stat = stats.get(klass);
       	else {
       		stats.put(klass, new ClassStats());
       		class_stat = stats.get(klass);
       	}

       	class_stat.num_examples += 1;
       	for (String feature : features) {
           	featureSet.add(feature);
           	if (!class_stat.counter.containsKey(feature))
           		class_stat.counter.put(feature, 1.0);
           	else {
        	   double count = class_stat.counter.get(feature);
        	   class_stat.counter.put(feature, count + 1);
           	}
           	class_stat.num_features++;
       	}
   	}
	
	/**********************************************************************************************
	* Classification
	**********************************************************************************************/
	@Override
	protected String parseLabel(String line) {
		Matcher m = dataPattern.matcher(line);
		if (m.find())
			return m.group(labelGroup).trim();
		
		return null;
	}
	
	private String parseData(String line) {
		Matcher m = dataPattern.matcher(line);
		if (m.find())
			return m.group(dataGroup).trim();
		
		return null;
	}

	public Classification<String> classifyLabeled(String line, boolean verbose) {
		String input = parseData(line);
		
		if (input != null)
			return classify(input, verbose);
		
		return null;
	}
	 
 	/** Returns a label for a set of features. **/
	@Override
	public Classification<String> classify(String input, boolean verbose) {
		//int total_examples = 0;
		/*for (String klass : classSet) {
           	total_examples = total_examples + stats.get(klass).num_examples;
       	}
       	double log_total_examples = Math.log(total_examples);*/
		
		List<String> features = featureParser.parse(input);
       
		String label = "NO LABEL";
		double maxProb = Double.NEGATIVE_INFINITY;
		for (String klass : classSet) {
			double prob = stats.get(klass).computeLikelihood(features, featureSet.size()); //+
						// Math.log(stats.get(klass).num_examples) - log_total_examples;
           
			logln(klass + ": " + prob, verbose);
           
			if (prob > maxProb) {
				maxProb = prob;
				label = klass;
			}
		}
       
		return new Classification<String>(label, 1.0);
	}
}
