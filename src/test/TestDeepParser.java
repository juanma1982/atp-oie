package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ar.edu.unlp.DeepNNDependeciesParser;

public class TestDeepParser {

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
		try{
			DeepNNDependeciesParser depnn =new DeepNNDependeciesParser();
			String str = depnn.generateDependencyGrammaticalStructureForSentence(TestConstants.SENTENCE_01).toString();
			System.out.println(str);
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

}
