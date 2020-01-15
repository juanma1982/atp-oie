package ar.edu.unlp.extractionsources;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import ar.edu.unlp.RelationExtractor;
import ar.edu.unlp.constants.Filenames;
import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.entities.RelationForSQL;
//import ar.edu.unlp.utils.ExtractionClassificatorBatch;
import ar.edu.unlp.utils.NGraphUt;

public class MySQLData {

	public static final int BATCH_SIZE = 100;

	private static final int MAX_STRING_LENGTH = 500;
	
	protected Connection conn = null;
	private String host;
	private String username;
	private String password;
	private String database;
	private String sqlSelectReadText;
	private String sqlInsertSingleExtraction;
	private String sqlUpdateExtraction;
	
	private ResultSet rs=null;
	private String currentText;
	public int currentIdText;
	public int currentIdDatabase;

	//private ExtractionClassificatorBatch ec;
	private NGraphUt graphut = null;
	public int countExtractions = 0;
	public int countValidExtractions = 0;

	public MySQLData() throws IOException, SQLException{
		readProperties();
		connect();
		startQuerySelect();
		//this.ec = new ExtractionClassificatorBatch();
		this.graphut = new NGraphUt();
	}
	
	protected void connect() throws SQLException{
		try {
		    conn =DriverManager.getConnection("jdbc:mysql://"+this.host+"/"+this.database+"?user="+this.username+"&password="+this.password);
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		    throw ex;
		}
	}
	
	protected void readProperties() throws IOException{
		
		try {
		    //load a properties file from class path, inside static method
			File file = new File(Filenames.MYSQL_PROPERTIES);
			FileInputStream fileInput = new FileInputStream(file);
			Properties prop = new Properties();
			prop.load(fileInput);
			fileInput.close();
			this.host = prop.getProperty("mysql.host"); 
			this.username =prop.getProperty("mysql.username"); 
			this.password =prop.getProperty("mysql.password"); 
			this.database =prop.getProperty("mysql.database"); 
			this.sqlSelectReadText =prop.getProperty("mysql.sqlSelectReadText"); 
			this.sqlInsertSingleExtraction =prop.getProperty("mysql.sqlInsertSingleExtraction");
			this.sqlUpdateExtraction =prop.getProperty("mysql.sqlUpdateExtraction");
			
		}catch (IOException ex) {
			System.err.println("Unable to load property file");
		    ex.printStackTrace();
		    throw ex;
		}
	}
	
	protected void startQuerySelect() throws SQLException{

	      Statement st = conn.createStatement();
	      this.rs = st.executeQuery(this.sqlSelectReadText);
	}
	
	public String readNextLine() throws SQLException{
		
		if(this.rs.next()){
			this.currentIdDatabase	= rs.getInt("id_database");
			this.currentIdText 		= rs.getInt("id_text");
			this.currentText 		= rs.getString("Text");
			return this.currentText ;
		}
		this.rs.close();
		return null;
	}
	
	public String cutLongStrings(String string) {
		if(string.length() <= MAX_STRING_LENGTH) return string;
		return string.substring(0, MAX_STRING_LENGTH);
	} 
	
	public void insertRelationExtracted(RelationForSQL relation) throws SQLException{
		
		  PreparedStatement preparedStmt = conn.prepareStatement(this.sqlInsertSingleExtraction);
	      preparedStmt.setString(1, cutLongStrings( relation.getEntity1()));
	      preparedStmt.setString(2, cutLongStrings(relation.getRelation()));
	      preparedStmt.setString(3, cutLongStrings(relation.getEntity2()));
	      preparedStmt.setInt(4, relation.getIdText());
	      preparedStmt.setInt(5, relation.getIdDatabase());
	      preparedStmt.setBoolean(6, relation.isFromReverb());
	      preparedStmt.setBoolean(7, relation.isFromClausIE());
	      preparedStmt.setInt(8, relation.getScore());
	    //  preparedStmt.setString(8, relation.getSourceSentence());

	      // execute the preparedstatement
	      preparedStmt.execute();
		
	}
	
	/*protected void processBatch(boolean insert,List<RelationForSQL> relationsBatch) throws SQLException {
		double[] results = this.evaluateExtractions(relationsBatch);
		for(int i=0;i<relationsBatch.size();i++) {
			this.countExtractions++;
			if(results!=null && ExtractionClassificatorBatch.INVALID == (int)results[i]) continue;
			this.countValidExtractions++;
			RelationForSQL relation = relationsBatch.get(i);			
			if(insert){
				this.insertRelationExtracted(relation);
			}
			System.out.println(relation.toString());
		}
		
	}*/
	
	public static void extractInformationFromTableAndInsert(boolean insert) throws Exception{
		extractInformationFromTableAndInsert(insert,false,false,false);
	}
	
