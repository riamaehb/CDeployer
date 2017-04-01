package jp.ac.keio.ics.db.bCrowd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TranslationHITCreator extends HITCreator {
	
	public TranslationHITCreator(String configFile, String[] textFiles){
		super(configFile,textFiles);
	}
	
	public void createHIT() throws IOException{
	    BufferedReader reader = new BufferedReader(new FileReader(loc+"TT-create.html"));
	    String line;
	    //read the HTML skeleton
	    StringBuilder html = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	html.append(line);
	    }
	    reader.close();
	    //read the text to translate
	    reader = new BufferedReader(new FileReader(this.getTextFiles()[0]));
	    StringBuilder text = new StringBuilder();
	    while((line = reader.readLine()) != null){
	    	text.append(line);
	    }
	    reader.close();
	    //insert the text in the HTML
	    String questionHTML = html.toString().replace("***Insert here***", text.toString());
	    this.setQuestion(this.htmlToQuestion(questionHTML));
		super.createHIT();
	}

}
