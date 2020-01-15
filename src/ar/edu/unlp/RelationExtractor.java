package ar.edu.unlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import com.google.gson.JsonIOException;

import ar.edu.unlp.constants.Constants;
import ar.edu.unlp.constants.Words;
import ar.edu.unlp.entities.Pattern;
import ar.edu.unlp.entities.PatternArgumentList;
import ar.edu.unlp.entities.PatternContainer;
import ar.edu.unlp.entities.PatternList;
import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.entities.SentenceData;
import ar.edu.unlp.utils.ParagraphStanford;
import ar.edu.unlp.utils.RelationFeaturesScore;
import ar.edu.unlp.utils.SentenceManipulation;
import ar.edu.unlp.utils.StringLengthComparator;

public class RelationExtractor {
	
	protected StanfordCoreParser parser = null;	
	protected ParagraphStanford paragraph=null;
	public static final boolean USE_ARGUMENT_PATTERNS = true;
	public static final boolean GET_RELATION_POSTAGS = false;
	public static final boolean CALCULATE_SCORE = true;
	public static final boolean CHECK_NON_FACTUAL = true;
	public static final int MIN_SCORE_FILTER = -1;
	
	protected PatternContainer patternContainer; 
	protected PatternList treePatternList;
	protected PatternArgumentList argumentPatternList;
	protected RelationFeaturesScore scoreCalculator = null;
	protected SentenceManipulation sentenceManipulation = null;
	protected ReVerbExtractorUtility reverbExtractor = null;
	protected ClausIEExtractorUtility clausIEExtractor = null;
	protected boolean useReverb = false;
	protected boolean useClausIE = false;
	protected boolean useOnlineTraining = false;
	protected TrainOnline trainOnline = null;
	
		
	protected ArgumentExtractorReverbStyle argumentExtractor = null;
	
	public RelationExtractor() throws Exception{		
		this(false,false,false);
	}
	public RelationExtractor(boolean useReverb,boolean useClausIE) throws Exception{		
		this(useReverb,useClausIE,false);
	}
	
	public RelationExtractor(boolean useReverb,boolean useClausIE, boolean useOnlineTraining) throws Exception{		
		this.setUseClausIE(useClausIE);
		parser = new StanfordCoreParser();
		paragraph = new ParagraphStanford();
		scoreCalculator =  new RelationFeaturesScore();
		sentenceManipulation = new SentenceManipulation();
		this.setUseReverb(useReverb);
		this.checkClausIEisUp();
		loadPatterns();
		
		this.setUseOnlineTraining(useOnlineTraining);
		
	}
	
	public void loadPatterns() throws Exception{
		try {
			this.patternContainer = PatternLoader.loadPatternsFromJson();
		}catch(Exception e) {
			if(!useOnlineTraining) throw e;
			if(!useReverb && !useClausIE) throw e;
			this.patternContainer = new PatternContainer();
			this.patternContainer.setTreePatterns(new PatternList());
			this.patternContainer.setPatternsForArguments(new PatternArgumentList());
		}
		this.treePatternList = this.patternContainer.getTreePatterns();
		this.argumentPatternList = this.patternContainer.getPatternsForArguments();
		//argumentExtractor = new ArgumentExtractor(this.argumentPatternList);		
		argumentExtractor = new ArgumentExtractorReverbStyle();
	}
	
	public List<Relation> extractInformationFromParagraph(String paragraphLine) throws Exception{
		return extractInformationFromParagraphGeneral(paragraphLine, false);
	}
	
	public List<Relation> extractInformationFromParagraphOnlyReverb(String paragraphLine) throws Exception{
		setUseReverb(true);
		return extractInformationFromParagraphGeneral(paragraphLine, true);
	}
	protected List<Relation> extractInformationFromParagraphGeneral(String paragraphLine, boolean useOnlyReverb) throws Exception{
		List<Relation> relations = new ArrayList<Relation>();
		if(paragraphLine == null) return relations;
		
		Map<String, String> mapOfReplacement = new HashMap<String,String>();
		String[] sentences = paragraph.splitIntoSentences(paragraphLine,mapOfReplacement);
		
		for (int i=0;i<sentences.length;i++) {
			String line = sentences[i];
			if(useOnlyReverb) {
				relations.addAll(extractInformationFromLineUsingReverb(line));
			}else {
				relations.addAll(extractInformationFromLine(line));
			}
		}
		this.replacedQuotedInRelations(relations,mapOfReplacement);
		if(useOnlineTraining) {
			this.trainOnline.save();
		}
		return relations;
	}
	
