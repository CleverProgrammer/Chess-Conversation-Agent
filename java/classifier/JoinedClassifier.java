package classifier;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import classifier.AbstractClassifier;

public class JoinedClassifier<LABEL> extends AbstractClassifier {
	private List<Classifier<LABEL>> classifiers;
	public void addClassifier(Classifier<LABEL> c) { classifiers.add(c); }
	
	/**********************************************************************************************
	* Constructors
	**********************************************************************************************/
	public JoinedClassifier() { 
		super(); 
		classifiers = new ArrayList<Classifier<LABEL>>();
	} 
	public JoinedClassifier(PrintStream o) { 
		super(o);
		classifiers = new ArrayList<Classifier<LABEL>>();
	}
	
	/**********************************************************************************************
	* Training
	**********************************************************************************************/
	protected void trainLine(String line, boolean verbose) {
		logln("Adding example from line: " + line, verbose);
		for (int i = 0; i < classifiers.size(); i++)
			classifiers.get(i).trainLine(line, verbose);
	}
	 
	/**********************************************************************************************
	* Classification
	**********************************************************************************************/
	public List<Classification<LABEL>> classify(String line) {return classify(line, false); }
	public List<Classification<LABEL>> classify(String line, boolean verbose) {
		List<Classification<LABEL>> output = new ArrayList<Classification<LABEL>>();
		
		for (Classifier<LABEL> c : classifiers) 
			output.add(c.classify(line, verbose));
		
		return output;
	}
	
	public List<Classification<LABEL>> classifyLabeled(String line) { return classifyLabeled(line, false); }
	public List<Classification<LABEL>> classifyLabeled(String line, boolean verbose) {
		List<Classification<LABEL>> output = new ArrayList<Classification<LABEL>>();
		
		for (Classifier<LABEL> c : classifiers) 
			output.add(c.classifyLabeled(line, verbose));
		
		return output;
	}
	
	/**********************************************************************************************
	* Cross Validation 1/folds % validation (default is folds = 10)
	**********************************************************************************************/
   protected void reset() {
      for (Classifier<LABEL> c : classifiers)
         c.reset();
   }

   protected void crossValidateLines(List<String> lines, int folds) {
      // TODO Auto-generated method stub
      
   }
	
	
	/**********************************************************************************************
	* Classifier Evaluation
	**********************************************************************************************/
	private List<LABEL> parseLabel(String line) {
		List<LABEL> labels = new ArrayList<LABEL>();
		
		for (Classifier<LABEL> c : classifiers)
			labels.add(c.parseLabel(line));
		
		return labels;
	}
	
	/* Unused */
   protected EvalResult evaluateLine(String line, boolean verbose) {
      return null;
   }
	
	@Override
	protected double evaluateLines(List<String> lines, boolean verbose) {
		int N = classifiers.size();
		
		if (N == 0) 
			return 1.0;
		
		int[] numCorrect = new int[N + 1];
		for (int i = 0; i < N + 1; i++)
			numCorrect[i] = 0;
		 
		int F = 0;
      
		for (String line : lines){
			List<Classification<LABEL>> evalOutputs = classifyLabeled(line);
			List<LABEL> goldLabels = parseLabel(line);
			
			if (!goldLabels.isEmpty() && !goldLabels.contains(null)) {
				F += 1;
				
				boolean isWrong = false;
				String output = "";
				String gold = "";
			
				for (int i = 0; i < N; i++) {
					LABEL goldLabel = goldLabels.get(i);
					LABEL evalLabel = evalOutputs.get(i).result;
					
					if (evalLabel != null) {
				
						if (!evalLabel.equals(goldLabel))
							isWrong = true;
						else
							numCorrect[i] += 1;
					
						output = (i != 0) ? output + " " + evalLabel.toString() 
										  : evalLabel.toString();
						
					}
					else {
						isWrong = true;
						output = (i != 0) ? output + " <NULL>" : "<NULL>";
					}
					
					gold = (i != 0) ? gold + " " + goldLabel.toString() : goldLabel.toString();
				}
				
				if (isWrong && verbose) {
					logln("Input: " + line);
					classifyLabeled(line, true);
					logln("\n Output: " + output + ", Label: " + gold + "\n");
				}		
				else if (!isWrong) {
					numCorrect[N] += 1;
				}
			}		
		}	
	
		System.out.println("Summary:\n-----------");
		for (int i = 0; i < N; i++) {
			double acc = 100 * ( (double)numCorrect[i]) / F;
			logln("Classifier " + i + ": " + acc + "% accurate (" + numCorrect[i] + "/" + F + ")");
		}	
		double acc = 100 * ( (double)numCorrect[N]) / F;
		logln("Total accuracy: " + acc + "% correct (" + numCorrect[N] + "/" + F + ")");
	
		return acc;
	}
}
