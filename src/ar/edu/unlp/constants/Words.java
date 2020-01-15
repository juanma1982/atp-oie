package ar.edu.unlp.constants;

import java.util.HashMap;
import java.util.Map;

public class Words {

	public class Chunks{
		
		public static final String B_NP = "B-NP";
		public static final String I_NP = "I-NP";
		public static final String B_ADVP = "B-ADVP";
		public static final String I_ADVP = "I-ADVP";
		public static final String BEGIN_LETTER = "B";
		public static final String CONTINUE_LETTER = "I";
	}
	
	public static final String IN = "IN";
	public static final String DT = "DT";
	public static final String CC = "CC";
	public static final String PRP$ = "PRP$";
	

	public static final String[] SAID_AND_SYNONYMS= {"said",  "told", "added", "announced", "asserted","believe","believed"};
	public static final String[] SAID_END_POINTS= {", ",  ";", "\""}; //added the space to the como to avoid numbers
	
	
	public static final String[] BAD_ENDINGS_WORDS_FOR_ARGUMENT= {"that",  "on", "at", "with", "a", "to", "and", "an", "by", "from", "in", "as"};
	public static final String[] GOOD_START_WORDS_FOR_ARGUMENT= {"that", "on", "at", "with", "a", "an", "by", "in", "from", "to" };
	public static final String[] PHRASAL_VERBS_COMMON_SECOND_WORDS = {"away", "by", "down", "for", "in", "into", "off", "on", "of", "out", "over", "up"};
	public static final String[] ENDING_POSTAGS_TO_REMOVE= {".", ","};
	public static final String[] NP_CONNECTORS= {"to", "of", "at"};
	
	public static final String NER_OTHER = "O";
	public static final String WORD_WILDCARD_NER_FULL = "_";
	public static final String VERB_POS_FIRST_LETTER = "V";	
	public static final String PROPER_NOUN_POS_START_WITH= "NNP";
	
	public static final String WILDCARD_QUOTED = "#QUOTED";
	public static final String WILDCARD_QUOTED_REVERB_SET = "# QUOTED"; 
	public static final String WILDCARD_QUOTED_POS = "NN";
	public static final String WILDCARD_ERROR_REPLACE = "something";
	public static final String WILDCARD_LEADING_ZEROES_FORMAT = "%03d";
	public static final int WILDCARD_LEADING_ZEROES_COUNT = 3;
	
	/**/
	public static final Map<String , String> APOSTROPHEMAP = new HashMap<String , String>() {{
		put("'s","is");
		put("'ll","will");
		put("'ve","have");
		put("'re","are");
	}};
	
}
