package jp.ac.keio.ics.db.bCrowd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;

import jp.ac.keio.ics.db.utils.AMTFunctions;
import jp.ac.keio.ics.db.utils.FleissKappa;

public class ProcessHIT {
	
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();

        Option config = new Option("h", "hitID", true, "ID of the HIT to process");
        config.setRequired(true);
        options.addOption(config);
        
        Option action = new Option("a", "action", true, "Should be Create");
        action.setRequired(false);
        options.addOption(action);
        
        Option output = new Option("o", "output", true, "Output file.");
        output.setRequired(true);
        options.addOption(output);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;        
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("CreateHIT", options);
            System.exit(1);
            return;
        }
        
        ArrayList<String> statuses = new ArrayList<String>();
        statuses.add("Approved");
        ListAssignmentsForHITResult assignments = AMTFunctions.getAssigmentsForHIT(cmd.getOptionValue("h"), statuses);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//score of each text
		int[] scores = new int[3];
		//Matrix to compute the rater inter agreement
		short[][] kappaMatrix = new short[3][9];
		for(int i = 0; i < 3; i++){
			for(int j = 0; j< 9; j++){
				kappaMatrix[i][j] = 0;
			}
		}
		
		//compute the scores and fill the kappa matrix
        for(Assignment a : assignments.getAssignments()){
        	int scoreT1A =0, scoreT2A = 0, scoreT3A = 0;
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
            	Node n1 = null;
            	for(int j = 0; j < n.getChildNodes().getLength(); j++){
    	        	if(n.getChildNodes().item(j).getTextContent() != null){
    	        		switch(n.getChildNodes().item(j).getTextContent()){
    	        		case "adequacy-1":
    	        			n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("adequacy-1: " + n1.getTextContent());
        	        		scoreT1A += Integer.decode(n1.getTextContent());
    	        			break;
    	        		case "fluency-1":
    	        			n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("fluency-1: " + n1.getTextContent());
        	        		scoreT1A += Integer.decode(n1.getTextContent());
    	        			break;
    	        		case "adequacy-2":
    	        			n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("adequacy-2: " + n1.getTextContent());
        	        		scoreT2A += Integer.decode(n1.getTextContent());
    	        			break;
    	        		case "fluency-2":
    	        			n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("fluency-2: " + n1.getTextContent());
        	        		scoreT2A += Integer.decode(n1.getTextContent());
    	        			break;
	        			case "adequacy-3":
	        				n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("adequacy-3: " + n1.getTextContent());
        	        		scoreT3A += Integer.decode(n1.getTextContent());
    	        			break;
	        			case "fluency-3":
	        				n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		System.out.println("fluency-3: " + n1.getTextContent());
        	        		scoreT3A += Integer.decode(n1.getTextContent());
    	        			break;
	        			default:
	        				break;
    	        		}
    	        	}
            	}
            }
            scores[0] += scoreT1A;
            scores[1] += scoreT2A;
            scores[2] += scoreT3A;
            
            kappaMatrix[0][scoreT1A-2]++;
            kappaMatrix[1][scoreT2A-2]++;
            kappaMatrix[2][scoreT3A-2]++;
        }
        
        String[] texts = new String[3];
        System.out.println("list size: " + assignments.getAssignments().size());
        Assignment a = assignments.getAssignments().get(0);
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
        	Node n1 = null;
        	for(int j = 0; j < n.getChildNodes().getLength(); j++){
	        	if(n.getChildNodes().item(j).getTextContent() != null){
	        		switch(n.getChildNodes().item(j).getTextContent()){
	        			case "text1":
	        				n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		texts[0] = n1.getTextContent();
	        				break;
	        			case "text2":
	        				n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		texts[1] = n1.getTextContent();
	        				break;
	        			case "text3":
	        				n1 = n.getChildNodes().item(j);
        	        		while(!n1.getNodeName().equals("FreeText")){
        	        			n1 = n1.getNextSibling();
        	        		}
        	        		texts[2] = n1.getTextContent();
	        				break;
	        			default:
	        				break;
	        		}
	        	}
        	}
        }
        
        PrintWriter writer;
        if(cmd.hasOption("o")){
        	writer = new PrintWriter(new FileOutputStream(new File(cmd.getOptionValue("o")), true));
        } else {
        	writer = new PrintWriter(new FileOutputStream(new File("./report.txt"), true));
        }
        
        for(int i = 0; i < 3; i++){
        	writer.println(i+1+",\""+texts[i]+"\","+scores[i]);
        }
        
        writer.print("kappa," + FleissKappa.computeKappa(kappaMatrix));
        writer.close();
        
	}

}
