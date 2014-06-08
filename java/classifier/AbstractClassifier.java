package classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

abstract public class AbstractClassifier {
	private PrintStream out;
	private BufferedReader reader;
	
	/**********************************************************************************************
	* File Reading
	**********************************************************************************************/
	protected void openFile(String filename) throws IOException {
		reader = new BufferedReader( new FileReader(filename) );
	}
	protected String nextLine() throws IOException { return reader.readLine(); }
	
	protected void closeFile() throws IOException { reader.close(); }
	
	protected List<String> readLines(String filename) throws IOException {
		openFile(filename);
		
		List<String> lines = new ArrayList<String>();
		String line;
		while ( (line = nextLine() ) != null) { lines.add(line); }

		closeFile();
		
		return lines;
	}
	
	/**********************************************************************************************
	* Constructors
	**********************************************************************************************/
	public AbstractClassifier() { out = System.out; }
	
	public AbstractClassifier(PrintStream o) { out = o; }
	
	/**********************************************************************************************
	* Logging Output Stream
	**********************************************************************************************/
	protected void log(String s) { log(s, true); }
	protected void log(String s, boolean verbose) { if (verbose) out.print(s); }
	protected void logln(String s) { logln(s, true); }
	protected void logln(String s, boolean verbose) { if (verbose) out.println(s); }
	
	/**********************************************************************************************
	* Training
	**********************************************************************************************/
	abstract protected void trainLine(String line, boolean verbose);

	public void trainLines(List<String> lines, boolean verbose) {
		for (String line : lines)
			trainLine(line, verbose);
	}
	
	public void train(List<String> filenames) throws IOException { train(filenames, false); }
	public void train(List<String> filenames, boolean verbose) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		for (String filename : filenames)
			lines.addAll(readLines(filename));
		
		trainLines(lines, verbose);
	}
	
	public void train(String filename) throws IOException { train(filename, false); }
	public void train(String filename, boolean verbose) throws IOException {
		trainLines(readLines(filename), verbose);
	}	
	
   /**********************************************************************************************
   * Cross Validation 1/folds % validation (default is folds = 10)
   **********************************************************************************************/
   /* Reset the classifier (used in cross-validation) */
   abstract protected void reset();
   
   /* 1/folds validation on given lines*/
   abstract protected void crossValidateLines(List<String> lines, int folds);
   
   public void crossValidate(String filename) throws IOException { crossValidate(filename, 10); }
   public void crossValidate(List<String> filenames) throws IOException {
      crossValidate(filenames, 10);
   }
   
   public void crossValidate(String filename, int folds) throws IOException 
   { crossValidateLines(readLines(filename), folds); }
   
   public void crossValidate(List<String> filenames, int folds) throws IOException {
      if (folds <= 0 || filenames == null || filenames.isEmpty()) 
         return;
      
      List<String> lines = new ArrayList<String>();
      
      for (String filename : filenames)
         lines.addAll(readLines(filename));
   }
	
	/**********************************************************************************************
	* Classifier Evaluation
	**********************************************************************************************/

   enum EvalResult { NODATA, REPEAT, INCORRECT, CORRECT }
   
   /* Evaluate a single line from a set of labeled data lines
    * Returns: 
    *    NODATA    : No label/data found in line
    *    INCORRECT : Classifier incorrect
    *    CORRECT   : Classifier correct 
    */
   abstract protected EvalResult evaluateLine(String line, int v);
   
   protected List<Triplet> evaluateLines(List<String> lines) { return evaluateLines(lines, 1); }
	protected List<Triplet> evaluateLines(List<String> lines, int v) {
      int numCorrect = 0;
      int numUnknown = 0;
      int F = 0;
      
      for (String line : lines) {
         EvalResult result = evaluateLine(line, v);
         if (result != EvalResult.NODATA) {
            F += 1;
            
            if (result == EvalResult.CORRECT) { numCorrect += 1; }
            if (result == EvalResult.REPEAT)  { numUnknown += 1; }
         }
      }  
      
      List<Triplet> out = new ArrayList<Triplet>();
       
      logln("Summary:\n-----------", v >= 1);
      double correct = 100 * ( (double)numCorrect) / F;
      logln(correct + "% correct (" + numCorrect + "/" + F + ")", v >= 1);
      double incorrect = 100 * ((double) (F - numCorrect) - numUnknown) / F;
      logln(incorrect + "% incorrect (" + ((F - numCorrect) - numUnknown) + "/" + F + ")", v >= 1);
      
      double unknown = 100 * ((double) numUnknown) / F;
      logln(unknown + "% repeat requested (" + numUnknown + "/" + F + ")", v >= 1);
      
      out.add(new Triplet(correct, incorrect, unknown));
      
      return out;
	}
	
	public List<Triplet> evaluate(String filename) throws IOException 
	{ return evaluateLines(readLines(filename), 1); }
	
	public List<Triplet> evaluate(String filename, int verbosity) throws IOException 
	{ return evaluateLines(readLines(filename), verbosity); }
	
}
