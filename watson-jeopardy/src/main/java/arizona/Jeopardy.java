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
public class Jeopardy 
{

    private boolean indexExists = false;
    private int correct = 0;
    private int total = 0;

    private String scoringMethod = "BM25";

    public Jeopardy() {
        try {
            Index.buildIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args ) {
        try {
            System.out.println("********Welcome to the Watson Jeopardy Engine!********");
            Jeopardy jeopardy = new Jeopardy();
            System.out.println("********Indexing Complete!********");

            System.out.println("Would you like to use Cosine Similarity TFIDF scoring instead of probabilistic BM25? (y/n)");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            while (!input.equals("y") && !input.equals("n")) {
                System.out.println("Please enter y or n");
                input = scanner.nextLine();
            }
            if (input.equals("y")) {
                jeopardy.scoringMethod = "Cosine";
            }
            scanner.close();
            System.out.println("********Scoring Method: " + jeopardy.scoringMethod + "********");
            jeopardy.scanQuestions();

        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Scans the questions file and calls the search method for each question. 
     * It will compare the answer to the correct answer and print out the results.
     */
    public void scanQuestions() throws IOException{

        try {

            Scanner scanner = new Scanner(new File("watson-jeopardy/src/main/resources/questions.txt"));

            while (scanner.hasNextLine()) {
                
                String category = scanner.nextLine();
                String question = scanner.nextLine();
                question.replace("\"", "");
                String correctAnswer = scanner.nextLine();
                // Skip over the blank line
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                // The correct answer could be multiple answers, separated by a |
                String[] correctAnswers = correctAnswer.split("\\|");

                String query = "";

                if (category.equals("GOLDEN GLOBE WINNERS")) {
                    String q = question.substring(3, question.length());
                    String year = q.split(":")[0];
                    String info = q.split(":")[1].trim().toLowerCase();
                    query = year + " AND \"" + info + "\" AND \"golden globe\" AND actor^3.3 AND \"early life\"";
                    System.out.println(query);

                }
                else if (category.equals("HE PLAYED A GUY NAMED JACK RYAN IN...")) {
                    query = question + " AND \"jack ryan\" AND \"played by\" AND \"played\" AND \"character\" AND \"film\"";
                }
                else {
                    query = question + " " + category;
                }

                String answer = Index.getBestDoc(query, this.scoringMethod);

                // if (category.equals("GOLDEN GLOBE WINNERS")) {
                //     answer = Index.getBestDoc(query, this.scoringMethod);
                //     System.out.println("Category: " + category);
                //     System.out.println("Question: " + question);
                //     System.out.println("Correct Answer: " + correctAnswer);
                //     System.out.println("Answers: ");
                //     for (String answer : answers) {
                //         System.out.println(answer);
                //     }
                //     System.out.println();
                // }

                // System.out.println("Category: " + category);
                // System.out.println("Question: " + question);
                // System.out.println("Query: " + query);
                // System.out.println("Correct Answer: " + correctAnswer);
                // System.out.println("Answer: " + answer);
                // System.out.println();

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
                    System.out.println("Query: " + query);
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
