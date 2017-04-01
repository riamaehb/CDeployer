package jp.ac.keio.ics.db.bCrowd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NarrativeImprovHITCreator extends HITCreator {
	
	public NarrativeImprovHITCreator(String configFile, String[] textFiles){
		super(configFile,textFiles);
	}
	
	public void createHIT() throws IOException{
	    BufferedReader reader = new BufferedReader(new FileReader(loc+"NW-improve.html"));
	    String line;
	    //read the HTML skeleton
	    StringBuilder html = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	html.append(line);
	    }
	    reader.close();
	    //read the information to narrate
	    reader = new BufferedReader(new FileReader(this.getTextFiles()[0]));
	    StringBuilder text = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	text.append(line);
	    }
	    reader.close();
	    //insert the text in the HTML
	    String questionHTML = html.toString().replace("***Insert here 1***", text.toString());
	    
	    //read the narration to improve
	    reader = new BufferedReader(new FileReader(this.getTextFiles()[1]));
	    StringBuilder narration = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	narration.append(line+"\n");
	    }
	    reader.close();
	    //insert the summary in the HTML
	    questionHTML = questionHTML.replace("***Insert here 2***", narration.toString());
	    
	    
	    this.setQuestion(this.htmlToQuestion(questionHTML));
		super.createHIT();
	}

}
