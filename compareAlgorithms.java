package assignment2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {
	public static void main(String[] args) throws IOException, ParseException {
		//Vector space model
		createLuceneAlgoResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index", "/mnt/sda4/Desktop/search/ass2/results/VSM", new ClassicSimilarity());
		//BM25
		createLuceneAlgoResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index", "/mnt/sda4/Desktop/search/ass2/results/BM25", new BM25Similarity());
		//Language model with dirichlet smoothing
		createLuceneAlgoResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index", "/mnt/sda4/Desktop/search/ass2/results/LMDirichlet", new LMDirichletSimilarity());
		//Languge model with JelinekMercerSimilarity
		createLuceneAlgoResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index", "/mnt/sda4/Desktop/search/ass2/results/LMJelinekMercer", new LMJelinekMercerSimilarity((float)0.7));
		//Create result file for tfidf model
		searchTRECtopics.createTFIDFResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index","/mnt/sda4/Desktop/search/ass2/results/TFIDF");
	}
	
	/**
	 * This function creates the result file using the supplied lucene algorithm
	 * @param queriesFile Path of queries document
	 * @param indexFolder Path of the lucene index folder
	 * @param resultFile Path where to store the result file
	 * @param searchAlgo The lucene algorithm to use
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void createLuceneAlgoResultFile(String queriesFile, String indexFolder, String resultFile, Similarity searchAlgo) throws IOException, ParseException {
		
		ArrayList<query> arrayQ =  searchTRECtopics.parseFile(queriesFile);
		ArrayList<String> dataToFileShort = new ArrayList<String>();
		ArrayList<String> dataToFileLong = new ArrayList<String>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexFolder)));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(searchAlgo); 
		//You need to explicitly specify the ranking algorithm using the respective Similarity class
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		for (query q:arrayQ) {
			//get short query result
			System.out.println(q.getQNum());
			System.out.println(q.getShortQ());
			
			Query query = parser.parse(q.getShortQ());
			TopDocs topDocs = searcher.search(query, 1000);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (int i = 0; i < scoreDocs.length; i++) {
				Document doc = searcher.doc(scoreDocs[i].doc);
				
				dataToFileShort.add(Integer.parseInt(q.getQNum())+" "+"0"+" "+doc.get("DOCNO")+" "+(i+1)+" "+scoreDocs[i].score+" "+searchAlgo.toString().replace(" ", "-")+"-short");
			}
			
			System.out.println(q.getLongQ());
			query = parser.parse(q.getLongQ());
			topDocs = searcher.search(query, 1000);
			scoreDocs = topDocs.scoreDocs;
			for (int i = 0; i < scoreDocs.length; i++) {
				Document doc = searcher.doc(scoreDocs[i].doc);
				
				dataToFileLong.add(Integer.parseInt(q.getQNum())+" "+"0"+" "+doc.get("DOCNO")+" "+(i+1)+" "+scoreDocs[i].score+" "+searchAlgo.toString().replace(" ", "-")+"-long");
			}
		}
		reader.close();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile+"Short"))) {
			for (String toWrite: dataToFileShort) {
				bw.write(toWrite);
				bw.write("\n");
			}
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile+"Long"))) {
			for (String toWrite: dataToFileLong) {
				bw.write(toWrite);
				bw.write("\n");
			}
		}
		
	}
}
