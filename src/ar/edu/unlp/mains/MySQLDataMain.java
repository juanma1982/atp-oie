package ar.edu.unlp.mains;

import ar.edu.unlp.extractionsources.MySQLData;

public class MySQLDataMain {

	private static final boolean USE_REVERB = true;
	private static final boolean USE_CLAUSIE = true;
	public static boolean INSERT_IN_DB = true;
	public static boolean USE_ONLINE_TRAIN = true;
	public static void main(String[] args) {
		try{
			MySQLData.extractInformationFromTableAndInsert(INSERT_IN_DB, USE_REVERB,USE_CLAUSIE,USE_ONLINE_TRAIN);
			//MySQLData.extractInformationFromTableAndInsertUsingOnlyReverb(INSERT_IN_DB);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		System.out.println("Done");
	}

}
