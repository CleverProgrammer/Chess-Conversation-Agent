package classifier;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditDistanceClassifier extends Classifier<String> {
	private List<List<String>> refPoints;
	private List<String> labels;
	private final boolean allowTransposition = false;
	
	private Pattern dataPattern;
	private int labelGroup;
	private int dataGroup;
	boolean enableUnk;
	
	/**********************************************************************************************
	* Reset
	**********************************************************************************************/
	@Override
	protected void reset() {
		refPoints.clear();
		labels.clear();
	}

	/**********************************************************************************************
	* Constructors
	**********************************************************************************************/
	public EditDistanceClassifier(Pattern filePattern, int dGroup, int lGroup, boolean eU) { 
		super();
		refPoints = new ArrayList<List<String>>();
		labels = new ArrayList<String>();
		dataPattern = filePattern;
		labelGroup = lGroup;
		dataGroup = dGroup;
		enableUnk = eU;
	}
	
	public EditDistanceClassifier(PrintStream o, Pattern filePattern, int dGroup, int lGroup, boolean eU) { 
		super(o); 
		refPoints = new ArrayList<List<String>>();
		labels = new ArrayList<String>();
		dataPattern = filePattern;
		labelGroup = lGroup;
		dataGroup = dGroup;
		enableUnk = eU;
	}
	
	/**********************************************************************************************
	* Training
	**********************************************************************************************/
	@Override
	protected void trainLine(String line, boolean verbose) {
		String label = parseLabel(line);
		String data  = parseData(line);
		data = data.toLowerCase();
		
		// Skip data entries with a length less than 4
		if (data.length() < 4)
		   return;
		
		int index = labels.indexOf(label);
		if (index >= 0) {
			refPoints.get(index).add(data);
		}
		else {
			labels.add(label);
			List<String> newRefs = new ArrayList<String>();
			newRefs.add(data);
			refPoints.add(newRefs);
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
	
   @Override
   public boolean isUnknown(Classification<String> output) {
      return output.result.equals("?");
   }

	@Override
	public Classification<String> classifyLabeled(String line, boolean verbose) {
		String input = parseData(line);
		return classify(input, verbose);
	}
	
	@Override
	public Classification<String> classify(String input, boolean verbose) {
	   if (input.length() < 4 && enableUnk)
	      return new Classification<String>("?", 0);
	   
	   input = input.toLowerCase();
	   
		int bestLabelIndex = 0;
		double minDist = Double.POSITIVE_INFINITY;
		
		List<Double> metrics = new ArrayList<Double>();
		
		for (int i = 0; i < refPoints.size(); i++ ) {
			List<String> refs = refPoints.get(i);
			double refDist = Double.POSITIVE_INFINITY;
			CharSequence best = "";
			boolean foundMatch = false;
		
			int r = 0;
			for (; r < refs.size() && !foundMatch; r++) {
				double dist = distance(refs.get(r), input);
				
				if (dist >= 0 && dist < refDist) {
					refDist = dist;
					best = refs.get(r);
					foundMatch = (dist == 0);
				}
			}
			
			logln("Total distance for label " + labels.get(i)+ ": " + refDist + " ('" + best + "' of " + refs.size() + ")", verbose);
			
			if (refDist < minDist) {
				bestLabelIndex = i;
				minDist = refDist;
			}
		
			metrics.add(refDist);
		}
		String label = labels.get(bestLabelIndex);
		
		double metric_num = 0.0;
		for (double m : metrics) { 
		   if (m == minDist)
		      metric_num += 1.0;
		}
		
		double confidence = 1.0 / metric_num;
		
		logln("==>" + label + "(" + confidence + ")", verbose);
		
		if (confidence > 0.9)
		   return new Classification<String>(label, confidence);
		
		return new Classification<String>("?", 1.0);
	}
	
	/**********************************************************************************************
	* Returns the edit distance between the character sequences with
	* or without transpositions as specified.  This distance is
	* symmetric.  This method is thread safe and may be accessed
	* concurrently.
	*
	* @param cSeq1              First character sequence.
	* @param cSeq2              Second character sequence.
	* @return Edit distance between the character sequences.
	**********************************************************************************************/
    private int distance(CharSequence cSeq1, CharSequence cSeq2) {
        // switch for min sized lattice slices
        if (cSeq1.length() < cSeq2.length()) {
            CharSequence temp = cSeq1;
            cSeq1 = cSeq2;
            cSeq2 = temp;
        }

        // compute small array cases
        if (cSeq2.length() == 0) return cSeq1.length();
        if (cSeq2.length() == 1) {
            char c = cSeq2.charAt(0);
            for (int i = 0; i < cSeq1.length(); ++i)
                if (cSeq1.charAt(i) == c)
                    return cSeq1.length() - 1; // one match
            return cSeq1.length(); // one subst, other deletes
        }

        if (allowTransposition)
            return editDistanceTranspose(cSeq1, cSeq2);
        return editDistanceNonTranspose(cSeq1, cSeq2);
    }

    private int editDistanceNonTranspose(CharSequence cSeq1, CharSequence cSeq2) {
        // cSeq1.length >= cSeq2.length > 1
        int xsLength = cSeq1.length() + 1; // > ysLength
        int ysLength = cSeq2.length() + 1; // > 2

        int[] lastSlice = new int[ysLength];
        int[] currentSlice = new int[ysLength];

        // first slice is just inserts
        for (int y = 0; y < ysLength; ++y)
            currentSlice[y] = y;  // y inserts down first column of lattice

        for (int x = 1; x < xsLength; ++x) {
            char cX = cSeq1.charAt(x - 1);
            int[] lastSliceTmp = lastSlice;
            lastSlice = currentSlice;
            currentSlice = lastSliceTmp;
            currentSlice[0] = x; // x deletes across first row of lattice
            for (int y = 1; y < ysLength; ++y) {
                int yMinus1 = y - 1;
                // unfold this one step further to put 1 + outside all mins on match
                currentSlice[y] = Math.min(cX == cSeq2.charAt(yMinus1)
                        ? lastSlice[yMinus1] // match
                        : 1 + lastSlice[yMinus1], // subst
                        1 + Math.min(lastSlice[y], // delelte
                                currentSlice[yMinus1])); // insert
            }
        }
        return currentSlice[currentSlice.length - 1];
    }

    private int editDistanceTranspose(CharSequence cSeq1, CharSequence cSeq2) {

        // cSeq1.length >= cSeq2.length > 1
        int xsLength = cSeq1.length() + 1; // > ysLength
        int ysLength = cSeq2.length() + 1; // > 2

        int[] twoLastSlice = new int[ysLength];
        int[] lastSlice = new int[ysLength];
        int[] currentSlice = new int[ysLength];

        // x=0: first slice is just inserts
        for (int y = 0; y < ysLength; ++y)
            lastSlice[y] = y;  // y inserts down first column of lattice

        // x=1:second slice no transpose
        currentSlice[0] = 1; // insert x[0]
        char cX = cSeq1.charAt(0);
        for (int y = 1; y < ysLength; ++y) {
            int yMinus1 = y - 1;
            currentSlice[y] = Math.min(cX == cSeq2.charAt(yMinus1)
                    ? lastSlice[yMinus1] // match
                    : 1 + lastSlice[yMinus1], // subst
                    1 + Math.min(lastSlice[y], // delelte
                            currentSlice[yMinus1])); // insert
        }

        char cYZero = cSeq2.charAt(0);

        // x>1:transpose after first element
        for (int x = 2; x < xsLength; ++x) {
            char cXMinus1 = cX;
            cX = cSeq1.charAt(x - 1);

            // rotate slices
            int[] tmpSlice = twoLastSlice;
            twoLastSlice = lastSlice;
            lastSlice = currentSlice;
            currentSlice = tmpSlice;

            currentSlice[0] = x; // x deletes across first row of lattice

            // y=1: no transpose here
            currentSlice[1] = Math.min(cX == cYZero
                    ? lastSlice[0] // match
                    : 1 + lastSlice[0], // subst
                    1 + Math.min(lastSlice[1], // delelte
                            currentSlice[0])); // insert

            // y > 1: transpose
            char cY = cYZero;
            for (int y = 2; y < ysLength; ++y) {
                int yMinus1 = y - 1;
                char cYMinus1 = cY;
                cY = cSeq2.charAt(yMinus1);
                currentSlice[y] = Math.min(cX == cY
                        ? lastSlice[yMinus1] // match
                        : 1 + lastSlice[yMinus1], // subst
                        1 + Math.min(lastSlice[y], // delelte
                                currentSlice[yMinus1])); // insert
                if (cX == cYMinus1 && cY == cXMinus1)
                    currentSlice[y] = Math.min(currentSlice[y], 1 + twoLastSlice[y - 2]);
            }
        }
        return currentSlice[currentSlice.length - 1];
    }


}
