package jp.ac.keio.ics.db.bCrowd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;

import jp.ac.keio.ics.db.utils.AMTFunctions;

public class SummaryEvalHITCreator extends HITCreator {
	
private String hitID;
	
	public SummaryEvalHITCreator(String configFile, String[] textFiles, String hitID){
		super(configFile,textFiles);
		this.hitID = hitID;
	}
	
	public void createHIT() throws IOException{
	    BufferedReader reader = new BufferedReader(new FileReader(loc+"SS-evaluate.html"));
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
	    String questionHTML = html.toString().replace("***Insert here 1***", text.toString());
	    
	    ArrayList<String> assignmentStatuses = new ArrayList<String>();
		assignmentStatuses.add("Approved");
        ListAssignmentsForHITResult assignments = AMTFunctions.getAssigmentsForHIT(hitID, assignmentStatuses);
        
        try{
        	if(assignments.getNumResults() > 3){
        		throw new Exception("This HIT has more than 3 accepted assignments");
        	} else if (assignments.getNumResults() < 3){
        		throw new Exception("This HIT has less than 3 accepted assignments");
        	}
        } catch(Exception e){
        	e.printStackTrace();
        	return;
        }
        
        int k = 2;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String answer ="";
        
        for(Assignment a : assignments.getAssignments()){
    		Document doc = null;
			try {
				doc = builder.parse(new InputSource(new StringReader(a.getAnswer())));
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	NodeList nList = doc.getElementsByTagName("Answer");
            for (int i = 0; i < nList.getLength(); i++) {
            	Node n = nList.item(i);
            	for(int j = 0; j < n.getChildNodes().getLength(); j++){
    	        	if(n.getChildNodes().item(j).getTextContent() != null && n.getChildNodes().item(j).getTextContent().equals("Q6MultiLineTextInput")){
    	        		Node n1 = n.getChildNodes().item(j);
    	        		while(!n1.getNodeName().equals("FreeText")){
    	        			n1 = n1.getNextSibling();
    	        		}
    	        		answer = n1.getTextContent().replaceAll("\n", "<br />");
    	        	}
            	}
            }
        	
        	questionHTML = questionHTML.replaceAll("\\*\\*\\*Insert here "+k+"\\*\\*\\*", answer);
        	k++;
        }
        
	    this.setQuestion(this.htmlToQuestion(questionHTML));
		super.createHIT();
	}

}
