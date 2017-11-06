package assignment1;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;

public class GenerateIndex {

	/** Creates index from corpus
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		new GenerateIndex("/mnt/sda4/Desktop/search//index//KeywordAnalyzer","/mnt/sda4/Desktop/search/corpus", new KeywordAnalyzer());
//		new GenerateIndex("C://search//index//SimpleAnalyzer","C://search//corpus", new SimpleAnalyzer());
//		new GenerateIndex("C://search//index//StopAnalyzer","C://search//corpus", new StopAnalyzer());
//		new GenerateIndex("C://search//index//StandardAnalyzer","C://search//corpus", new StandardAnalyzer());
	}
	
	/**This is the constructor of index generator class
	 * @param indexDir Pass the directory where the created index is to be stored
	 * @param corpusDir Pass the corpus folder path
	 * @param analyzer Pass the analyzer for indexing
	 * @throws IOException
	 */
	public GenerateIndex(String indexDir, String corpusDir, Analyzer analyzer) throws IOException{
		System.out.println("Starting Index Construction");
		System.out.println(analyzer.toString());
		Directory dir = FSDirectory.open(Paths.get(indexDir ));
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	    iwc.setOpenMode(OpenMode.CREATE);
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    
	    File dir1 = new File(corpusDir);
		File[] files = dir1.listFiles();
		//Parsing each file
		for (File fileName : files) {
			if (fileName.toString().endsWith(".trectext")==true){
				parseFile(fileName, writer);
			}
			}
		writer.forceMerge(1);
		writer.commit();
		writer.close();
		System.out.println("Completed Index Construction");
	}
	
	/**Parses provided file
	 * @param File name
	 * @param writer Indexwriter java code
	 */
	private void parseFile(File fileName,IndexWriter writer){
		System.out.println(fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String sCurrentLine;
			Document lDoc = null;			
			String DOCNO = "";
			String HEAD = "";
			String BYLINE = "";
			String DATELINE = "";
			String TEXT = "";
			while ((sCurrentLine = br.readLine()) != null) {

				if (sCurrentLine.startsWith("<DOC>")){
					lDoc = new Document();
								
				}
				if (sCurrentLine.startsWith("<DOCNO>")){
					DOCNO = DOCNO.concat(dataBtwTags("</DOCNO>", sCurrentLine, br));
					DOCNO = DOCNO.concat(" ");
					}
				if (sCurrentLine.startsWith("<DATELINE>")){
					DATELINE = DATELINE.concat(dataBtwTags("</DATELINE>", sCurrentLine, br));
					DATELINE = DATELINE.concat(" ");
					}
				if (sCurrentLine.startsWith("<HEAD>")){
					HEAD = HEAD.concat(dataBtwTags("</HEAD>", sCurrentLine, br));
					HEAD = HEAD.concat(" ");
					}
				if (sCurrentLine.startsWith("<BYLINE>")){
					BYLINE = BYLINE.concat(dataBtwTags("</BYLINE>", sCurrentLine, br));
					BYLINE = BYLINE.concat(" ");
					}
				if (sCurrentLine.startsWith("<TEXT>")){
					TEXT = TEXT.concat(dataBtwTags("</TEXT>", sCurrentLine, br));
					TEXT = TEXT.concat(" ");
					
				}
				
				if (sCurrentLine.startsWith("</DOC>")){
					lDoc.add(new StringField("DOCNO", DOCNO,Field.Store.YES));
					DOCNO = "";
					lDoc.add(new StringField("DATELINE", DATELINE,Field.Store.YES));
					DATELINE = "";
					lDoc.add(new StringField("HEAD", HEAD,Field.Store.YES));
					HEAD = "";
					lDoc.add(new StringField("BYLINE", BYLINE,Field.Store.YES));
					BYLINE = "";
					lDoc.add(new TextField("TEXT", TEXT,Field.Store.YES));
					TEXT = "";
					writer.addDocument(lDoc);
					lDoc = null;
					
					
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Extracts data between tags
	 * @param EndStr Ending tag
	 * @param CurrentLine The string value at current line position
	 * @param FilePos File position data structure
	 * @return String data between tags
	 * @throws IOException
	 */
	private String dataBtwTags(String EndStr, String CurrentLine, BufferedReader FilePos) throws IOException{
		String data = CurrentLine;
		if (!CurrentLine.endsWith(EndStr)){
			data = data.concat(" ");
			do {
				CurrentLine = FilePos.readLine();
				data = data.concat(CurrentLine);
				data = data.concat(" ");
			}while(!CurrentLine.endsWith(EndStr));
		}
		List<String> strings = Arrays.asList(data.replaceAll("^.*?>", "").split("</.*?(</|$)"));
		
		return strings.get(0).trim();
		
	}
}
	


