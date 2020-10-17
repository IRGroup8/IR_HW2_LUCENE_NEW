import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;



public class Lucene_Indexer {

    Analyzer analyzer;
    Directory directory;
    Path indexPath;

    public Lucene_Indexer(Analyzer analyzer, Directory directory, Path indexPath) {

        this.analyzer = analyzer;
        this.directory = directory;
        this.indexPath = indexPath;
    }

    public void indexer() throws IOException {

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);
        Document doc = new Document();

        String text = "Test text for indexing! We should add an array of docs instead of this text in the future.";
        doc.add(new Field("field_name", text, TextField.TYPE_STORED));

        writer.addDocument(doc);
        writer.close();
    }

    public void searcher() throws ParseException, IOException {

        // Here we search an index:
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // Parse a simple query that searches for the word "instead":
        QueryParser parser = new QueryParser("field_name", analyzer);
        Query query = parser.parse("instead");
        ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;

        //I think the "hits" returns us the docs which had that word in them.
        System.out.println(hits.length);

        // Here we can get the docs which the word were in them.
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = searcher.doc(hits[i].doc);
            System.out.println(hitDoc.get("field_name"));
        }
    }


    public static void main(String[] args) throws IOException, ParseException {

        Analyzer analyzer = new StandardAnalyzer();

        Path indexPath = Files.createTempDirectory("tempIndex");

        Directory directory = FSDirectory.open(indexPath);

        Lucene_Indexer lucene_indexer = new Lucene_Indexer(analyzer, directory, indexPath);

        lucene_indexer.indexer();
        lucene_indexer.searcher();


        directory.close();
    }
}