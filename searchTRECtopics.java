package assignment2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class searchTRECtopics {

	public static void main(String[] args) throws IOException, ParseException {
//		createTFIDFResultFile("/mnt/sda4/Desktop/search/ass2/topics.51-100","/mnt/sda4/Desktop/search/ass2/index","/mnt/sda4/Desktop/search/ass2/results/TFIDF");
		parseFile("/mnt/sda4/Desktop/search/ass2/topics.51-100");
	}
	
	/**
	 * This function parses the query file and creates the result file for TFIDF searcher implemented in easySearch for all parsed long and short queries.
	 * @param queriesFile Path of queries document
	 * @param indexFolder Path of the lucene index folder
	 * @param resultFile Path where to store the result file
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void createTFIDFResultFile(String queriesFile, String indexFolder, String resultFile)  throws IOException, ParseException{
		ArrayList<query> arrayQ =  parseFile(queriesFile);
		ArrayList<String> dataToFileShort = new ArrayList();
		ArrayList<String> dataToFileLong = new ArrayList();
		easySearch searchObj = new easySearch(indexFolder);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexFolder)));
		IndexSearcher searcher = new IndexSearcher(reader);
		for (query q:arrayQ) {
			//get short query result
			System.out.println(q.getQNum());
			System.out.println(q.getShortQ());
			PriorityQueue<Entry> resultShort = searchObj.getTfIDFResults(q.getShortQ());
			int resCount = 1;
			while (!resultShort.isEmpty()&&resCount<=1000) {
		    	Entry searchDocRes = resultShort.remove();
		    	String DocName = searcher.doc(Integer.parseInt(searchDocRes.getKey())).get("DOCNO");
		    	dataToFileShort.add(Integer.parseInt(q.getQNum())+" "+"0"+" "+DocName+" "+resCount+" "+searchDocRes.getValue()+" "+"TFIDF-short");
		    	resCount=resCount+1;
			}
			//get long query result
			System.out.println(q.getLongQ());
			PriorityQueue<Entry> resultLong = searchObj.getTfIDFResults(q.getLongQ());
			resCount = 1;
			while (!resultLong.isEmpty()&&resCount<=1000) {
		    	Entry searchDocRes = resultLong.remove();
		    	String DocName = searcher.doc(Integer.parseInt(searchDocRes.getKey())).get("DOCNO");
		    	dataToFileLong.add(Integer.parseInt(q.getQNum())+" "+"0"+" "+DocName+" "+resCount+" "+searchDocRes.getValue()+" "+"TFIDF-Long");
		    	resCount=resCount+1;
			}
		}

		
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
	
	/**
	 * Function parses the queries document to return the short and long queries.
	 * @param fileName Path to the queries file
	 * @return
	 */
	public static ArrayList<query> parseFile(String fileName){
		System.out.println(fileName);
		ArrayList<query> arrayQ = new <query>ArrayList();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
						
			String num = "";
			String title = "";
			String desc = "";
			sCurrentLine = br.readLine();
			do {
				int i=0;
				if (sCurrentLine.startsWith("<top>")){
					while(!sCurrentLine.startsWith("</top>")) {
						if (sCurrentLine.startsWith("<num>")) {
							do {
								num = num.concat(sCurrentLine);
								sCurrentLine = br.readLine();
							}while (!sCurrentLine.startsWith("<"));
							
						}
						else if (sCurrentLine.startsWith("<title>")) {
							do {
								title = title.concat(sCurrentLine);
								sCurrentLine = br.readLine();
							}while (!sCurrentLine.startsWith("<"));
						}
						else if (sCurrentLine.startsWith("<desc>")) {
							do {
								desc = desc.concat(sCurrentLine);
								sCurrentLine = br.readLine();
							}while (!sCurrentLine.startsWith("<"));
						}
						else {sCurrentLine = br.readLine();}
					
				}
//					System.out.println(num+title+desc);
					arrayQ.add(new query(num.trim().substring(num.length() - 3).trim()
							,title.replace("<title>", "").replace("Topic:", "").replace("/", " ").trim()
							,desc.replace("<desc>", "").replace("Description:", "").replace("/", " ").trim()));
					num = "";
					title = "";
					desc = "";
				
			}
			}while ((sCurrentLine = br.readLine()) != null);

		} catch (IOException e) {
			e.printStackTrace();
		}
//		for (query q: arrayQ) {System.out.println(q.toString());}
		return arrayQ;
	}
	

}

/**
 * Data Structure to store queries
 * @author alienware
 *
 */
class query {
	private String qNum;
    private String shortQ;
    private String longQ;
	public query(String qNum, String shortQ, String longQ) {
		this.qNum = qNum;
		this.shortQ = shortQ;
		this.longQ = longQ;
	}
	public String getQNum() {
		return this.qNum;
	}
	public String getShortQ() {
		return this.shortQ;
	}
	public String getLongQ() {
		return this.longQ;
	}
	public String toString() {
		return this.qNum+"|||"+this.shortQ+"|||"+this.longQ;
	}
}
