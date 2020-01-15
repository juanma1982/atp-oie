package ar.edu.unlp.entities;

import ar.edu.unlp.constants.Words;

public class Relation {

	protected static int nextID = 0;
	protected String entity1;
	protected String entity2;
	protected String relation;
	protected String fullExtractionAsPosTags;
	protected String sourceSentence = null;
	protected int score;
	protected int id;
	protected int dependsOf=-1;
	protected boolean fromReverb=false;
	protected boolean fromClausIE=false;
	
	public Relation(){
		this.id=nextID;
		nextID++;
	}
	
	public Relation(Relation toCopy){
		this.id=nextID;
		nextID++;
		this.entity1 = toCopy.entity1;
		this.entity2 = toCopy.entity2;
		this.relation = toCopy.relation;
		this.score = toCopy.score;
		this.fromReverb = toCopy.fromReverb;
		this.fromClausIE = toCopy.fromClausIE;
		this.fullExtractionAsPosTags = toCopy.fullExtractionAsPosTags;
		this.sourceSentence = toCopy.sourceSentence;
	}
		
	public String getEntity1() {
		return entity1;
	}
	public void setEntity1(String entity1) {
		this.entity1 = entity1.replace(" '", "'");
	}
	public String getEntity2() {
		return entity2;
	}
	public void setEntity2(String entity2) {
		this.entity2 = entity2.replace(" '", "'");
		if(this.entity2.startsWith("and")) {
			this.entity2 = this.entity2.substring(4);
		}
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation.replace(" '", "'");
		if(this.relation.startsWith("'")) {
			String[] words = this.relation.split(" ");
			if(words.length>0) {
				String replace = Words.APOSTROPHEMAP.get(words[0]);
				if(replace!=null) {
					this.relation = this.relation.replace(words[0], replace);
				}
			}
		}
	}
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDependsOf() {
		return dependsOf;
	}

	public void setDependsOf(int dependsOf) {
		this.dependsOf = dependsOf;
	}

	public boolean isFromReverb() {
		return fromReverb;
	}

	public void setFromReverb(boolean fromReverb) {
		this.fromReverb = fromReverb;
	}

	public boolean isFromClausIE() {
		return fromClausIE;
	}

	public void setFromClausIE(boolean fromClausIE) {
		this.fromClausIE = fromClausIE;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(entity1);
		sb.append(", ");
		sb.append(relation);
		sb.append(", ");
		sb.append(entity2);
		sb.append(")");
		return sb.toString();
	}
	
	public String toStringScore(){
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(entity1);
		sb.append(", ");
		sb.append(relation);
		sb.append(", ");
		sb.append(entity2);
		sb.append(")");
		sb.append(" => (");
		sb.append(this.score+") ");
		return sb.toString();
	}
	
	public String toStringFull() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.id);
		sb.append(" (");
		sb.append(entity1);
		sb.append(", ");
		sb.append(relation);
		sb.append(", ");
		sb.append(entity2);
		sb.append(")");
		sb.append(" => (");
		sb.append(this.score+") ");
		if(this.fromReverb) {
			sb.append(" (from ReVerb) ");
		}
		if(this.fromClausIE) {
			sb.append(" (from ClausIE) ");
		}
		if(this.dependsOf >-1) {
			sb.append(" DEPENDS OF ");
			sb.append(this.dependsOf);
		}
		return sb.toString();
	}

	
	public String inRow(){
		StringBuilder sb = new StringBuilder();		
		sb.append(entity1);
		sb.append(" ");
		sb.append(relation);
		sb.append(" ");
		sb.append(entity2);		
		return sb.toString();
	}
	
	public boolean isComplete(){
		return entity1!=null && !entity1.isEmpty() && entity2!=null && 
				!entity2.isEmpty() && relation!=null && !relation.isEmpty(); 
	}
	public String getFullExtractionAsPosTags() {
		return fullExtractionAsPosTags;
	}

	public void setFullExtractionAsPosTags(String fullExtractionAsPosTags) {
		this.fullExtractionAsPosTags = fullExtractionAsPosTags;
	}
	
	public void setSourceSentence(String sentence) {
		this.sourceSentence = sentence;
		
	}
	public String getSourceSentence() {
		return this.sourceSentence;
		
	}

	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Relation other = (Relation) obj;
      return this.toString().equals(other.toString());
   }





   
}
