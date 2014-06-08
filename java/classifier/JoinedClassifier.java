package classifier;

import java.io.IOException;
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
	
	protected void trainLineOnlyOn(String line, int num) {
	   classifiers.get(num).trainLine(line, false);
	}
	
	protected void trainOnlyOn(String filename, int num) throws IOException {
	   List<String> lines = readLines(filename);
	   
	   for (String line : lines)
	      trainLineOnlyOn(line, num);
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
	@Override
   protected EvalResult evaluateLine(String line, int v) {
      return null;
   }
	
	@Override
	protected List<Triplet> evaluateLines(List<String> lines, int v) {
		int N = classifiers.size();
		
		if (N == 0) 
			return null;
		
		int[] numCorrect = new int[N + 1];
		int[] numUnknown = new int[N + 1];
		for (int i = 0; i < N + 1; i++) {
			numCorrect[i] = 0;
			numUnknown[i] = 0;
		}
		 
		int F = 0;
      
		for (String line : lines){
			List<Classification<LABEL>> evalOutputs = classifyLabeled(line);
			List<LABEL> goldLabels = parseLabel(line);
			
			if (!goldLabels.isEmpty() && !goldLabels.contains(null)) {
				F += 1;
				
				boolean [] isPartWrong = new boolean[3];
				
				boolean isWrong = false;
				boolean isRepeat = false;
				String output = "";
				String gold = "";
			
				for (int i = 0; i < N; i++) {
					LABEL goldLabel = goldLabels.get(i);
					Classification<LABEL> eval = evalOutputs.get(i);
					LABEL evalLabel = eval.result;
					double conf = eval.confidence;
					
					if (evalLabel != null) {
				
						if (!evalLabel.equals(goldLabel)) {
							if (classifiers.get(i).isUnknown(eval)) {
							   numUnknown[i] += 1;
							   isRepeat = true;
							}
							else {
							   isWrong = true;
							   isPartWrong[i] = true;
							}
						}
						else
							numCorrect[i] += 1;
					
						output = (i != 0) ? output + " " + evalLabel.toString() + " [" + (int)(100*conf) + "%]"
										  : evalLabel.toString() + "[" + (int)(100*conf) + "%]"; 
						
					}
					else {
						isWrong = true;
						output = (i != 0) ? output + " <NULL>" : "<NULL>";
					}
					
					gold = (i != 0) ? gold + " " + goldLabel.toString() : goldLabel.toString();
				}
				
				if (isWrong && v >= 2 && isPartWrong[2]) {
					logln("Input: " + line.split(":")[0]);
					//classifyLabeled(line, true);
					logln("Output: " + output + ", Label: " + gold + "\n");
				}		
				else if (isRepeat) {
				   numUnknown[N] += 1;
				}
				else if (!isWrong && !isRepeat) {
					numCorrect[N] += 1;
				}
			}		
		}	
	
		List<Triplet> out = new ArrayList<Triplet>();
		
		logln("Summary:\n-----------", v >= 1);
		for (int i = 0; i < N; i++) {
			double correct = 100 * ( (double)numCorrect[i]) / F;
			logln("Classifier " + i + ": ", v >= 1);
	      logln(correct + "% correct (" + numCorrect[i] + "/" + F + ")", v >= 1);
	      double incorrect = 100 * ((double) (F - numCorrect[i]) - numUnknown[i]) / F;
	      logln(incorrect + "% incorrect (" + ((F - numCorrect[i]) - numUnknown[i]) + "/" + F + ")", v >= 1);
	      
	      double unknown = 100 * ((double) numUnknown[i]) / F;
	      logln(unknown + "% repeat requested (" + numUnknown[i] + "/" + F + ")", v >= 1);
	      
	      out.add(new Triplet(correct, incorrect, unknown));
		}	
		double correct = 100 * ( (double)numCorrect[N]) / F;
		logln("Total: ", v >= 1);
		logln(correct + "% correct (" + numCorrect[N] + "/" + F + ")", v >= 1);
      double incorrect = 100 * ((double) (F - numCorrect[N]) - numUnknown[N]) / F;
      logln(incorrect + "% incorrect (" + ((F - numCorrect[N]) - numUnknown[N]) + "/" + F + ")", v >= 1);
      
      double unknown = 100 * ((double) numUnknown[N]) / F;
      logln(unknown + "% repeat requested (" + numUnknown[N] + "/" + F + ")", v >= 1);
      
      out.add(new Triplet(correct, incorrect, unknown));
		
		return out;
	}
}