	private void replacedQuotedInRelations(List<Relation> relations,Map<String, String> mapOfReplacement) {
		
		for (Iterator<Relation> iter = relations.listIterator(); iter.hasNext(); ) {
			Relation relation = iter.next();
			if(relation.getEntity1().contains(Words.WILDCARD_QUOTED)){
		    	 iter.remove();
		    	 continue;
		    }
			if(relation.getRelation().contains(Words.WILDCARD_QUOTED)){
		    	 iter.remove();
		    	 continue;
		    }
			int index = relation.getEntity2().indexOf(Words.WILDCARD_QUOTED);
			if(index > -1){				
				String keyWord = relation.getEntity2().substring(index,index+Words.WILDCARD_QUOTED.length()+Words.WILDCARD_LEADING_ZEROES_COUNT);
				relation.setEntity2(relation.getEntity2().replace(keyWord, "\""+mapOfReplacement.get(keyWord)+"\""));
		    }
		}
	}

	public List<Relation> extractInformationFromLine(String line) throws Exception{
		List<Relation> relations = new ArrayList<Relation>();
		if(line==null) return relations;
		List<SentenceData> listOfParsedData = parser.doParser(line);		
		for (SentenceData sentenceData : listOfParsedData) {
			Set<Relation> relationsAux = extractInformationFromXMLTree(sentenceData);
				if(relationsAux.size() > 0) {
					if(CALCULATE_SCORE) {
						for (Relation relation : relationsAux) {
							if(relation.getScore() > MIN_SCORE_FILTER)
								relations.add(relation);
						}
					}else {
						relations.addAll(relationsAux) ;
					}
				}
					
				if(relations.size() <= 0) {
					
						List<Relation> relationsExtra = null;
						if(this.useReverb) {
							relationsExtra = reverbExtractor.extractRelationsFromLine(sentenceData.getSentence());				
							for (Relation relation : relationsExtra) {
								relation.setFromReverb(true);
								if(CALCULATE_SCORE) {
									relation.setScore(scoreCalculator.calculate(sentenceData, relation));
									if(relation.getScore() > MIN_SCORE_FILTER) {
										relations.add(relation) ;
									}
								}
							}
							if(!CALCULATE_SCORE) {								
								relations.addAll(relationsExtra) ;
							}
						}
					
						if(this.useClausIE && (relations == null || relations.isEmpty())) {
							relationsExtra =  clausIEExtractor.processSentence(sentenceData.getSentence());
							for (Relation relation : relationsExtra) {
								relation.setFromClausIE(true);
								if(CALCULATE_SCORE) {
									relation.setScore(scoreCalculator.calculate(sentenceData, relation));
									if(relation.getScore() > MIN_SCORE_FILTER) {
										relations.add(relation) ;
									}
								}
							}
							if(!CALCULATE_SCORE) {								
								relations.addAll(relationsExtra) ;
							}
						}
						
						if(CALCULATE_SCORE && useOnlineTraining && relationsExtra!=null && !relationsExtra.isEmpty()) {
							this.trainOnline.extractNewPatternFromRelation(relationsExtra,sentenceData);
						}
				}
				
			}//end for
			if(CHECK_NON_FACTUAL) {
				checkNonFactualExtractions(relations);
			}		
		return relations;
	}
	
	public List<Relation> extractInformationFromLineUsingReverb(String line) throws Exception{
		List<Relation> relations = new ArrayList<Relation>();
		if(line==null) return relations;
		List<SentenceData> listOfParsedData = parser.doParser(line);		
		for (SentenceData sentenceData : listOfParsedData) {
			
				List<Relation> relationsAuxReverb = reverbExtractor.extractRelationsFromLine(sentenceData.getSentence());
				
				for (Relation relation : relationsAuxReverb) {
					relation.setFromReverb(true);
					relation.setSourceSentence(sentenceData.getSentence());
					if(CALCULATE_SCORE) {
						relation.setScore(scoreCalculator.calculate(sentenceData, relation));
					}
				}
				
				relations.addAll(relationsAuxReverb) ;
			
		}
		if(CHECK_NON_FACTUAL) {
			checkNonFactualExtractions(relations);
		}
		return relations;
	}
	
	private void checkNonFactualExtractions(List<Relation> relations) {
		int id=-1;
		String argument = "";
		for (Relation relation : relations) {
			if(this.argumentExtractor.relationIsSaid(relation.getRelation())) {
				id= relation.getId();
				argument = relation.getEntity2();
				break;
			}
		}
		if(id>-1) {
			for (Relation relation : relations) {
				if(relation.getId() == id) continue;
				if(argument.contains(relation.getEntity1()) && argument.contains(relation.getRelation())
						&& argument.contains(relation.getEntity2())) {
					relation.setDependsOf(id);
					break;
				}
			}
		}
		
	}

