package ar.edu.unlp.mains;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ar.edu.unlp.RelationExtractor;
import ar.edu.unlp.entities.Relation;

public class CommandLine {

	private static final String VERSION = "1.0.1 - 2020-01-15";	
	public static boolean flagUseReverb = false;
	public static boolean flagUseClausIE = false;
	public static boolean flagOutputFile = false;
	public static boolean flagShowScore = false;
	public static boolean flagShowHelp = false;
	public static boolean flagShowFull = false;
	public static boolean flagTrainOnline = false;
	public static String inputFile = "";
	public static String outputFile = "";
	public static BufferedWriter bw = null;
	public static FileWriter fw = null;
	
	protected String filename = "";	
	protected BufferedReader br=null;
	protected int fileIndex = 0;

	
	public void startReading(String filename) throws FileNotFoundException {
		
		this.br = new BufferedReader(new FileReader(filename));
		
	}
	
	public void endRead(){
		try {			
			this.br.close();
		} catch (IOException e) {
			System.out.println("fail to close BufferRead");
		}
	}
	
	public String readNextLine() throws IOException{
		
	    String line;
	    while ((line = br.readLine()) != null) {
	    	return line;
	    }
	    return null;
	}
	
	public static void options(String[] args) throws Exception {
		
		for(int i=0;i<args.length;i++) {
			
			String param = args[i];
			
			switch (param) {
				case "-f":
					if(args.length<=(i+1)) {
						throw new Exception("Param -f expects a filepath name ");
					}
					inputFile = args[i+1];
					break;
				case "-o":
					flagOutputFile = true;
					if(args.length<=(i+1)) {
						throw new Exception("Param -o expects a filepath name ");
					}
					outputFile = args[i+1];
					
					fw = new FileWriter(outputFile);
		            bw = new BufferedWriter(fw);
					
					break;
				case "-reverb":
					flagUseReverb = true;
					break;
				case "-clausie":
					flagUseClausIE = true;
					break;
				case "-trainOnline":
					flagTrainOnline = true;
					break;
				case "-score":
					flagShowScore = true;
					break;
				case "-full":
					flagShowFull = true;
					break;
				case "-help":
					flagShowHelp = true;
					break;
				default:
					break;
			}
		}
	}
	
	public static void output(String line, List<Relation> relations) throws IOException {
		if(!flagOutputFile) {
			System.out.println(line);					
			for (Relation relation : relations) {
				if(flagShowScore) {
					System.out.println(relation.toStringScore());
				}else if(flagShowFull) {
					System.out.println(relation.toStringFull());
				}else {
					System.out.println(relation.toString());
				}
			}
			System.out.println();
		}else {
			
			bw.write(line);					
			for (Relation relation : relations) {
				if(flagShowScore) {
					 bw.write(relation.toStringScore());
				}else if(flagShowFull) {
					 bw.write(relation.toStringFull());
				}else {
					 bw.write(relation.toString());
				}
			}
			bw.write("\n");
		}
	}
	
	public static void main(String[] args) {
		
		try {
			CommandLine.options(args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		
		if(flagShowHelp) {
			printHelp();
			return;
		}
		
		CommandLine cm = new CommandLine();
		
		RelationExtractor extractor;
		try {
			extractor = new RelationExtractor(flagUseReverb,flagUseClausIE,flagTrainOnline);			
		} catch (Exception e1) {			
			e1.printStackTrace();
			return;
		}
		
		try {
			cm.startReading(CommandLine.inputFile);
			String line = cm.readNextLine();
			 while(line!=null ){
				 List<Relation> relations = extractor.extractInformationFromParagraph(line);
				 if(relations!=null && !relations.isEmpty()){
					 output(line, relations);
				 }else{
					 System.err.println("No relation extracted");
				 }
				 line = cm.readNextLine();
			 }		
			 cm.endRead();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			extractor.turnOffClausIEServer();
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                System.err.format("IOException: %s%n", ex);
            }
        }

	}

	private static void printHelp() {
		System.out.println("ATP-OIE : Autonomous Tree pattern - Open Information Extractor"); 
		System.out.println("Version: "+VERSION);
		System.out.println("Usage examples:");
		System.out.println("	java -jar -Xmx4056m  -Xms1024m -ea atp-oie.jar -f /path/inputfile.txt");
		System.out.println("	java -jar -Xmx4056m  -Xms1024m -ea atp-oie.jar -f /path/inputfile.txt -o /path/output.file");
		System.out.println("	java -jar -Xmx4056m  -Xms1024m -ea atp-oie.jar <options> -f /path/inputfile.txt -o /path/output.file");
		System.out.println("");
		System.out.println("available options: ");
		System.out.println(" -f : mandatory param, indicates the input text file");
		System.out.println(" -o : indicates the output file. If not present, the result will be printed in console");
		System.out.println(" -reverb :  Use reverb for sentences without extractions ");
		System.out.println(" -clausie : Use ClausIE for sentences without extractions (after using Reverb if it is also set) ");
		System.out.println(" -trainOnline : if Reverb and/or ClausIE are set, this flag allow the creation of new extraction patterns using the relations extracted of this methods. ");
		System.out.println(" -score : also prints the score of the exraction");
		System.out.println(" -full : prints score, id, and if the relation is non factual its dependency");
		System.out.println(" -help : prints this menu");
	}

}
