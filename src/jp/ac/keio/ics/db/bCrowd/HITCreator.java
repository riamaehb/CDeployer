package jp.ac.keio.ics.db.bCrowd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.CreateHITRequest;
import com.amazonaws.services.mturk.model.CreateHITResult;
import com.amazonaws.services.mturk.model.NotificationSpecification;
import com.amazonaws.services.mturk.model.QualificationRequirement;
import com.amazonaws.services.mturk.model.UpdateNotificationSettingsRequest;

import jp.ac.keio.ics.db.utils.Properties;

public class HITCreator {	
	public static String loc = Properties.loc;
	private String configFile;
	private String[] textFiles;
	private String question;
	private Collection<QualificationRequirement> qualificationRequirements = new ArrayList<QualificationRequirement>();  //
	
	public HITCreator(String configFile, String[] textFiles){
		this.configFile = configFile;
		this.textFiles = textFiles;
	}
	
	public void createHIT() throws IOException{
		ResourceBundle resource;
		try (FileInputStream fis = new FileInputStream(configFile)) {
			  resource = new PropertyResourceBundle(fis);
			  fis.close();
			}
		CreateHITRequest request = new CreateHITRequest();
		request.setTitle(resource.getString("title"));
		request.setDescription(resource.getString("description"));
		request.setReward(resource.getString("reward"));
		request.setAssignmentDurationInSeconds(Long.decode(resource.getString("assignmentduration")));
		request.setLifetimeInSeconds(Long.decode(resource.getString("hitlifetime")));
		request.setKeywords(resource.getString("keywords"));
		request.setMaxAssignments(Integer.decode(resource.getString("assignments")));
		//TODO Build the qualificationRequirements
		int i = 1;
		while(resource.containsKey("qualification."+i)){
			QualificationRequirement qualif = new QualificationRequirement();
			qualif.setQualificationTypeId(resource.getString("qualification."+i));
			qualif.setComparator(resource.getString("qualification.comparator."+i));
			if(resource.containsKey("qualification.value."+i)){
				ArrayList<Integer> integerValues = new ArrayList<Integer>();
				integerValues.add(Integer.decode(resource.getString("qualification.value."+i)));
				qualif.setIntegerValues(integerValues);
			}
			if(resource.containsKey("qualification.private."+i) && resource.getString("qualification.private."+i).equals("true")){
				qualif.setRequiredToPreview(true);
			} else {
				qualif.setRequiredToPreview(false);
			}
			qualificationRequirements.add(qualif);
			i++;
		}
		request.setQualificationRequirements(qualificationRequirements);
		request.setQuestion(question);
		
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(Properties.endpoint,"us-east-1"));
		AmazonMTurk client = builder.build();
		CreateHITResult createHITResult= client.createHIT(request);
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy, hh:mm a z");
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(new File(loc + "log.txt"), true));
	    writer.println(createHITResult.getHIT().getTitle() + "," + createHITResult.getHIT().getHITId() + "," + sdf.format(createHITResult.getHIT().getCreationTime()));
	    writer.close();
	    
	    if(Properties.email != null){
		    NotificationSpecification notif = new NotificationSpecification();
		    notif.setTransport("Email");
		    notif.setDestination(Properties.email);
		    notif.setVersion("2006-05-05");
		    ArrayList<String> eventTypes = new ArrayList<String>();
		    eventTypes.add("HITReviewable");
		    notif.setEventTypes(eventTypes);
		    
		    UpdateNotificationSettingsRequest notifRequest = new UpdateNotificationSettingsRequest();
		    notifRequest.setHITTypeId(createHITResult.getHIT().getHITTypeId());
		    notifRequest.setNotification(notif);
		    notifRequest.setActive(true);
		    
		    client.updateNotificationSettings(notifRequest);
	    }
	}
	
	public String htmlToQuestion(String html){
		String result ="<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">\n";
		result += "  <HTMLContent><![CDATA[\n";
		result += html;
		result += "]]>\n";
		result += "  </HTMLContent>";
		result += "  <FrameHeight>1000</FrameHeight>";
		result += "</HTMLQuestion>";
		return result;
	}
	
	public void setQuestion(String question){
		this.question = question;
	}
	
	public String[]  getTextFiles(){
		return this.textFiles;
	}
}