	/**
	 * This method will execute some improvements:<br/>
	 * -Try to detect if the relation is a phrasal verb, splited into relation and argument. And if it's so<br/>
	 *  will join the phrasal verb together into the relation
	 * @param relations
	 */
	/*private void improveRelations(List<Relation> relations) {
		for (Relation relation : relations) {
			for(String startingWords: Words.PHRASAL_VERBS_COMMON_SECOND_WORDS) {
				if(relation.getEntity2().startsWith(startingWords+" ")) {
					and 
				}
			}	
		}
	}*/

	private String extractFirst(Pattern pattern, Document doc, String words){
		if(words == null){
			words = "";
		}		
		Pattern currentPattern = pattern;
		Element element = doc.select(currentPattern.getPatternStr()).first();				
		if(element == null){ return null;}
		
		if(!words.isEmpty()){
			words = words+ " ";
		}
		words = words+ element.attr("word");
		if(pattern.isLeaf()){
		 return words;
		}
			
		List<Pattern> nextPatterns =  pattern.getListOfNext();
		if(nextPatterns != null){
			for (Pattern nextPattern : nextPatterns) {
				String aux = this.extractFirst(nextPattern, doc, words);
				if(aux != null) return aux;
			}
		}
		return null;
	}
	
	private void extractInformation(Pattern pattern, Document doc, Map<String, String> listOfExtractions, String words, String patterStr){
		if(words == null){
			words = "";
		}
		if(patterStr == null){
			patterStr = "";
		}
		Pattern currentPattern = pattern;
		Element element = doc.select(currentPattern.getPatternStr()).first();
		if(element != null){
			if(!words.isEmpty()){
				words = words+ " ";
			}
			if(!patterStr.isEmpty()){
				patterStr = patterStr+ " ";
			}
			words = words+ element.attr("word").replaceAll("&apos;", "'");
			patterStr = patterStr+ currentPattern.getPatternStr();			
			if(pattern.isLeaf()){			
				listOfExtractions.put(patterStr, words);
			}
			
			List<Pattern> nextPatterns =  pattern.getListOfNext();
			if(nextPatterns != null){
				for (Pattern nextPattern : nextPatterns) {
					this.extractInformation(nextPattern, doc,listOfExtractions, words, patterStr);
				}
			}
		}
	}
	
	/**
	 * Make some validations in the extracted subject (entity 1), in order to improve it
	 * @param subjectCanidate
	 * @param sentenceData
	 * @return
	 */
	public String validateSubject(String subjectCanidate,SentenceData sentenceData){
		if(subjectCanidate == null) return null;
		if(!subjectCanidate.contains(" ")){ //It means: contain only one word 
			String newSubject = sentenceData.getWordNER().get(subjectCanidate+Words.WORD_WILDCARD_NER_FULL);
			if(newSubject!=null) return newSubject;
		}
		if(!sentenceData.getCleanSentence().contains(subjectCanidate)){ 
			//TODO FIX: if the word order is different from the reading order (left to right), it can extract anything wrong, verify with: "U.S. farmers who in the past have grown oats for their own use but failed to certify to the government that they had done so probably will be allowed to continue planting that crop and be eligible for corn program benefits, an aide to Agriculture Secretary Richard Lyng said."
			subjectCanidate = completeSubject(subjectCanidate,sentenceData.getCleanSentence());
		}
		if(subjectCanidate == null) return null;
		//Add the "The" at Left if its exists
		String WordAtLeft = this.sentenceManipulation.getWordAtLeftOf(sentenceData, subjectCanidate);
		String POSTtAtLeft =null;
		if(WordAtLeft!=null && !WordAtLeft.isEmpty()) {
			POSTtAtLeft = sentenceData.getWordPOSTAG().get(WordAtLeft);
		}
		if(POSTtAtLeft!=null && POSTtAtLeft.equals(Words.DT)) {
			subjectCanidate =WordAtLeft+" "+subjectCanidate;
		}
		////////////////////////

		if(!subjectCanidate.isEmpty()) {
			String nounPhraseAtRigth = this.sentenceManipulation.getTheRestOfTheNounPhrase(sentenceData, subjectCanidate);
			if(nounPhraseAtRigth!=null && !nounPhraseAtRigth.isEmpty()) {
				subjectCanidate = subjectCanidate+" "+nounPhraseAtRigth;
			}
		}else {
			return null;
		}
		return subjectCanidate;
	}
	
