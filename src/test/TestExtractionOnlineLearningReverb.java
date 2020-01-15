package test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ar.edu.unlp.ReadTestFile;
import ar.edu.unlp.RelationExtractor;
import ar.edu.unlp.constants.Filenames;
import ar.edu.unlp.entities.Relation;
import test.utils.PatternFileTestUt;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestExtractionOnlineLearningReverb {
	
	public static final String BACKUP_FILENAME = Filenames.JSON_PATTERNS+".backup";
	public static RelationExtractor relationExtractorStatic = null;
	
	protected static long currentSize = 0;
	protected RelationExtractor extractor = null;
	
	@BeforeClass
    public static void beforeAllTestMethods() {
		try {
			PatternFileTestUt.fileSwap(Filenames.JSON_PATTERNS, TestExtractionOnlineLearningReverb.BACKUP_FILENAME);
			TestExtractionOnlineLearningReverb.relationExtractorStatic = new RelationExtractor(true,false);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
 
    @AfterClass
    public static void afterAllTestMethods() {    	
		try {
			PatternFileTestUt.fileSwap(Filenames.JSON_PATTERNS, Filenames.JSON_PATTERNS+".testResult.json");
			PatternFileTestUt.fileSwap(TestExtractionOnlineLearningReverb.BACKUP_FILENAME, Filenames.JSON_PATTERNS);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
	 
    public TestExtractionOnlineLearningReverb(){
    	try {
			this.extractor = TestExtractionOnlineLearningReverb.relationExtractorStatic;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
	
	@Test	
	public void testA_First() {
		 System.out.println("");
		System.out.println(" First test: creation of Pattern JSON from scrach ");
		System.out.println(" -------------- ");
		try{ 
			 ReadTestFile rtf = new ReadTestFile();
			 rtf.startReading();
			 String line = rtf.readNextLine();
			 while(line!=null ){
				 List<Relation> relations = extractor.extractInformationFromParagraph(line);
				 if(relations!=null && !relations.isEmpty()){
						System.out.println(line);
						for (Relation relation : relations) {						
							System.out.print(relation.toString());
							System.out.println(" =>  ("+relation.getScore()+")");
						}
						System.out.println();
				 }else{
					 System.err.println("No relation extracted!");
					 fail("No relation extracted!");
				 }
				 line = rtf.readNextLine();
			 }		
			 rtf.endRead();
			 extractor.saveExtractionPatternsToFile();
			 System.out.println("");
			 System.out.println(" == Patterns Generated == ");
			 System.out.println("");
			 BufferedReader br = new BufferedReader(new FileReader(Filenames.JSON_PATTERNS));
			 String lineJson = null;
			 while ((lineJson = br.readLine()) != null) {
			   System.out.println(lineJson);
			 }
			 br.close();
			 File patternFiles = new File(Filenames.JSON_PATTERNS);
			 currentSize = patternFiles.length();
			 
			 System.out.println("");
			 System.out.println(" File size: "+currentSize+" bytes ");
			 System.out.println("");
			
			}catch(Exception e){
				e.printStackTrace();
				fail(e.getMessage());
			}
	}
	
	@Test
	public void testB_Second() {
		 System.out.println("");
		 System.out.println(" Second test: Extract realtions without updating JSON Patterns File, becasuse score is not enougth  ");
		 System.out.println(" -------------- ");
		try{
			 			 
			 ReadTestFile rtf = new ReadTestFile(1);
			 rtf.startReading();
			 String line = rtf.readNextLine();
			 while(line!=null ){
				 List<Relation> relations = extractor.extractInformationFromParagraph(line);
				 if(relations!=null && !relations.isEmpty()){
						System.out.println(line);
						for (Relation relation : relations) {						
							System.out.print(relation.toString());
							System.out.println(" =>  ("+relation.getScore()+")");
						}
						System.out.println();
				 }else{
					 System.err.println("No relation extracted!");
					 fail("No relation extracted!");
				 }
				 line = rtf.readNextLine();
			 }		
			 rtf.endRead();
			 extractor.saveExtractionPatternsToFile();
			 
			 File patternFiles = new File(Filenames.JSON_PATTERNS);
			 long secondSize = patternFiles.length();
			 System.out.println("");
			 System.out.println(" File size: "+secondSize+" bytes ");
			 System.out.println("");
			 if(secondSize != currentSize) {
				 fail("Pattern file was updated, but shouldn't. Becasuse extractions have 0 score");
			 }
			 
			}catch(Exception e){
				e.printStackTrace();
				fail(e.getMessage());
			}
	}
	
	@Test
	public void testC_Third() {
		System.out.println("");
		System.out.println(" Third test: Update existing JSON Pattern file ");
		System.out.println(" -------------------------------------------- ");
		try{
				String line = "The girls want to protect themselves.";
				 List<Relation> relations = extractor.extractInformationFromParagraph(line);
				 if(relations!=null && !relations.isEmpty()){
						System.out.println(line);
						for (Relation relation : relations) {						
							System.out.print(relation.toString());
							System.out.println(" =>  ("+relation.getScore()+")");
						}
						System.out.println();
				 }else{
					 System.err.println("No relation extracted!");
					 fail("No relation extracted!");
				 }
				 extractor.saveExtractionPatternsToFile();
				 
				 File patternFiles = new File(Filenames.JSON_PATTERNS);
				 long thirdSize = patternFiles.length();
				 System.out.println("");
				 System.out.println(" File size: "+thirdSize+" bytes ");
				 System.out.println("");
				 if(thirdSize <= currentSize) {
					 System.err.println("Pattern file was not updated");
					 fail("Pattern file was not updated");
				 }
				 currentSize = thirdSize;
		 
		}catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testD_Four() {
		System.out.println("");
		System.out.println(" Four test: Using genereted patterns to extract a new sentence without update Patterns file");
		System.out.println(" -------------- ");
		try{
				 String line = "Albert Einstein was awarded the Nobel Prize in Sweden in 1921.";
				 List<Relation> relations = extractor.extractInformationFromParagraph(line);
				 if(relations!=null && !relations.isEmpty()){
						System.out.println(line);
						for (Relation relation : relations) {
							System.out.print(relation.toString());
							System.out.println(" =>  ("+relation.getScore()+")");
							if(relation.isFromReverb()) {
								System.err.println("Was extrated using Reverb!!!");
								fail("Was extrated using Reverb!!!");
							}
						}
						System.out.println();
				 }else{
					 System.err.println("No relation extracted!");
					 fail("No relation extracted!");
				 }
				 extractor.saveExtractionPatternsToFile();
				 
				 File patternFiles = new File(Filenames.JSON_PATTERNS);
				 long fourdSize = patternFiles.length();
				 System.out.println("");
				 System.out.println(" File size: "+fourdSize+" bytes ");
				 System.out.println("");
				 if(fourdSize != currentSize) {
					 fail("Pattern file was not updated");
				 }
		 
		}catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
