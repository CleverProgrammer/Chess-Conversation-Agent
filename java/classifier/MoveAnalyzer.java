package classifier;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class MoveAnalyzer {
   private JoinedClassifier<String> moveAnalyzer;
   private EditDistanceClassifier castleAnalyzer;
   private EditDistanceClassifier pieceAnalyzer;
   private EditDistanceClassifier moveCastleDiscriminator;
   
   static String defaultMovesFile = "./data/moves_default.txt";
   static String spokenMovesFile = "./data/moves_computer.txt";
   static String spokenMovesFile2 = "./data/moves_david.txt";
   static String spokenMovesFile3 = "./data/moves_mys.txt";
   
   static String castleFile = "./data/moves_castling.txt";
   
   static String pieceSelectFile = "./data/moves_pieces.txt";
   
   static String castleMoveDiscrimFile = "./data/moves_discrim.txt";
   
   public MoveAnalyzer() {
      moveAnalyzer = new JoinedClassifier<String>();
   }
   
   public void init() {
      Pattern dataPattern = Pattern.compile("(.*): (\\w+) to ([a-z])([1-9])");
      Pattern simplePattern = Pattern.compile("(.*): (.*)");
      
      moveAnalyzer.addClassifier (new EditDistanceClassifier(dataPattern, 1, 2));
      moveAnalyzer.addClassifier (new EditDistanceClassifier(dataPattern, 1, 3));
      moveAnalyzer.addClassifier (new EditDistanceClassifier(dataPattern, 1, 4));
      
      pieceAnalyzer = new EditDistanceClassifier(simplePattern, 1, 2);
      castleAnalyzer = new EditDistanceClassifier(simplePattern, 1, 2);
      moveCastleDiscriminator = new EditDistanceClassifier(simplePattern, 1, 2);
      
      try {
         moveAnalyzer.train(defaultMovesFile);
         moveAnalyzer.train(spokenMovesFile);
         moveAnalyzer.train(spokenMovesFile2);
         moveAnalyzer.train(spokenMovesFile3);
         
         pieceAnalyzer.train(pieceSelectFile);
         castleAnalyzer.train(castleFile);
         moveCastleDiscriminator.train(castleMoveDiscrimFile);
      }
      catch (IOException e) {
         System.out.println("Unable to train on files.");
      }
   }
   
   public void evaluate(String filename, boolean verbose) {
      try {
         moveAnalyzer.evaluate(filename, verbose);
      }
      catch (IOException e) {
         System.out.println("Unable to open file for evaluation");
      }
   }
   
   public List<Classification<String>> classify(String input) {
      return moveAnalyzer.classify(input, true);
   }
  
   //[MOVE]/[CASTLE] (Piece Letter Number) (Castle side)
   public String classifyMove(String input) {
      // Classify move type
      Classification<String> out = moveCastleDiscriminator.classify(input);
      if (out.result.equals("move")) {
         List<Classification<String>> move = moveAnalyzer.classify(input);
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
   
   public String classifyPieceSelection(String input) {
      return pieceAnalyzer.classify(input).result;
   }
   
   public static void main(String[] args) {
      MoveAnalyzer moveAnalyzer = new MoveAnalyzer();
      
      moveAnalyzer.init();
      moveAnalyzer.evaluate(defaultMovesFile, false);
      moveAnalyzer.evaluate(spokenMovesFile, false);
      moveAnalyzer.evaluate(spokenMovesFile2, false);
      moveAnalyzer.evaluate(spokenMovesFile3, false);
      
      Scanner in = new Scanner(System.in);
      
      String input = "";
      while (!input.equals("stop")) {
         System.out.print("Input: ");
         input = in.nextLine();
         
         if (!input.equals("stop")) {
            List<Classification<String>> output = moveAnalyzer.classify(input);
            for (Classification<String> part : output)
               System.out.print(part.result + " (" + part.confidence + ") ");
            
            System.out.println("\n");
         }     
      }  
      
      in.close();
   }
}
