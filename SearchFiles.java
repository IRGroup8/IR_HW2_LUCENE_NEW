import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
public class SearchFiles {

    private SearchFiles() {}
    public static void main(String[] args) throws Exception {
        String index = "index";
        String field = "contents";
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        QueryParser parser = new QueryParser(field, analyzer);
        while (true) {
            System.out.println("Enter query: ");
            String line = in.readLine();
            if (line == null || line.length() == -1) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(field));
            doPagingSearch(in, searcher, query, hitsPerPage);
        }
        reader.close();
    }
    public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query,
                                      int hitsPerPage) throws IOException {

        TopDocs results = searcher.search(query, 20 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;


        int numTotalHits = Math.toIntExact(results.totalHits.value);
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        while (true) {
            for (int i = start; i < end; i++) {
                Document doc = searcher.doc(hits[i].doc);
                String path = doc.get("path");
                if(path!=null) {
                    System.out.println("PATH : " + path + " docID=" + hits[i].doc + " score=" + hits[i].score);
                    continue;
                }
                else{
                    System.out.println("NO PATH FOUND");
                }
            }
            if (end == 0) {
                break;

            }

            if (numTotalHits >= end) {
                boolean quit = false;
                while (true) {
                    System.out.print("Press ");
                    if (start - hitsPerPage >= 0) {
                        System.out.print("(p)revious page, ");

                    }
                    if (start + hitsPerPage < numTotalHits) {
                        System.out.print("(n)ext page, ");

                    }
                    System.out.println("(q)uit or enter number to jump to a page.");

                    String line = in.readLine();
                    if (line.length() == 0 || line.charAt(0) == 'q') {
                        quit = true;
                        break;

                    }
                    if (line.charAt(0) == 'p') {
                        start = Math.max(0, start - hitsPerPage);
                        break;

                    } else if (line.charAt(0) == 'n') {
                        if (start + hitsPerPage < numTotalHits) {
                            start += hitsPerPage;

                        }
                        break;

                    } else {
                        int page = Integer.parseInt(line);
                        if ((page - 1) * hitsPerPage < numTotalHits) {
                            start = (page - 1) * hitsPerPage;
                            break;

                        } else {
                            System.out.println("No such page");

                        }

                    }

                }
                if (quit) break;
                end = Math.min(numTotalHits, start + hitsPerPage);
            }

        }

    }

}