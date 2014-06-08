package classifier;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExClassifier extends Classifier<String> {
   private Pattern filePattern;
   private int fileLabelGroup;
   private int fileDataGroup;
   private Pattern dataPattern;
   private int dataGroup;
   
   /**********************************************************************************************
   * Reset
   **********************************************************************************************/
   @Override
   protected void reset() {}

   /**********************************************************************************************
   * Constructors
   **********************************************************************************************/
   public RegExClassifier(Pattern fp, int fpDGroup, int fpLGroup, Pattern dp, int dGroup) { 
      super();
      filePattern = fp;
      fileLabelGroup = fpLGroup;
      fileDataGroup = fpDGroup;
      dataPattern = dp;
      dataGroup = dGroup;
   }
   
   public RegExClassifier(PrintStream o, Pattern fp, int fpDGroup, int fpLGroup, Pattern dp, int dGroup) { 
      super(o); 
      filePattern = fp;
      fileLabelGroup = fpLGroup;
      fileDataGroup = fpDGroup;
      dataPattern = dp;
      dataGroup = dGroup;
   }
   
   /**********************************************************************************************
   * Training
   **********************************************************************************************/
   @Override
   // No training
   protected void trainLine(String line, boolean verbose) {}
   
   /**********************************************************************************************
   * Classification
   **********************************************************************************************/
   @Override
   protected String parseLabel(String line) {
      Matcher m = filePattern.matcher(line);
      if (m.find())
         return m.group(fileLabelGroup).trim();
      
      return null;
   }
   
   private String parseData(String line) {
      Matcher m = filePattern.matcher(line);
      if (m.find())
         return m.group(fileDataGroup).trim();   
      
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
      if (input.length() < 4)
         return new Classification<String>("?", 0);
      
      Matcher m = dataPattern.matcher(input);
      
      if (m.find()) {
         return new Classification<String>(m.group(dataGroup).trim(), 1.0);
      }
      
      return new Classification<String>("?", 1.0);
   }



}