	/**
	 * When a subject is extracted, but it's a string which not appears in the text, ie: "A spokeman", an the full text:<br/>
	 * "A defense spokesman added that British officials ..."<br/>
	 * This method creates a pattern in the form: "A(.+)spokeman" to get the full the subject: "A defense spokesman"
	 *   
	 * @param subjectCanidate
	 * @param fullText
	 * @return the improved subjectCanidate
	 */
	public String completeSubject(String subjectCanidate,String fullText){
		
		
		//String[] fulltextWords = fullText.split(" ");
		String[] subjectWords = subjectCanidate.split(" ");
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int j = 0; j < subjectWords.length; j++) {
			sb.append(subjectWords[j]);
			if(j < (subjectWords.length-1)){
				count++;
				sb.append("(.+)");	
			}
		}
		java.util.regex.Pattern r = java.util.regex.Pattern.compile(sb.toString());
		java.util.regex.Matcher m = r.matcher(fullText);		
		if (m.find()) {
			for (int i = 1; i <= count; i++) {
				String argMatched = m.group(i);
				if (argMatched == null || argMatched.isEmpty()) return null;
				if(argMatched.trim().split(" ").length > Constants.MIN_WORD_DISTANCE+1) return null;
			}
			return m.group(0);
		}
		return null;
		
	}
	
	public void validatePhrasalVerbs(SentenceData sentenceData, Map<String, String> currentRelationExtraction) {
		
		for (String key : currentRelationExtraction.keySet()) {
			String relation = currentRelationExtraction.get(key);
			String[] relationWords = relation.split(" ");
			String lasPOS = sentenceData.getWordPOSTAG().get(relationWords[relationWords.length-1]);
			if(lasPOS!=null && !lasPOS.isEmpty() && lasPOS.startsWith(Words.VERB_POS_FIRST_LETTER)) {
				String wAtRigth = sentenceManipulation.getWordAtRightOf(sentenceData, relation+" ");
				if(wAtRigth!=null && !wAtRigth.isEmpty()) {
					for (int i = 0; i < Words.PHRASAL_VERBS_COMMON_SECOND_WORDS.length; i++) {
						if(wAtRigth.equals(Words.PHRASAL_VERBS_COMMON_SECOND_WORDS[i])) {
							currentRelationExtraction.put(key, relation+" "+wAtRigth);
							break;
						}
					}
				}
			}
		}
	}
	
	private Set<Relation> extractInformationFromXMLTree(SentenceData sentenceData) throws Exception {
		
		String treeDependenciesLine = sentenceData.getTreeDependenciesLine();
		Document doc = Jsoup.parse(treeDependenciesLine, "", Parser.xmlParser());
		Set<Relation> set = new HashSet<Relation>();
		//RelationExtractor extractor = new RelationExtractor();
		
		/*************Relation extraction******************/		
		Map<String, String> currentRelationExtraction = new HashMap<String, String>();
		for (Pattern pattern : this.treePatternList.getListOfRelations()) { //Get each pattern from the list of patterns for extract relations
			extractInformation(pattern,doc,currentRelationExtraction,"",""); //Extract the relations, using each pattern
		} //END We get each pattern from the list of patterns for extract relations
		
		/*******Delete duplicated relations*************/		
		deleteDuplicatedRelations(currentRelationExtraction, sentenceData.getCleanSentence());
		validatePhrasalVerbs(sentenceData, currentRelationExtraction);
		
		for(String keyRelation: currentRelationExtraction.keySet()){ //for each relation candidate, obtained
				List<Pattern> subjectPatterns = this.treePatternList.getSubjectsByRelationPatternList(keyRelation); //We get the patterns to extract subjects (entity01), according with the extracted relation
				Map<String, String> subjectCurrentExtractionAll = new HashMap<String, String>();				
				for (Pattern subjectPattern : subjectPatterns) { //for each pattern for extract subjects
					/*************Entity1 / Subject extraction******************/
					Map<String, String>  subjectCurrentExtraction = new HashMap<String, String>();					
					extractInformation(subjectPattern,doc,subjectCurrentExtraction,"",""); //extract subjects for given relations
					subjectCurrentExtractionAll.putAll(subjectCurrentExtraction);					
				} //END for each pattern for extract subjects
				
					
					Relation relation = new Relation();
					relation.setRelation(currentRelationExtraction.get(keyRelation));						
					if(Constants.BE_SPECIFIC_WITH_POSTAG_IN){
						sentenceData.setUseExtended(true);	
					}
					List<String> arguments = this.argumentExtractor.argumentExtractorAll(sentenceData, relation.getRelation(), null); //extract all arguments candidates, (entity02)
	
					for (String argument : arguments) {
						for(String keySubject: subjectCurrentExtractionAll.keySet()){
							String subject = this.validateSubject(subjectCurrentExtractionAll.get(keySubject), sentenceData);
							if(subject == null) continue;
							relation.setEntity1(subject);
							relation.setEntity2(argument);							
							if(relation.isComplete() && SemanticRelationValidator.isValid(relation)){
								if(GET_RELATION_POSTAGS) {
									String words[] = relation.inRow().split(" ");
									StringBuilder sb = new StringBuilder();
									for (int i = 0; i < words.length; i++) {										
										sb.append(sentenceData.getWordPOSTAG_extended().get(words[i]));
										sb.append(" ");
									}
									relation.setFullExtractionAsPosTags(sb.toString().trim());
								}
								if(CALCULATE_SCORE) {
									relation.setScore(scoreCalculator.calculate(sentenceData, relation));
								}
								set.add(relation);
								relation = new Relation(relation);
							}
						}//end subject
						if(set.isEmpty() && relation.getRelation()!=null && relation.getEntity2()!=null ) {
							String subject = this.argumentExtractor.extractNounPhraseAtLeft(sentenceData,  relation.getRelation(),  relation.getEntity2());
							subject = this.validateSubject(subject, sentenceData);
							if(subject != null) {
								relation.setEntity1(subject);
								set.add(relation);
							}
						}
					}
					

			} // END for each relation candidate, obtained
	
		return set;		
	}

	private void deleteDuplicatedRelations(Map<String, String> currentRelationExtraction, String sentence) {
		
		Set<String> relationsSet = new HashSet<String>();
		Set<String> relationsToDelete = new HashSet<String>(); 
		
		for (String key : currentRelationExtraction.keySet()) {
			String relation = currentRelationExtraction.get(key);
			if(sentence.contains(relation)) {
				relationsSet.add(relation);
			}
		}
		
		List<String> relations = new ArrayList<String>();
		relations.addAll(relationsSet);		
		java.util.Collections.sort(relations, new StringLengthComparator());
		
		for (int i = 0; i < relations.size(); i++) {
			String relation = relations.get(i);
			for (int j = i+1; j < relations.size(); j++) {
				String relation2 = relations.get(j);
				if(relation2.contains(relation)) {
					relationsToDelete.add(relation);
				}
			}
		}
		Iterator<String> it = currentRelationExtraction.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (relationsToDelete.contains(currentRelationExtraction.get(key))) {
				it.remove();
			}
		}
	}

	public boolean isUseReverb() {
		return useReverb;
	}
	

	public void setUseReverb(boolean useReverb) {
		this.useReverb = useReverb;
		if(this.useReverb && reverbExtractor==null) {
			reverbExtractor = new ReVerbExtractorUtility();
		}
	}

	public boolean isUseClausIE() {
		return useClausIE;
	}

	public boolean isUseOnlineTraining() {
		return useOnlineTraining;
	}

	public void setUseOnlineTraining(boolean useOnlineTraining) throws Exception {
		this.useOnlineTraining = useOnlineTraining;
		if(useOnlineTraining) {
			this.trainOnline = new TrainOnline(parser, patternContainer);
			this.trainOnline.setArgumentPatternList(this.argumentPatternList);
		}
	}

	public void setUseClausIE(boolean useClausIE) {
		this.useClausIE = useClausIE;
		if(this.useClausIE) {
			try {
				this.clausIEExtractor = new ClausIEExtractorUtility();				
			} catch (Exception e1) {
				this.useClausIE = false;
				System.err.println("Unable to initialice ClausIE extractor. "+e1.getMessage());
			}
		}
	}
	
	public void checkClausIEisUp() {
		if(this.useClausIE) {
			try {	
				if(!clausIEExtractor.checkServerIsUp()) {
					throw new Exception("Unable to detect if ClausIE server is up and running");
				}
			} catch (Exception e1) {
				this.useClausIE = false;
				System.err.println("Unable to initialice ClausIE extractor. "+e1.getMessage());
			}
		}
	}
	
	public void turnOffClausIEServer() {
		if(this.useClausIE) {
			System.out.println("TurningOff clausIE Server");
			this.clausIEExtractor.turnOffServer();
		}
	}
		
	
	public void saveExtractionPatternsToFile() throws JsonIOException, IOException {
		if(useOnlineTraining) {
			this.trainOnline.saveTofile();
		}
	}

}
