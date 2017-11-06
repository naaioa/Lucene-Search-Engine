package assignment1;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class IndexComparison {
	public static void main(String[] args) throws Exception {
		
		getIndexInfo("/mnt/sda4/Desktop/search//index_ass1//Results//KeywordAnalyzer.txt", "/mnt/sda4/Desktop//search//index_ass1//KeywordAnalyzer");
//		getIndexInfo("C://search//index//Results//SimpleAnalyzer.txt", "C://search//index//SimpleAnalyzer");
//		getIndexInfo("C://search//index//Results//StopAnalyzer.txt", "C://search//index//StopAnalyzer");
//		getIndexInfo("C://search//index//Results//StandardAnalyzer.txt", "C://search//index//StandardAnalyzer");
		
		System.out.println("Completed Lucene  demo");
	}
	
	public static void getIndexInfo(String fileName, String pathToIndex) throws Exception {
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get( (pathToIndex))));

		//Print the total number of documents in the corpus
		writer.println("Total number of documents in the corpus: "+reader.maxDoc());                            
		//Print the number of documents containing the term "new" in <field>TEXT</field>.
		writer.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
		//Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
		writer.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));                                                       				                                                               
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
		//Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
		writer.println("Size of the vocabulary for this field: "+vocabulary.size());
		//Print the total number of documents that have at least one term for <field>TEXT</field>
		writer.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
		//Print the total number of tokens for <field>TEXT</field>
		writer.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
		//Print the total number of postings for <field>TEXT</field>
		writer.println("Number of postings for this field: "+vocabulary.getSumDocFreq());      

		//Print the vocabulary for <field>TEXT</field>
		TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		writer.println("\n*******Vocabulary-Start**********");
		while((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			writer.println(term);
		}
		writer.println("\n*******Vocabulary-End**********");        
		reader.close();
		System.out.println("Completed Creating Result file");
	}

}
