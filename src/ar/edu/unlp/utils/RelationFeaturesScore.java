package ar.edu.unlp.utils;

import ar.edu.unlp.constants.Words;
import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.entities.SentenceData;

public class RelationFeaturesScore {
	
	private SentenceData sentenceData = null;
	private SentenceManipulation utils = new SentenceManipulation();
		
	public static final int S_EQ_REL_01 = 116; /*If the statement contains the complate extraction and also they are long equal (except for one or two words) */
	public static final int REL_LAST_WORD_FOR_02 = 50; /*the last word of the relarion is FOR*/
	public static final int REL_LAST_WORD_ON_03 = 49; /*the last word of the relarion is ON*/
	public static final int REL_LAST_WORD_OF_04 = 46; /*the last word of the relarion is OF*/
	public static final int S_LEN_LT11_05 = 43;  /*amount of words in sentence is lower than 11*/
	public static final int LEFT_POS_REL_06 = 43; /*the word at the left of the relations, has a POS TAG starting with W*/
	public static final int REL_COMPLIANT_VWP_07 = 42; /*The relation compliant with the restriction given by VWP*/
	public static final int REL_LAST_WORD_TO_08 = 39; /*the last word of the relarion is TO*/
	public static final int REL_LAST_WORD_IN_09 = 25; /*the last word of the relarion is IN*/
	public static final int S_LEN_LT21_10 = 23; /*amount of words in sentence is lower than 21*/
	public static final int S_START_W_E1_11 = 21; /*sentence begin with Entity1*/
	public static final int E2_IS_PROPPER_NOUN_12 = 16; /*Entity2 is a properNoun*/
	public static final int E1_IS_PROPPER_NOUN_13 = 1; /*EntityE1 is a properNoun*/
	public static final int NP_AT_LEFT_OF_E1_14 = -30; /*At the left of entity01 is Noun Phrase*/
	public static final int S_LEN_GT21_15 = 0 ; /*amount of words in sentence is greater than 21*/

	public static final int REL_COMPLIANT_V_16 = -61; /*Relation compliant with V*/
	public static final int PREP_AT_LEFT_OF_E1_17 = -65; /*Preposition at the left of E1*/
	public static final int NP_AT_RIGHT_E2_18 = -81; /*There is a nounPhrase at the rigth of Entity2*/
	public static final int LEFT_OF_R_CC_19 = -93;  /*The word at the keft of the relation is a conjuntion, and, or, etc*/
	
	public static final int E2_EQ_E1_20 = -100; /*if entity 2 and entity1 are the same*/
	public static final int E2_EQ_IT_21 = -100; /*if entity 2 is the word "IT"*/
	public static final int REL_IN_E1_22 = -100; /*the hole relation is inside entity01*/
	public static final int E1_ENDS_LIKE_REL_STARTS_23 = -50; /*if the Entity01 ends with the same word that relation starts*/
	
	
	public static final int E2_IS_DT_20 = -1000; /*Entity2 is a determinant*/
	
	public RelationFeaturesScore() {		
	}
		
	public boolean isWH(String posToken) {
		return posToken.startsWith("W");
	}
		
	public boolean isW(String posToken) {
		
		if(posToken.startsWith("N") ||
		   posToken.startsWith("JJ") ||
		   posToken.startsWith("RB") ||
		   posToken.startsWith("PR") ||
		   posToken.startsWith("DT")) return true;
		
		return false;
	}
	public boolean isP(String posToken) {
		
		if(posToken.equals("IN") ||
				   posToken.equals("RP") ) return true;
		return false;
	}
	
