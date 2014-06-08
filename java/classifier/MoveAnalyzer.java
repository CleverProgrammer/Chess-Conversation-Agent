package classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

class WordParser implements NaiveFeatureParser {
   @Override
   public List<String> parse(String data) {
      List<String> features = new ArrayList<String>();
      
      String curWord = "";
      for (int i = 0; i < data.length(); i++) {
         char c = data.charAt(i);
         // Single digits, others as words
         if (Character.isAlphabetic(c) || (!Character.isDigit(c) && !Character.isSpaceChar(c)))
            curWord += c;
         else {
            if (!Character.isSpaceChar(c))
               features.add(Character.toString(c));
            
            if (curWord.length() > 0)
               features.add(curWord);
            
            curWord = "";  
         }
      }
      if (curWord.length() > 0)
         features.add(curWord);
      
      return features;
   }  
}

class CharacterParser implements NaiveFeatureParser {
   @Override
   public List<String> parse(String data) {
      List<String> features = new ArrayList<String>();
      
      for (int i = 0; i < data.length(); i++) {
         char c = data.charAt(i);
         if (!Character.isSpaceChar(c)) {
            features.add(Character.toString(c));
         }
      }
      return features;
   }
}

// Features are anything separated by spaces
class SimpleTokenizer implements NaiveFeatureParser {
   @Override
   public List<String> parse(String data) {
      List<String> features = new ArrayList<String>();
      
      String curWord = "";
      for (int i = 0; i < data.length(); i++) {
         char c = data.charAt(i);
         // Single digits, others as words
         if (!Character.isSpaceChar(c))
            curWord += c;
         else if (curWord.length() > 0) {
            features.add(curWord);
            curWord = "";  
         }
      }
      if (curWord.length() > 0)
         features.add(curWord);
      
      return features;
   }
}


public class MoveAnalyzer {
   private JoinedClassifier<String> moveAnalyzer;
   private EditDistanceClassifier castleAnalyzer;
   private EditDistanceClassifier pieceAnalyzer;
   private EditDistanceClassifier moveDiscrim;
   
   static String defaultMovesFile = "./data/moves_default.txt";
   static String spokenMovesFile = "./data/moves_computer.txt";
   static String spokenMovesFile2 = "./data/moves_david.txt";
   static String spokenMovesFile3 = "./data/moves_mys.txt";
   
   static String castleFile = "./data/moves_castling.txt";
   
   static String pieceSelectFile = "./data/moves_pieces.txt";
   
   static String castleMoveDiscrimFile = "./data/moves_discrim.txt";
   
   static String testFile = "./data/moves_test.txt";
   
   double pieceThresh = 0.38;
   double letterThresh = 0.31;
   double numberThresh = 0.24;
   
   public MoveAnalyzer(double d1, double d2, double d3) {
      pieceThresh = d1; letterThresh = d2; numberThresh = d3;
   }
   
   public MoveAnalyzer() { }
   
