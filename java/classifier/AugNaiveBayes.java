package classifier;

import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

public class AugNaiveBayes extends NaiveBayes {
   double thresh;
   
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
   * t - edit distance threshold (features can be modified up to this number of characters)
   **********************************************************************************************/
   public AugNaiveBayes(NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, double t) {
      super(fp, dPattern, dGroup, lGroup);
      thresh = t;
   }
   
   public AugNaiveBayes(PrintStream o, NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, 
                        double t) {
      super(o, fp, dPattern, dGroup, lGroup);
      thresh = t;
   }
   
   public AugNaiveBayes(NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, 
                        boolean eU, int minL, double minC, double t) {
      super(fp, dPattern, dGroup, lGroup, eU, minL, minC);
      thresh = t;
   }
   public AugNaiveBayes(PrintStream o, NaiveFeatureParser fp, Pattern dPattern, int dGroup, int lGroup, 
                        boolean eU, int minL, double minC, double t) {
      super(o, fp, dPattern, dGroup, lGroup, eU, minL, minC);
      thresh = t;
   }
   
   /**********************************************************************************************
   * Classification
   **********************************************************************************************/
    
   /** Returns a label for a set of features. **/
   @Override
   public Classification<String> classify(String input, boolean verbose) {
      if (input.length() < minLength && enableUnk)
         return new Classification<String>("?", 0);
      
      input = input.toLowerCase();
      
      List<String> features = featureParser.parse(input);
      List<String> passedFeatures = new ArrayList<String>();
      for (String f : features) {
         if (featureSet.contains(f))
            passedFeatures.add(f);
         else {
            String closeFeature = getCloseFeature(featureSet, f);
            if (closeFeature != null)
               passedFeatures.add(closeFeature);
            else
               passedFeatures.add(f);
         }
      }
       
      return classifyFeatures(passedFeatures, verbose);
   }
   
   public String getCloseFeature(Set<String> features, String feature) {
      String best = null;
      double dist = Double.POSITIVE_INFINITY;
      
      boolean numeric = Character.isDigit(feature.charAt(0));
      
      if (numeric || feature.length() == 1)
         return null;
      
      for (String f : features) {
         if (!Character.isDigit(f.charAt(0)) && !numeric && f.length() > 1) {
            double d = distance(f, feature);
            if (d < dist) {
               best = f;
               dist = d;
            }
         }
      }
      
      double change = dist / feature.length();
      
      if (change < thresh)
         return best;

      return null;
   }  
   
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
}
