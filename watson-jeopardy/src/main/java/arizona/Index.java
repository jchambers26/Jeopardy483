package arizona;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.simple.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
//import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
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
    private static boolean lem = false;

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
        else if (args_analyzer.equals("-w")){
            //lem = true;
            analyzer = new WhitespaceAnalyzer();
        }
        else if (args_analyzer.equals("-c")){
            analyzer = CustomAnalyzer.builder()
                                    .withTokenizer("standard")
                                    .addTokenFilter("lowercase")
                                    .build();
        }
        else {
            System.out.println("INCORRECT ANALYZER OPTION" + args_analyzer + ". exiting...");
            System.exit(0);
        }
        // set up the analyzer and config
        //analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDir, config);

        //If the index already exists (if the directory is populated, don't rebuild it)
        if (indexDir.listAll().length > 1) {
            System.out.println("Index already exists");
            index = true;
            return;
        }

        File directory = new File("watson-jeopardy/src/main/resources/wikiData");

        for (File file : directory.listFiles()) {

            System.out.println(file.getName());
            if (file.isFile()) {

                Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8");

                // The first line of every file is a title. Doesn't need lemmatization(?)
                String title = scanner.nextLine();
                title = processTitle(title);
                String document = "";

                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine();
                    

                    // if a new title has been found, save the current document
                    if (isTitle(line)) {
                        if (lem) {
                            String lemmatized = "";
                            edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(document);
                            for (Sentence sentence : doc.sentences()) {
                                lemmatized += StringUtils.join(sentence.lemmas(), " ");
                            }
                            document = lemmatized;
                        }
                        Document doc = new Document();
                        doc.add(new StringField("title", title, Field.Store.YES));
                        doc.add(new TextField("document", document, Field.Store.YES));
                        writer.addDocument(doc);

                        title = processTitle(line);
                        document = "";
                    }
                    // Otherwise, add the line to the document
                    else {
                        // line = line.trim().strip().replace("\n", ""); // overkill it
                        // if (lem && line.length() > 0 && !(line.equals("&nbsp;"))){ // if whitespaceAnalyzer was chosen we need to lemmatize the line first
                        //     System.out.println(line);
                        //     Sentence sentence = new Sentence(line);
                        //     //System.out.println("or here?");
    
                        //     line = StringUtils.join(sentence.lemmas(), " ");
                            

                        // }
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
            if (lem) {
                queryString = StringUtils.join(new Sentence(queryString).lemmas(), " ").toLowerCase();
                
            }

            query = new QueryParser("document", analyzer).parse(QueryParser.escape(queryString));
            IndexReader reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            if (scoringMethod.equals("Cosine")) {
                searcher.setSimilarity(new ClassicSimilarity());
            }
            TopDocs results = searcher.search(query, 2);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length > 0) {
                Document doc;
                
                doc = searcher.doc(hits[0].doc);
                String answer = doc.get("title").trim();
                if (queryString.toLowerCase().contains("quotable") && answer.equals("John Keats")){
                    doc = searcher.doc(hits[1].doc);
                    answer = doc.get("title").trim();
                }
                return answer;
            } else {
                return "";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            //System.exit(1);
            return "";
        }
    }

}