   public void init() {
      Pattern dataPattern = Pattern.compile("(.*): (\\w+) to ([a-z])([1-9])");
      Pattern simplePattern = Pattern.compile("(.*): (.*)");
      
      moveAnalyzer = new JoinedClassifier<String>();
      moveAnalyzer.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 2, true, 4, pieceThresh, 0.8));
      moveAnalyzer.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 3, true, 1, letterThresh, 0.3));
      moveAnalyzer.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 4, true, 1, numberThresh, 0.3));
      
      pieceAnalyzer = new EditDistanceClassifier(simplePattern, 1, 2, true);
      castleAnalyzer = new EditDistanceClassifier(simplePattern, 1, 2, true);
      moveDiscrim = new EditDistanceClassifier(simplePattern, 1, 2, true);
      
      try {
         moveAnalyzer.trainOnlyOn(defaultMovesFile, 1);
         moveAnalyzer.trainOnlyOn(defaultMovesFile, 2);
         moveAnalyzer.train(spokenMovesFile);
         moveAnalyzer.train(spokenMovesFile2);
         moveAnalyzer.train(spokenMovesFile3);
         
         pieceAnalyzer.train(pieceSelectFile);
         castleAnalyzer.train(castleFile);
         moveDiscrim.train(castleMoveDiscrimFile);
      }
      catch (IOException e) {
         System.out.println("Unable to train on files.");
      }
   }
   
   public List<Triplet> evaluate(String filename, int verbosity) {
      try {
         return moveAnalyzer.evaluate(filename, verbosity);
      }
      catch (IOException e) {
         System.out.println("Unable to open file for evaluation");
      }
      return null;
   }
  
   //[MOVE]/[CASTLE] (Piece Letter Number) (Castle side)
   public String classifyMove(String input) { return classifyMove(input, false); }
   public String classifyMove(String input, boolean verbose) {
      // Classify move type
      Classification<String> out = moveDiscrim.classify(input);
      if (out.result.equals("move")) {
         List<Classification<String>> move = moveAnalyzer.classify(input);
         
         if (verbose) {
            System.out.print("[MOVE] " + move.get(0).result + "(" + move.get(0).confidence + ") ");
            System.out.print(move.get(1).result + " (" + move.get(1).confidence + ") " );
            System.out.println(move.get(2).result + " (" + move.get(2).confidence + ")");
         }
         
         String output = "[MOVE] ";
         output += move.get(0).result + " ";
         output += move.get(1).result + " ";
         output += move.get(2).result;
         return output;  
      }
      else if (out.result.equals("castle")) {
         Classification<String> castle = castleAnalyzer.classify(input);
         return "[CASTLE] " + castle.result; 
      }
      else {
         return "[?]";
      }
   }
   
   public String classifyPiece(String input) {
      List<Classification<String>> move = moveAnalyzer.classify(input);
      if (move.get(0).result == "?")
         return pieceAnalyzer.classify(input).result;
      
      return move.get(0).result;
   }
   

   
   public static void main(String[] args) {
      
      // Test RegEx Classifier
      Pattern dataPattern = Pattern.compile("(.*): (\\w+) to ([a-z])([1-9])");
      Pattern regExData = Pattern.compile(".*(king|queen|knight|bishop|castle|rook|pawn).*([a-h]).?([1-8])");
      
      System.out.println("Regular Expression 'Classifier'");
      JoinedClassifier<String> regclass = new JoinedClassifier<String>();
      regclass.addClassifier(new RegExClassifier(dataPattern, 1, 2, regExData, 1));
      regclass.addClassifier(new RegExClassifier(dataPattern, 1, 3, regExData, 2));
      regclass.addClassifier(new RegExClassifier(dataPattern, 1, 4, regExData, 3));
      
      try { regclass.evaluate(testFile, 1); }
      catch (IOException e) { System.out.println("Unable to open test file"); }
      
      // Test Edit Distance Classifier
      System.out.println("\n\nEdit Distance Classifier");
      JoinedClassifier<String> editDist = new JoinedClassifier<String>();
      editDist.addClassifier(new EditDistanceClassifier(dataPattern, 1, 2, true));
      editDist.addClassifier(new EditDistanceClassifier(dataPattern, 1, 3, false));
      editDist.addClassifier(new EditDistanceClassifier(dataPattern, 1, 4, false));
      
      try { 
         editDist.train(defaultMovesFile);
         editDist.train(spokenMovesFile);
         editDist.train(spokenMovesFile2);
         editDist.train(spokenMovesFile3);
         editDist.evaluate(testFile, 1); 
      }
      catch (IOException e) { System.out.println("Unable to open test file"); }
      
      // Test Naive Bayes
      System.out.println("\n\nNaive Bayes Classifier");
      JoinedClassifier<String> nb = new JoinedClassifier<String>();
      nb.addClassifier(new NaiveBayes(new WordParser(), dataPattern, 1, 2, true, 4, 0));
      nb.addClassifier(new NaiveBayes(new WordParser(), dataPattern, 1, 3));
      nb.addClassifier(new NaiveBayes(new WordParser(), dataPattern, 1, 4));
      
      try { 
         nb.trainOnlyOn(defaultMovesFile, 1);
         nb.trainOnlyOn(defaultMovesFile, 2);
         nb.train(spokenMovesFile);
         nb.train(spokenMovesFile2);
         nb.train(spokenMovesFile3);
         nb.evaluate(testFile, 1); 
      }
      catch (IOException e) { System.out.println("Unable to open test file"); }
     
      
      System.out.println("\n\nAugmented Naive Bayes Classifier");
      JoinedClassifier<String> augnb = new JoinedClassifier<String>();
     
      augnb.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 2, true, 4, 0, 0.8));
      augnb.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 3, false, 1, 0, 0.3));
      augnb.addClassifier(new AugNaiveBayes(new WordParser(), dataPattern, 1, 4, false, 1, 0, 0.3));
      
      try { 
         augnb.trainOnlyOn(defaultMovesFile, 1);
         augnb.trainOnlyOn(defaultMovesFile, 2);
         augnb.train(spokenMovesFile);
         augnb.train(spokenMovesFile2);
         augnb.train(spokenMovesFile3);
         augnb.evaluate(testFile, 1); 
      }
      catch (IOException e) { System.out.println("Unable to open test file"); }
      
      System.out.println("\n\nCurrent Classifier:");
      
      /*for (double i = 0.0; i < 1.0; i += 0.01) {
         MoveAnalyzer moveAnalyzer = new MoveAnalyzer(0.38, 0.31, 0.22);
         moveAnalyzer.init();
         List<Triplet> results = moveAnalyzer.evaluate(testFile, 0);
         
         Triplet rev = results.get(2);
         
         System.out.format("%.2f, %.3f, %.3f, %.3f\n", i, rev.d1,rev.d2, rev.d3);
      }
      
      if (true)
         return;*/
      
      
      
      MoveAnalyzer moveAnalyzer = new MoveAnalyzer();
      moveAnalyzer.init();
      moveAnalyzer.evaluate(testFile, 1);
      
      Scanner in = new Scanner(System.in);
      
      String input = "";
      while (!input.equals("stop")) {
         System.out.print("Input: ");
         input = in.nextLine();
         
         if (!input.equals("stop")) {
            String output = moveAnalyzer.classifyPiece(input);
            System.out.println(output + "\n");
         }     
      }  
      
      in.close();
   }
}
