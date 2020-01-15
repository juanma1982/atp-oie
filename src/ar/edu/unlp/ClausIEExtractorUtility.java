package ar.edu.unlp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unlp.entities.Relation;
import ar.edu.unlp.utils.ObservableStream;

public class ClausIEExtractorUtility {
	
	public static final int TIMEOUT = 20000; //20 seconds
	public final String HOST = "127.0.0.1";
	public final String PORT = "9999";
	protected boolean showExceptions = true;
	
	// The Observable object allowing to send the input lines to my external process
	protected ObservableStream output = new ObservableStream();
	protected Process p;
	
	public ClausIEExtractorUtility() throws Exception {
		final Process p = Runtime.getRuntime().exec("external/startClausIEServer.sh");
		
		System.out.println("Initializing ClausIE as Server...");
		boolean isUp = false;
		int count = 0;
		showExceptions = false;
		do {
			Thread.sleep(200);
			try {
				isUp = checkServerIsUp();
			}catch (Exception e) {

			}
			count++;
			if(count > 100) {
				throw new Exception("Unable to initialice ClausIE Server");
			}
		}while(!isUp);
		showExceptions = true;
		System.out.println("ClausIE started");
	}
	
	public boolean checkServerIsUp() {
		
		String returnStr = this.requestToServer("GET", null);
		if(returnStr.equals("alive")) return true;
		return false;
	}
	
	public void turnOffServer() {
		
		String returnStr = this.requestToServer("DELETE", null);
		returnStr = this.requestToServer("DELETE", null);		
		
	}
	
	public List<Relation> processSentence(String line){
		 List<Relation> relations= new ArrayList<Relation>();
		 String returnStr = this.requestToServer("POST", line+"\nend\n");
		 if(!returnStr.isEmpty()) {
			 String[] extractions = returnStr.split("1\t");
			 for (String string : extractions) {
				String[] extractionselements = string.split("\t");
				if(extractionselements.length >=3) {
					Relation relation = new Relation();
					relation.setEntity1(extractionselements[0].replaceAll("\"", ""));
					relation.setRelation(extractionselements[1].replaceAll("\"", ""));
					relation.setEntity2(extractionselements[2].replaceAll("\"", ""));
					relation.setFromClausIE(true);
					relations.add(relation);
				}
			}
		 }
		 //System.out.println(returnStr);
		 output.addLine(line);
		 return relations;
	}
	
	protected String requestToServer(String method, String body) {
		URL url;
		String returnStr = "";
		BufferedReader in = null;
		HttpURLConnection con =null;
		
		try {
			url = new URL("http://"+HOST+":"+PORT);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("Content-Type", "text/plain");
			//String contentType = con.getHeaderField("Content-Type");
			con.setConnectTimeout(TIMEOUT);
			con.setReadTimeout(TIMEOUT);
			if(body != null && !body.isEmpty()) {
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(body);
				out.flush();
				out.close();
			}
			
			/*con.disconnect();
			con = (HttpURLConnection) url.openConnection();*/
			int status = con.getResponseCode();
			in = new BufferedReader(
			new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}			
			
			returnStr = content.toString();
			
			
		} catch (MalformedURLException e) {
			if(showExceptions) e.printStackTrace();
		} catch (IOException e) {
			if(showExceptions)  e.printStackTrace();
		}finally {		
			try {
				in.close();
			} catch (IOException e) {
				System.out.println("Unable to close socket");
				e.printStackTrace();
			}
			con.disconnect();
		}
		return returnStr;
	}
	
	

}