	public boolean compliantVWP(String[] relationWords) {
		
		if(relationWords.length<3 || relationWords.length==0) return false;
		
		String firstPos = relationWords[0];
		String lastPos =  relationWords[relationWords.length-1];
		if(!firstPos.startsWith("V")) return false;
		if(!isP(lastPos)) return false;
		boolean wSetted = false;
		for (int i = 1; i < (relationWords.length-1); i++) {
			String currentpos = relationWords[i];
			
			if(i==1) {
				wSetted=isW(currentpos);
				if(!currentpos.equals("RP") && !currentpos.startsWith("RB") && !wSetted) return false; 
			}
			if(i==2) {
				wSetted=isW(currentpos);
				if(!currentpos.startsWith("RB") && !wSetted) return false;
			}
			if(i==3) {
				wSetted=isW(currentpos);
				if(!wSetted) return false;
			}
		}		
		if(wSetted) return true;
		return false;
	}
	
	public boolean compliantV(String[] posTagTokens) {

		if(posTagTokens.length<2 || posTagTokens.length>3) return false;
		if(!posTagTokens[0].startsWith("V")) return false;
		if(!posTagTokens[1].equals("RP")) return false;
		/*if(posTagTokens.length==2) {
			//if(!posTagTokens[1].startsWith("RB") && !posTagTokens[1].equals("RP")) return false;
			
		}*/
		if(posTagTokens.length==3) {			
			if(!posTagTokens[2].startsWith("RB")) return false;
		}		
		return true;
	}
	
	public int lastIndexofPOS(String[] relationWords, String posToken){
		for (int i = relationWords.length-1; i >=0; i--) {
			String currPos = sentenceData.getWordPOSTAG().get(relationWords[i]);
			if(posToken.equals(currPos)) return i;
		}
		return -1;
	}
	
