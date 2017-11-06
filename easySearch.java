package assignment2;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {
	private String filePath;
	
	/**
	 * Initialize class with Lucene Index path
	 * @param indexPath Path of Lucene Index
	 */
	public easySearch(String indexPath) {
		this.filePath = indexPath;
	}

	/**
	 * Function to return TFIDF ranking results for user supplied query.
	 * @param userQuery User Query
	 * @return A priority queue with result arranged according to descending order of score
	 * @throws ParseException
	 * @throws IOException
	 */
	public PriorityQueue<Entry> getTfIDFResults(String userQuery) throws ParseException, IOException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(this.filePath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		
		
		Query query = parser.parse(userQuery);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		ClassicSimilarity dSimi = new ClassicSimilarity();
		
		int dcount = reader.maxDoc();
		HashMap<String, Float> hmap = new HashMap<String, Float>();

		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();// Processing each segment
		for (Term t : queryTerms) {
			float df=reader.docFreq(new Term("TEXT", t.text()));
			for (int i = 0; i < leafContexts.size(); i++) {
			
				
				
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				int doc;
				
				if (de != null) 
					{
					
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						int docid = de.docID() + startDocNo;
						// Get normalized length (1/sqrt(numOfTokens)) of the document
						float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
								.getNormValues("TEXT").get(de.docID()));
						// Get length of the document
						float docLeng = 1 / (normDocLeng * normDocLeng);
						float docTermFreq = de.freq();
//						System.out.println(t.text()+" occurs " + docTermFreq
//						+ " time(s) in doc(" + searcher.doc(docid).get("DOCNO")
//						+ ") with document length " + docLeng+". Term occurs in "
//						+ df+" documents. Total number of docs: "+ dcount
//						+". Total score="+(((docTermFreq/docLeng)*Math.log(1+(df/dcount))))
//						);
//						
						if (hmap.containsKey(Integer.toString(docid))) {
							Float oldVal = hmap.get(Integer.toString(docid));
							hmap.put(Integer.toString(docid), oldVal+(float)((docTermFreq/docLeng)*Math.log(1+(df/dcount))));
						}else {hmap.put(Integer.toString(docid),  (float)((docTermFreq/docLeng)*Math.log(1+(df/dcount))));}
						
					}
				}
			}
			
			
			
		}
		reader.close();
		PriorityQueue<Entry> q = new PriorityQueue<Entry>();
		for (Map.Entry<String, Float> entry : hmap.entrySet())
		{
		    q.add(new Entry(entry.getKey(),entry.getValue()));
		}
		return q;
	}
	public static void main(String[] args) throws Exception {
		
		easySearch searchObj = new easySearch("/mnt/sda4/Desktop/search/ass2/index");
		PriorityQueue<Entry> result = searchObj.getTfIDFResults("Airbus flat");

		
        while (!result.isEmpty()) {
        	Entry entry = result.remove();
            System.out.println("Top document docid="
            		+entry.getKey()+" with value="+entry.getValue());
		
	}

}
}

/**
 * Data structure to store result
 * 
 *
 */
class Entry implements Comparable<Entry>{
    private String key;
    private Float value;
    public Entry(String key, Float value) {
    	setKey(key);
    	setValue(value);
    }
    public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int compareTo(Entry o) {
		// TODO Auto-generated method stub
		return o.getValue().compareTo(this.getValue());
	}
    
     
}


