package test.utils;

import java.io.File;

import ar.edu.unlp.constants.Filenames;

public class PatternFileTestUt {

	
	
	public static void fileSwap(String sourceFile, String targetFile) throws Exception {
		 File fsource = new File(sourceFile);
		 File ftarget = new File(targetFile);
		 if(ftarget.exists() && !fsource.exists()) {
			 return;
		 }else if(ftarget.exists() && fsource.exists()) {
			 if(ftarget.delete()) {
				 System.out.println("Deleted existing file: "+targetFile);
			 }else {
				 throw new Exception("Unable to delete "+targetFile);
			 }
		 }else if(!ftarget.exists() && !fsource.exists()) {
			 throw new Exception(sourceFile+" doesn't exists and neither "+targetFile);
		 }
		
		
        boolean renameResult = fsource.renameTo(ftarget);
        if(renameResult){
            System.out.println("Rename "+sourceFile+" as "+targetFile+" Success");
        }else{
        	throw new Exception("Rename "+sourceFile+" as "+targetFile+" FaiL!!");
        }
	}
	
	public static void fileRename(String sourceFile, String targetFile) throws Exception {
		 File fsource = new File(sourceFile);
		 File ftarget = new File(targetFile);
		 boolean renameResult = fsource.renameTo(ftarget);
	        if(renameResult){
	            System.out.println("Rename "+sourceFile+" as "+targetFile+" Success");
	        }else{
	        	throw new Exception("Rename "+sourceFile+" as "+targetFile+" FaiL!!");
	        }
	}
}