	public static void extractInformationFromTableAndInsert(boolean insert, boolean useReverb, boolean useClausIE, boolean useOnlineTraining) throws Exception{
		
		MySQLData mysql = new MySQLData();
		String text = mysql.readNextLine();
		RelationExtractor extractor = new RelationExtractor();						  
		extractor.setUseReverb(useReverb);
		extractor.setUseClausIE(useClausIE);
		extractor.setUseOnlineTraining(useOnlineTraining);
		int count =0;
		
		while (text!=null ) {
			count++;
			System.out.println(text);
			List<Relation> relations = extractor.extractInformationFromParagraph(text);
			for (Relation relation : relations) {
				if(relation.getScore()<-50) continue;
				RelationForSQL local = new RelationForSQL(relation);
				local.setIdDatabase(mysql.currentIdDatabase);
				local.setIdText(mysql.currentIdText);
				if(insert){
					mysql.insertRelationExtracted(local);
				}else {
					System.out.println(relation.toString());
				}
				
			}			
			text = mysql.readNextLine();
		}
		extractor.turnOffClausIEServer();
		mysql.closeConnection();
		System.out.println();
		System.out.println("total sentences: "+count);
		System.out.println("total extractions: "+mysql.countExtractions);
		System.out.println("total valid extractions: "+mysql.countValidExtractions);
	}
	
	public static void extractInformationFromTableAndInsertUsingOnlyReverb(boolean insert) throws Exception{
		
		MySQLData mysql = new MySQLData();
		String text = mysql.readNextLine();
		RelationExtractor extractor = new RelationExtractor();					  
		
		int count =0;
		
		while (text!=null ) {
			count++;
			System.out.println(text);
			List<Relation> relations = extractor.extractInformationFromParagraphOnlyReverb(text);
			for (Relation relation : relations) {
				if(relation.getScore()<-50) continue;
				RelationForSQL local = new RelationForSQL(relation);
				local.setIdDatabase(mysql.currentIdDatabase);
				local.setIdText(mysql.currentIdText);
				if(insert){
					mysql.insertRelationExtracted(local);
				}else {
					System.out.println(relation.toString());
				}
			}			
			text = mysql.readNextLine();
		}
		mysql.closeConnection();
		System.out.println();
		System.out.println("total sentences: "+count);
		System.out.println("total extractions: "+mysql.countExtractions);
		System.out.println("total valid extractions: "+mysql.countValidExtractions);
	}
	
	/*private double[] evaluateExtractions(List<RelationForSQL> relationsBatch) {

		for (RelationForSQL relation : relationsBatch) {			
			String bigrams = graphut.getBigramsAsString(relation.getFullExtractionAsPosTags());			
			try {
				ec.addCase(bigrams);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}			
		}
		try {
			return ec.evalCurrentCases();
		} catch (Exception e) {			
			e.printStackTrace();
			return null;
		}
	}*/

	public void closeConnection(){
		try {
			this.conn.close();
		} catch (SQLException e) {				
			System.err.println("The connection failed to closed");
			e.printStackTrace();
		}
	}

	public static void extractInformationFromTableAndUpdate(boolean useReverb) throws Exception {
		MySQLData mysql = new MySQLData();
		String text = mysql.readNextLine();
		RelationExtractor extractor = new RelationExtractor();						  
		extractor.setUseReverb(useReverb);
		int count =0;
		
		while (text!=null ) {
			count++;
			System.out.println(text);
			List<Relation> relations = extractor.extractInformationFromParagraph(text);
			for (Relation relation : relations) {
				if(relation.getScore()<-50) continue;
				RelationForSQL local = new RelationForSQL(relation);
				local.setIdDatabase(mysql.currentIdDatabase);
				local.setIdText(mysql.currentIdText);
				mysql.update(local);
			}			
			text = mysql.readNextLine();
		}
		extractor.turnOffClausIEServer();
		mysql.closeConnection();
		System.out.println();
		System.out.println("total sentences: "+count);
		System.out.println("total extractions: "+mysql.countExtractions);
		System.out.println("total valid extractions: "+mysql.countValidExtractions);
		
	}

	private void update(RelationForSQL relation) throws SQLException {
		 PreparedStatement preparedStmt = conn.prepareStatement(this.sqlUpdateExtraction);
		  preparedStmt.setBoolean(1, relation.isFromReverb());
	      preparedStmt.setInt(2, relation.getScore());
	      
	      preparedStmt.setString(3, relation.getEntity1());
	      preparedStmt.setString(4, relation.getRelation());
	      preparedStmt.setString(5, relation.getEntity2());
	      preparedStmt.setInt(6, relation.getIdText());
	      
	      
	      
	      // execute the preparedstatement
	      preparedStmt.execute();
		
	}


}
