package classifier;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import classifier.AbstractClassifier;

public abstract class Classifier<LABEL> extends AbstractClassifier {

	/**********************************************************************************************
	* Implementation-dependent methods
	**********************************************************************************************/
	/* Parse a label of type LABEL from input line */
	abstract protected LABEL parseLabel(String line);
	
	/* Add example to classifier */
	abstract protected void trainLine(String line, boolean verbose);
	
	/* Classify a given string input (should include feature parsing) */
	abstract public Classification<LABEL> classifyLabeled(String line, boolean verbose);
	public Classification<LABEL> classifyLabeled(String line) { return classifyLabeled(line, false); }
	
	abstract public Classification<LABEL> classify(String input, boolean verbose);
	public Classification<LABEL> classify(String input) { return classify(input, false); }
	
	abstract public boolean isUnknown(Classification<LABEL> output);
	
   /**********************************************************************************************
   * Constructors
   **********************************************************************************************/
   public Classifier() { super(); }
   public Classifier(PrintStream o) { super(o); }
	
	/**********************************************************************************************
	* Cross Validation 1/folds % validation (default is folds = 10)
	**********************************************************************************************/
	protected void crossValidateLines(List<String> lines, int folds) {
		double aggregateScore = 0;
		
		double partitionSize = ((double)lines.size())/folds;
		
		for (int i = 0; i < folds; i++) {
			int start = (int)Math.floor(i * partitionSize);
			int end = (i == folds - 1) ? lines.size() : (int)Math.floor((i + 1) * partitionSize);

	        List<String> trainingLines = new ArrayList<String>();
	        List<String> testingLines = new ArrayList<String>();
	        int j = 0;
	        for (String line : lines) {
	        	if (j >= start && j <= end)
	        		trainingLines.add(line);
	        	else
	        		testingLines.add(line);
	        	
	        	j++;
	        }
	        trainLines(trainingLines, false);
	        
	        List<Triplet> out = evaluateLines(testingLines);
	        double score = out.get(0).d1;
	        System.out.println("Fold " + i + " (train: " + trainingLines.size() + " test: "
	                              + testingLines.size()+") score: " + score);
	        aggregateScore += score;
	        
	        this.reset();
		}
		
		System.out.println("Average score: " + (aggregateScore / folds));
	}
	
	
	/**********************************************************************************************
	* Classifier Evaluation
	**********************************************************************************************/
   /* Evaluate a single line from a set of labeled data lines
    * Returns: 
    *    NODATA    : No label/data found in line
    *    INCORRECT : Classifier incorrect
    *    CORRECT   : Classifier correct 
    */
	@Override
	protected EvalResult evaluateLine(String line, int v) {
		LABEL gold_label = parseLabel(line);
         
		if (gold_label != null) {
		   Classification<LABEL> classif = classifyLabeled(line);
			LABEL eval_label = classif.result;
			
			if (!eval_label.equals(gold_label)) {
				if (v >= 2)
					classifyLabeled(line, true);
	
				logln("Input: " + line + " (Gold Label: " + gold_label + ")", v >= 2);
				logln("Output: " + eval_label + "\n", v >= 2);
				
				if (isUnknown(classif))
				   return EvalResult.REPEAT;
				
				return EvalResult.INCORRECT;
			}
			else
				return EvalResult.CORRECT;
		}
		return EvalResult.NODATA;
	}
	
}
