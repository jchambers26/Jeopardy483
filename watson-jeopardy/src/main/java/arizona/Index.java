package arizona;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

public class Index {

    private static boolean index = false;
    private static FSDirectory indexDir;
    private static Analyzer analyzer;
    private static IndexWriterConfig config;
    private static IndexWriter writer;

    public static void buildIndex(String args_analyzer) throws IOException {

        indexDir = FSDirectory.open(Paths.get("watson-jeopardy/src/main/resources/index"));

        // choose your fighter
        if (args_analyzer.equals("-s"))
            analyzer = new StandardAnalyzer();
        else if (args_analyzer.equals("-e"))
            analyzer = new EnglishAnalyzer();
        else if (args_analyzer.equals("-si"))
            analyzer = new SimpleAnalyzer();
        // else if (args_analyzer .equals("-st"))
        // analyzer = new StopAnalyzer("/path/to/stopwords/file"); // TODO make stop
        // words file
        else if (args_analyzer.equals("-k"))
            analyzer = new KeywordAnalyzer();
        // else if (args_analyzer .equals("-sn"))
        // analyzer = new SnowballAnalyzer(null, args_analyzer, null);
        else if (args_analyzer.equals("-w"))
            analyzer = new WhitespaceAnalyzer();
        else {
            System.out.println("INCORRECT ANALYZER OPTION" + args_analyzer + ". exiting...");
            System.exit(0);
        }
        // set up the analyzer and config
        analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDir, config);

        // If the index already exists (if the directory is populated, don't rebuild it)
        if (indexDir.listAll().length > 1) {
            System.out.println("Index already exists");
            index = true;
            return;
        }

        File directory = new File("src/main/resources/wikiData");

        for (File file : directory.listFiles()) {

            System.out.println(file.getName());
            if (file.isFile()) {

                Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8");

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
     * Process the title of a document by removing the 2 brackets on either side of
     * it
     * 
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
     * 
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

    /**
     * Get the best document for the given query
     * 
     * @param queryString   The query to search for
     * @param scoringMethod The scoring method to use
     * @return The name of the best document
     * @throws IOException If the index cannot be read
     */
    public static String getBestDoc(String queryString, String scoringMethod) throws IOException {
        if (!index) {
            System.out.println(
                    "INDEX DID NOT EXIST WHEN .getBestDoc() WAS CALLED!\n Building index with StandardAnalyzer...");
            buildIndex("-s");
        }

        Query query;
        try {
            // Replace all newlines, ands, ors, and nots with spaces
            queryString = queryString.replace("\n", " ").toLowerCase();

            query = new QueryParser("document", analyzer).parse(QueryParser.escape(queryString));
            IndexReader reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            if (scoringMethod.equals("Cosine")) {
                searcher.setSimilarity(new ClassicSimilarity());
            }
            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length > 0) {
                Document doc = searcher.doc(hits[0].doc);
                return doc.get("title").trim();
            } else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
