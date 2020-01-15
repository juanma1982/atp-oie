package ar.edu.unlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;

import ar.edu.unlp.entities.Example;
import ar.edu.unlp.entities.PatternArgumentList;
import ar.edu.unlp.entities.PatternContainer;
import ar.edu.unlp.entities.PatternList;
import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.entities.SentenceData;
import ar.edu.unlp.utils.ParagraphStanford;

public class TrainOnline {

	protected StanfordCoreParser parser = null;
	protected PatternContainer patternContainer; 
	protected PatternList treePatternList;
	public static final int MIN_SCORE_FOR_TRAINING = 43;
	protected PatternArgumentList argumentPatternList;
	protected OIEPatternExtraction oiePatternExtraction = null;
		 
	public ParagraphStanford paragraph=null;
	
	
	public TrainOnline(StanfordCoreParser parser,PatternContainer patternContainer) throws Exception{
		this.parser = parser;
		this.patternContainer = patternContainer;
		this.treePatternList = this.patternContainer.getTreePatterns();
		this.oiePatternExtraction = new OIEPatternExtraction();
		
	}
	
	
	public void extractNewPatternFromRelation(List<Relation> relations, SentenceData sentenceData) throws Exception{
		
		Example example = this.generateExamplesFromRelations(relations, sentenceData);
		if(example.getRelations().size() <= 0) return;
		this.generateExtractionPatternsFromExample(example);
	}
	
	public void save() throws JsonIOException, IOException {
		treePatternList.initArguments(); //clean arguments, because is not longer used
		this.patternContainer.setTreePatterns(treePatternList);
		
		this.patternContainer.setPatternsForArguments(new PatternArgumentList()); //initialice empty, because is not longer used
		PatternLoader.saveNewPatternsIntoJson(this.patternContainer);		
	}

	
	protected Example generateExamplesFromRelations(List<Relation> relations, SentenceData sentenceData){
		
		Example fakeExample = new Example();
		List<Relation> relationsWithScore = new ArrayList<Relation>();
		fakeExample.setSentence(sentenceData.getSentence());
		for (Relation relation : relations) {
			if(relation.getScore() > MIN_SCORE_FOR_TRAINING) {
				relationsWithScore.add(relation);
			}
		}
		
		fakeExample.setRelations(relationsWithScore);
		fakeExample.setSentenceData(sentenceData);
		return fakeExample;
	}

	
	protected void generateExtractionPatternsFromExample(Example example) {
		
		oiePatternExtraction.generateExtractionPatterns(example, this.treePatternList);
		//this.argumentPatternList = oiePatternExtraction.getArgumentExtractor().getPatterns();
	}


	public PatternArgumentList getArgumentPatternList() {
		return argumentPatternList;
	}


	public void setArgumentPatternList(PatternArgumentList argumentPatternList) {
		this.argumentPatternList = argumentPatternList;
	}
	
	public void saveTofile() throws JsonIOException, IOException {

		treePatternList.initArguments(); //clean arguments, because is not longer used
		this.patternContainer.setTreePatterns(treePatternList);
		//this.patternContainer.setPatternsForArguments(this.argumentPatternList);
		this.patternContainer.setPatternsForArguments(new PatternArgumentList()); //initialice empty, because is not longer used
		PatternLoader.savePatternsIntoJson(this.patternContainer);	
	
	}
	
}
