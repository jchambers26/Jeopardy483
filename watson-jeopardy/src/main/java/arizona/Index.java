package arizona;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class Index {

    private static boolean index = false;
    private static FSDirectory indexDir;
    private static StandardAnalyzer analyzer;
    private static IndexWriterConfig config;
    private static IndexWriter writer;
    
    public static void buildIndex() throws IOException {
        indexDir = FSDirectory.open(Paths.get("watson-jeopardy/src/main/resources/index"));

        // set up the analyzer and config
        analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDir, config);

        File directory = new File("watson-jeopardy/src/main/resources/wikiData");

        for (File file : directory.listFiles()) {

            System.out.println(file.getName());
            if (file.isFile()) {

                Scanner scanner = new Scanner(file);

                // The first line of every file is a title
                String title = scanner.nextLine();
                title = processTitle(title);
                String document = "";

                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine();

                    // if a new title has been found, save the current document
                    if (isTitle(line)) {
                        Document doc = new Document();
                        doc.add(new StringField("title", title, Field.Store.YES));
                        doc.add(new TextField("document", document, Field.Store.YES));
                        writer.addDocument(doc);

                        title = processTitle(line);
                        document = "";
                    }
                    // Otherwise, add the line to the document
                    else {
                        document += line;
                    }
                }

                scanner.close();

            }
        }

        writer.close();
        index = true;

    }

    /**
     * Process the title of a document by removing the 2 brackets on either side of it
     * @param title The title of the document
     * @return The processed title
     */
    private static String processTitle(String title) {
        title = title.replace("[", "");
        title = title.replace("]", "");
        return title;
    }

    /**
     * Check if the line is a title
     * @param line The line to check
     * @return True if the line is a title, false otherwise
     */
    private static boolean isTitle(String line) {
        if (line.length() > 4) {
            return line.substring(0, 2).equals("[[") && line.substring(line.length() - 2).equals("]]");
        } else {
            return false;
        }
    }

}
