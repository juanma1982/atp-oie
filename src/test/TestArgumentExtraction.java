package test;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ar.edu.unlp.ArgumentExtractorRegex;
import ar.edu.unlp.PatternLoader;
import ar.edu.unlp.StanfordCoreParser;
import ar.edu.unlp.entities.PatternContainer;
import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.entities.SentenceData;

public class TestArgumentExtraction {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		ArgumentExtractorRegex argumentExtractor = null;
		PatternContainer patternContainer = null;
		try {
			patternContainer = PatternLoader.loadPatternsFromJson();
		} catch (FileNotFoundException e1) {			
			e1.printStackTrace();
			fail();
		}
		argumentExtractor = new ArgumentExtractorRegex(patternContainer.getPatternsForArguments());
		
		try {
			StanfordCoreParser parser= new StanfordCoreParser();
			List<SentenceData> listOfParsedData = parser.doParser(TestConstants.SENTENCE_05);
			System.out.println(TestConstants.SENTENCE_05);
			for (SentenceData sentenceData : listOfParsedData) {
				Relation relation = new Relation();				
				relation.setEntity1("James Bakunin");
				relation.setRelation("were climaxed");
				List<String> arguments = argumentExtractor.argumentExtractorAll(sentenceData, relation.getRelation(), relation.getEntity1());				
				for (String argument : arguments) {
					relation.setEntity2(argument);
					if(relation.isComplete()){
						System.out.println(relation.toString());
					}
				}
			}
		} catch (Exception e) {			
			e.printStackTrace();
			fail();
		}
		
	}

}
