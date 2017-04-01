package jp.ac.keio.ics.db.utils;

import java.util.Collection;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.GetHITRequest;
import com.amazonaws.services.mturk.model.GetHITResult;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;

public class AMTFunctions {
	public static ListAssignmentsForHITResult getAssigmentsForHIT(String hitID, Collection<String> assignmentStatuses){
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(Properties.endpoint,"us-east-1"));
		AmazonMTurk client = builder.build();
		
		GetHITRequest hitRequest = new GetHITRequest();
		hitRequest.setHITId(hitID);
		GetHITResult hitResult = client.getHIT(hitRequest);
		
		ListAssignmentsForHITRequest listAssignmentsForHITRequest = new ListAssignmentsForHITRequest();
		listAssignmentsForHITRequest.setHITId(hitResult.getHIT().getHITId());
		listAssignmentsForHITRequest.setAssignmentStatuses(assignmentStatuses);
		return client.listAssignmentsForHIT(listAssignmentsForHITRequest);
		
	}
}
