package classifier;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaiveBayes extends Classifier<String>{
	protected Map<String,ClassStats> stats = new HashMap<String,ClassStats>();
	protected Set<String> featureSet = new HashSet<String>();
	protected Set<String> classSet = new HashSet<String>();
	
	protected NaiveFeatureParser featureParser;
	private Pattern dataPattern; 
	private int dataGroup;
	private int labelGroup;
	
	protected boolean enableUnk;
	protected int minLength = 4;
	protected double minConfidence = 0.0;

	protected int minTrainLength = 4;
	
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
	* 
	* fp - implementation of the Naive Bayes feature parser (NaiveFeatureParser)
   * dPattern - compiled regex pattern for labeled file data
   * dGroup - regex group within dPattern for data
   * lGroup - regex group within dPattern for the gold label
   * eU - enable output of unknown/repeat request ("?") labels
   * minL - minimum length of input (only used with unknown enabled)
   * minC - minimum confidence for output (only used with unknown enabled)
	**********************************************************************************************/
	public NaiveBayes(NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup) {
	   super();
	   featureParser = fp;
	   dataPattern = dPattern;
	   dataGroup = dGroup;
	   labelGroup = lGroup;
	   enableUnk = false;
	}
	
	public NaiveBayes(PrintStream o, NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup) { 
      super(o);
      featureParser = fp;
      dataPattern = dPattern;
      dataGroup = dGroup;
      labelGroup = lGroup;
      enableUnk = false;
   }
	
	public NaiveBayes(NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, 
	                  boolean eU, int minL, double minC) 
	{
		super();
		featureParser = fp;
		dataPattern = dPattern;
		dataGroup = dGroup;
		labelGroup = lGroup;
		enableUnk = eU;
		minLength = minL;
		minConfidence = minC;
	}
	public NaiveBayes(PrintStream o, NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, 
	                  boolean eU, int minL, double minC) 
	{
		super(o);
		featureParser = fp;
		dataPattern = dPattern;
		dataGroup = dGroup;
		labelGroup = lGroup;
		enableUnk = eU;
		minLength = minL;
		minConfidence = minC;
	}
	
	/**********************************************************************************************
	* Training
	**********************************************************************************************/
	public void trainLine(String line, boolean verbose) {
		String data = parseData(line);
		
		if (data == null || data.length() < minTrainLength)
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
			return m.group(labelGroup).trim().toLowerCase();
		
		return null;
	}
	
	private String parseData(String line) {
		Matcher m = dataPattern.matcher(line);
		if (m.find())
			return m.group(dataGroup).trim().toLowerCase();
		
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
	   if (input.length() < minLength && enableUnk) {
	      return new Classification<String>("?", 0);
	   }
	   
	   input = input.toLowerCase();
		
		List<String> features = featureParser.parse(input);
       
		return classifyFeatures(features, verbose);
	}
	
	protected Classification<String> classifyFeatures(List<String> features, boolean verbose) {
	   String label = "NO LABEL";
      
      double sum = 0;
      double maxProb = Double.NEGATIVE_INFINITY;
      for (String klass : classSet) {
         double prob = stats.get(klass).computeLikelihood(features, featureSet.size()); //+
                  // Math.log(stats.get(klass).num_examples) - log_total_examples;
           
         logln(klass + ": " + prob, verbose);
           
         sum += Math.pow(2, prob);
         
         if (prob > maxProb) {
            maxProb = prob;
            label = klass;
         }
      }
      
      sum = Math.pow(2, maxProb) / sum;
      
      if (sum >= minConfidence || !enableUnk)
         return new Classification<String>(label, sum);
      
      return new Classification<String>("?", 1 - sum);
	}

   @Override
   public boolean isUnknown(Classification<String> output) {
      return output.result.equals("?");
   }
}
