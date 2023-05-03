package arizona;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class Jeopardy {

    private boolean indexExists = false;
    private int correct = 0;
    private int total = 0;

    private String scoringMethod = "BM25";

    public Jeopardy(String analyzer_option) {
        try {
            Index.buildIndex(analyzer_option);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // args[0] - pick analyzer
    // StandardAnalyzer - - "-s"
    // EnglishAnalyzer - - "-e"
    // SimpleAnalyzer - - "-si"
    // StopAnalyzer - - "-st" --> not active, need stop words file
    // KeywordAnalyzer - - "-k"
    // SnowballAnalyzer - - "-sn" --> not active
    // WhitespaceAnalyzer - - "-w"
    //
    // args[1] - cosines similarity over bm25?
    // use cosine similarity - "-y"
    // use bm25 - "-n"
    public static void main(String[] args) {
        try {
            System.out.println("********Welcome to the Watson Jeopardy Engine!********");
            Jeopardy jeopardy = new Jeopardy(args[0]);
            System.out.println("********Indexing Complete!********");

            if (args[1].equals("-y")) {
                jeopardy.scoringMethod = "Cosine";
            } else if (args[1].equals("-n"))
                ; // keep default
            else {
                System.out.println("INVALID args[1]!");
                System.exit(1);
            }
            System.out.println("********Scoring Method: " + jeopardy.scoringMethod + "********");
            jeopardy.scanQuestions();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Scans the questions file and calls the search method for each question.
     * It will compare the answer to the correct answer and print out the results.
     */
    public void scanQuestions() throws IOException {

        try {

            Scanner scanner = new Scanner(new File("watson-jeopardy/src/main/resources/questions.txt"));

            while (scanner.hasNextLine()) {

                String category = scanner.nextLine();
                String question = scanner.nextLine();
                String correctAnswer = scanner.nextLine();
                // Skip over the blank line
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                // The correct answer could be multiple answers, separated by a |
                String[] correctAnswers = correctAnswer.split("\\|");

                String query = "";
                query = question + " " + category;
                // if (category.strip().equals("GOLDEN GLOBE WINNERS")) {
                //     String q = question.substring(3, question.length());
                //     String year = q.split(":")[0];
                //     String info = q.split(":")[1].trim().toLowerCase();
                //     query = year + " AND +\"" + info.replace(" on film", "") + "\" AND +\"golden globe\" AND actor^3.3 AND \"early life\" " + category;
                //     System.out.println(query);

                // }
                // else if (category.strip().equals("HE PLAYED A GUY NAMED JACK RYAN IN...")) {
                //     String movie = question.split(";")[0];
                //     query = movie + " AND actor AND \" he \" AND \"+jack ryan\" " + category;
                // }
                // else {
                //     query = question + " " + category;
                // }

                String answer = Index.getBestDoc(query, this.scoringMethod);

                // If the answer is one of the correct answers, increment the correct counter
                boolean good = false;
                for (String cor : correctAnswers) {
                    if (cor.toLowerCase().equals(answer.toLowerCase())) {
                        correct++;
                        good = true;
                        break;
                    }
                }
                if (!good) {
                    System.out.println("Category: " + category);
                    System.out.println("Question: " + question);
                    System.out.println("Correct Answer: " + correctAnswer);
                    System.out.println("Answer: " + answer);
                    System.out.println();
                }
                total++;
            }
            scanner.close();
            System.out.println("Correct: " + correct + " out of " + total);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}