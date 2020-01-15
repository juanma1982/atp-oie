package test;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ar.edu.unlp.ClausIEExtractorUtility;
import ar.edu.unlp.ReadTestFile;
import ar.edu.unlp.entities.Relation;

public class TestClausIE {

	
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
		ClausIEExtractorUtility clausIEExtractor = null;
		try {
			clausIEExtractor = new ClausIEExtractorUtility();
			
			if(!clausIEExtractor.checkServerIsUp()) {
				fail("Not alive");
			}else {
				System.out.println("Server is alive");
			}
			System.out.println("initialization ends");
		} catch (Exception e1) {
			e1.printStackTrace();
			fail();
		}
		
		List<Relation> relationList = new ArrayList<Relation>();
		ReadTestFile rtf = new ReadTestFile();
		try {
			rtf.startReading();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
			
		}
		String line = null;
		try {
			line = rtf.readNextLine();
			while( line!=null ){
				relationList.addAll(clausIEExtractor.processSentence(line));
				line = rtf.readNextLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		for (Relation relation : relationList) {
			System.out.println(relation.toStringScore());
		}
		clausIEExtractor.turnOffServer();
	}

}
