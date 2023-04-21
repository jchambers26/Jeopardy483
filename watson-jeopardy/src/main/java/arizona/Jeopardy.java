package arizona;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
/**
 * Hello world!
 *
 */
public class Jeopardy 
{

    boolean indexExists = false;

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
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