	public int calculate(SentenceData sentenceData,Relation relation){
		int score = 0;
		
		try {
			this.sentenceData = sentenceData;
			String[] relationWords = relation.getRelation().split(" ");
			String[] entity01Words = relation.getEntity1().split(" ");			
			String[] sentenceWords = sentenceData.getCleanSentence().split(" ");
			
			StringBuilder posTagTokensSB = new StringBuilder();
			
			for (int i = 0; i < relationWords.length; i++) {
				posTagTokensSB.append(sentenceData.getWordPOSTAG().get(relationWords[i]));
				posTagTokensSB.append(" ");
			}
			String[] relationPosTagTokens = posTagTokensSB.toString().trim().split(" ");
			
			/* If the statement contains the complete extraction and also they are long equal plus a couple of punctuation marks, the characteristic 1 is fulfilled. 
			 * */
			if(sentenceData.getSentence().contains(relation.inRow()) && (relation.inRow().length())+2 >= sentenceData.getSentence().length()) {
				score += S_EQ_REL_01;
			}
			
			int index = lastIndexofPOS(relationWords,Words.IN);
			String lastPrepositionWord=null;
			if(index!=-1) {
				lastPrepositionWord=relationWords[index].toLowerCase();
				if("for".equals(lastPrepositionWord)) {
					score += REL_LAST_WORD_FOR_02;
				}else if("on".equals(lastPrepositionWord)) {
					score += REL_LAST_WORD_ON_03;
				}else if("of".equals(lastPrepositionWord)) {
					score += REL_LAST_WORD_OF_04;
				}else if("to".equals(lastPrepositionWord)) {
					score += REL_LAST_WORD_TO_08;
				}else if("in".equals(lastPrepositionWord)) {
					score += REL_LAST_WORD_IN_09;
				}
			}
			
			
			int sentenceLen = sentenceWords.length;
			if(sentenceLen<=10) {
				score +=S_LEN_LT11_05;
			}else if(sentenceLen<=20) {
				score +=S_LEN_LT21_10;
			}else {
				score +=S_LEN_GT21_15;
			}
			
			/*04 feature*/
			String posLeftOfR = utils.getPOSTagAtLeftOf(sentenceData,relation.getRelation());
			if(posLeftOfR.startsWith("W")) {
				score +=LEFT_POS_REL_06;
			}
					
			/*05 RELATION STRUCTURE*/
			if(compliantVWP(relationPosTagTokens)) {
				score +=REL_COMPLIANT_VWP_07;
			}
			
			
			/* 06 Sentence begin with */
			if(sentenceData.getCleanSentence().startsWith(relation.getEntity1())) {
				score +=S_START_W_E1_11;
				
			}
			
			/* 07 is entity01 a proper noun?*/
			if(!relation.getEntity1().contains(" ")) { //is a single word
				String e1PosTag = sentenceData.getWordPOSTAG().get(relation.getEntity1());
				if(e1PosTag!=null && !e1PosTag.isEmpty() && e1PosTag.startsWith(Words.PROPER_NOUN_POS_START_WITH)) {
					score +=E1_IS_PROPPER_NOUN_13;
				}
			}
			
			/* 08 is entity02 a proper noun?*/
			if(!relation.getEntity2().contains(" ")) { //is a single word
				String e2PosTag = sentenceData.getWordPOSTAG().get(relation.getEntity2());
				if(e2PosTag!=null && !e2PosTag.isEmpty()) { 
					if( e2PosTag.startsWith(Words.PROPER_NOUN_POS_START_WITH)) {
						score +=E2_IS_PROPPER_NOUN_12;
					}else if(e2PosTag.equals(Words.DT) || e2PosTag.equals(Words.PRP$)) {
						score +=E2_IS_DT_20;
					}else {
						for(int i=0;i<Words.BAD_ENDINGS_WORDS_FOR_ARGUMENT.length;i++) {
							if(relation.getEntity2().equals(Words.BAD_ENDINGS_WORDS_FOR_ARGUMENT[i])) {
								score +=E2_IS_DT_20;
								break;
							}
						}
						//if relation ends with: Said, told, added and e2 is one single word != than WILDCARD_QUOTED
						if(!relation.getEntity2().startsWith(Words.WILDCARD_QUOTED)) {
							for(int i=0;i<Words.SAID_AND_SYNONYMS.length;i++) {
								if(relation.getRelation().endsWith(Words.SAID_AND_SYNONYMS[i])) {
									score +=E2_IS_DT_20;
									break;
								}
							}	
						}
					}
				}
			}
			
			/*09 left of entity01*/
			String chunkedLeft = utils.getChunkedSentenceAtLeftOf(sentenceData, relation.getEntity1(),null);
			if(chunkedLeft!=null && !chunkedLeft.isEmpty() && chunkedLeft.endsWith(Words.Chunks.I_NP)){
				score +=NP_AT_LEFT_OF_E1_14;
			}
			
			/*05 RELATION STRUCTURE*/
			if(compliantV(relationPosTagTokens)) {
				score +=REL_COMPLIANT_V_16;
			}
			String posLeftOfE1 = utils.getPOSTagAtLeftOf(sentenceData,relation.getEntity1());
			if(posLeftOfE1.equals(Words.IN)){
				score +=PREP_AT_LEFT_OF_E1_17;
			}
			
			String chunkedRight =utils.getChunkedSentenceAtRightOf(sentenceData,  relation.getEntity2(),null);
			if(chunkedRight!=null && !chunkedRight.isEmpty() && chunkedRight.startsWith(Words.Chunks.B_NP)){
				score +=NP_AT_RIGHT_E2_18;
			}

			if(posLeftOfR.equals(Words.CC)) {
				score+=LEFT_OF_R_CC_19;
			}
			/*********/
			if(relation.getEntity1().toLowerCase().equals(relation.getEntity2().toLowerCase())) {
				score+=E2_EQ_E1_20;
			}
			if(relation.getEntity2().toLowerCase().equals("it")) {
				score+=E2_EQ_IT_21;
			}
			if(relation.getEntity1().contains(relation.getRelation())) {
				score+=REL_IN_E1_22;
			}
			if(entity01Words!=null && entity01Words.length>0 && relationWords!=null && relationWords.length>0) {
				if(entity01Words[entity01Words.length-1].equals(relationWords[0])) {
					score+=E1_ENDS_LIKE_REL_STARTS_23;
				}
			}
			
			
		}catch (Exception e) {
			System.err.println("error calculating score, relation: "+relation+". Message: "+e.getMessage());			
		}
		
		
		return score;
	}

}
