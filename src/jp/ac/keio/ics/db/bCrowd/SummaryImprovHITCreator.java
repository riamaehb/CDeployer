package jp.ac.keio.ics.db.bCrowd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SummaryImprovHITCreator extends HITCreator {

	public SummaryImprovHITCreator(String configFile, String[] textFiles){
		super(configFile,textFiles);
	}
	
	public void createHIT() throws IOException{
	    BufferedReader reader = new BufferedReader(new FileReader(loc+"SS-improve.html"));
	    String line;
	    //read the HTML skeleton
	    StringBuilder html = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	html.append(line);
	    }
	    reader.close();
	    //read the text to summarize
	    reader = new BufferedReader(new FileReader(this.getTextFiles()[0]));
	    StringBuilder text = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	text.append(line);
	    }
	    reader.close();
	    //insert the text in the HTML
	    String questionHTML = html.toString().replace("***Insert here 1***", text.toString());
	    
	    //read the summary to improve
	    reader = new BufferedReader(new FileReader(this.getTextFiles()[1]));
	    StringBuilder summary = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	summary.append(line+"\n");
	    }
	    reader.close();
	    //insert the summary in the HTML
	    questionHTML = questionHTML.replace("***Insert here 2***", summary.toString());
	    
	    
	    this.setQuestion(this.htmlToQuestion(questionHTML));
		super.createHIT();
	}
}
