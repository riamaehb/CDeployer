package jp.ac.keio.ics.db.bCrowd;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jp.ac.keio.ics.db.utils.Properties;

public class FrontEnd {
	
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();

        Option action = new Option("a", "action", true, "Action to perform: Create, Review or Eval");
        action.setRequired(true);
        options.addOption(action);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;        
        
        try {
            cmd = parser.parse(options, args, true);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("bCrowd", options);
            System.exit(1);
            return;
        }
        
        try{
        	ResourceBundle properties = ResourceBundle.getBundle("bCrowd");
        	if(properties.containsKey("basedir")){
        		Properties.loc = properties.getString("basedir");
        	}
        	if(properties.containsKey("sandbox") && properties.getString("sandbox").equals("false")){
        		Properties.endpoint = "mturk-requester.us-east-1.amazonaws.com";
        	}
        	if(properties.containsKey("notificationEmail")){
        		Properties.email = properties.getString("notificationEmail");
        	}
        } catch (MissingResourceException e){
        	e.printStackTrace();
        	System.out.println("No configuration file found, using default values.");
        }
        
        switch(cmd.getOptionValue("a")){
        	case "Create":
        		CreateHIT.main(args);
        		break;
        	case "Review":
        		ReviewHIT.main(args);
        		break;
        	case "Eval":
        		EvalHIT.main(args);
        		break;
        	case "Process":
        		ProcessHIT.main(args);
        		break;
        	default:
        		throw new Exception("Action not recognized.\n Has to be one of the following: " 
        				+ "Create, Review, Eval or Process");
        }
	}
}
