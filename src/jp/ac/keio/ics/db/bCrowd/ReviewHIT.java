package jp.ac.keio.ics.db.bCrowd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

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

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.AssociateQualificationWithWorkerRequest;
import com.amazonaws.services.mturk.model.CreateAdditionalAssignmentsForHITRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;
import com.amazonaws.services.mturk.model.RejectAssignmentRequest;

import jp.ac.keio.ics.db.utils.AMTFunctions;
import jp.ac.keio.ics.db.utils.Properties;
import jp.ac.keio.ics.db.utils.StringSimilarity;

public class ReviewHIT {
	
	private static ArrayList<String> benchmarkTexts = new ArrayList<String>();
	private static ArrayList<String> qualificationIDs = new ArrayList<String>();



	public static void main(String[] args) throws Exception {
		
		Options options = new Options();

        Option hitID = new Option("h", "hitID", true, "ID of the HIT to review");
        hitID.setRequired(true);
        options.addOption(hitID);
        
        Option benchmark = new Option("b", "benchmark", true, "Space separated list of files where the benchmark texts are stored");
        benchmark.setRequired(true);
        benchmark.setArgs(Option.UNLIMITED_VALUES);
        benchmark.setOptionalArg(true);
        options.addOption(benchmark);
        
        Option qualifs = new Option("q", "qualifs", true, "Space separated list of qualification IDs to assign to workers");
        qualifs.setRequired(false);
        qualifs.setArgs(Option.UNLIMITED_VALUES);
        qualifs.setOptionalArg(true);
        options.addOption(qualifs);
        
        Option action = new Option("a", "action", true, "Should be Review");
        action.setRequired(false);
        options.addOption(action);
        
        Option threshold = new Option("t", "threshold", true, "Threshold used to compare contributions to the benchmark");
        threshold.setRequired(true);
        options.addOption(threshold);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;        
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ReviewHIT", options);
            System.exit(1);
            return;
        }
        
        processBenchmarks(cmd.getOptionValues("benchmark"));
        
        if(cmd.hasOption("qualifs")){
        	processQualifs(cmd.getOptionValues("qualifs"));
        }
        
        ArrayList<String> assignmentStatuses = new ArrayList<String>();
		assignmentStatuses.add("Submitted");
        ListAssignmentsForHITResult assignments = AMTFunctions.getAssigmentsForHIT(cmd.getOptionValue("hitID"), assignmentStatuses);
        
		for(Assignment a : assignments.getAssignments()){
			reviewAssignment(a, Float.parseFloat(cmd.getOptionValue("t")));
		}
	}
	
	private static void reviewAssignment(Assignment a, float threshold) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(a.getAnswer())));
        NodeList nList = doc.getElementsByTagName("Answer");
        for (int i = 0; i < nList.getLength(); i++) {
        	Node n = nList.item(i);
        	for(int j = 0; j < n.getChildNodes().getLength(); j++){
	        	if(n.getChildNodes().item(j).getTextContent() != null && n.getChildNodes().item(j).getTextContent().equals("Q6MultiLineTextInput")){
	        		Node n1 = n.getChildNodes().item(j);
	        		while(!n1.getNodeName().equals("FreeText")){
	        			n1 = n1.getNextSibling();
	        		}
	        		String answer = n1.getTextContent();
	        		for(String benchmarkText : benchmarkTexts){
	        			if(StringSimilarity.similarity(benchmarkText,answer) > threshold){
	        		        System.out.println("Rejected assignment");
	        				rejectAssignment(a);
	        				assignQualifications(a.getWorkerId());
	        				return;
	        			}
	        		}
	        	}
        	}
        }
        System.out.println("Assignment seems ok");
        assignQualifications(a.getWorkerId());
	}
	
	private static void processBenchmarks(String[] benchmarkFiles) throws IOException{
		for(String benchmarkFile : benchmarkFiles){
			BufferedReader reader = new BufferedReader(new FileReader(benchmarkFile));
		    String line;
		    //read the HTML skeleton
		    StringBuilder benchmarkText = new StringBuilder();
		    while((line = reader.readLine()) != null){
		    	benchmarkText.append(line);
		    }
		    reader.close();
		    benchmarkTexts.add(benchmarkText.toString());
		}
	}
	
	private static void rejectAssignment(Assignment a){
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(Properties.endpoint,"us-east-1"));
		AmazonMTurk client = builder.build();
		
		RejectAssignmentRequest rejectRequest = new RejectAssignmentRequest();
		rejectRequest.setAssignmentId(a.getAssignmentId());
		rejectRequest.setRequesterFeedback("Your contribution was rejected because it matched an automaticaly generated contribution (e.g. google translate).");
		
		client.rejectAssignment(rejectRequest);
		
		CreateAdditionalAssignmentsForHITRequest additionalAssignmentRequest = new CreateAdditionalAssignmentsForHITRequest();
		additionalAssignmentRequest.setHITId(a.getHITId());
		additionalAssignmentRequest.setNumberOfAdditionalAssignments(1);
		client.createAdditionalAssignmentsForHIT(additionalAssignmentRequest);
		
	}
	
	private static void processQualifs(String[] qualifs) throws IOException{
//		From when the qualifs were in a file.
//		ResourceBundle resource;
//		try (FileInputStream fis = new FileInputStream(qualifs)) {
//			resource = new PropertyResourceBundle(fis);
//			fis.close();
//			int i = 1;
//			while(resource.containsKey("qualification."+i)){
//				qualificationIDs.add(resource.getString("qualification."+i));
//				i++;
//			}
//		}
		qualificationIDs.addAll(Arrays.asList(qualifs));
	}
	
	private static void assignQualifications(String workerID){
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(Properties.endpoint,"us-east-1"));
		AmazonMTurk client = builder.build();
		
		for(String qualifID : qualificationIDs){
			System.out.println("Set qualification: " + qualifID);
			AssociateQualificationWithWorkerRequest qualifRequest = new AssociateQualificationWithWorkerRequest();
			qualifRequest.setWorkerId(workerID);
			qualifRequest.setQualificationTypeId(qualifID);
			qualifRequest.setIntegerValue(100);
			client.associateQualificationWithWorker(qualifRequest);
		}
		
	}
}
