package jp.ac.keio.ics.db.bCrowd;

import org.apache.commons.cli.*;

public class CreateHIT {
	
	public static void main(String[] args) throws Exception {
	
		Options options = new Options();

        Option config = new Option("c", "config", true, "Path to the configuration file");
        config.setRequired(true);
        options.addOption(config);
        
        Option taskType = new Option("tt", "tasktype", true, "Type of task to be created (translation, "
        		+"summary, narrative, translationimprove, summaryimprove or narrativeimprove");
        taskType.setRequired(true);
        options.addOption(taskType);
        
        Option textfiles = new Option("tf", "textfile", true, "Space separated list of text files to populate the HIT");
        textfiles.setRequired(true);
        textfiles.setArgs(2);
        textfiles.setOptionalArg(true);
        options.addOption(textfiles);
        
        Option action = new Option("a", "action", true, "Should be Create");
        action.setRequired(false);
        options.addOption(action);


        
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
        
        HITCreator creator = null;
        
        String configFile = cmd.getOptionValue("config");
        String[] textFiles = cmd.getOptionValues("textfile");                
        
        switch(cmd.getOptionValue("tasktype")){
        	case "translation":
        		creator = new TranslationHITCreator(configFile, textFiles);
        		break;
        	case "summary":
        		creator = new SummaryHITCreator(configFile, textFiles);
        		break;
        	case "narrative":
        		creator = new NarrativeHITCreator(configFile, textFiles);
        		break;
        	case "translationimprove":
        		creator = new TranslationImprovHITCreator(configFile, textFiles);
        		break;
        	case "summaryimprove":
        		creator = new SummaryImprovHITCreator(configFile, textFiles);
        		break;
        	case "narrativeimprove":
        		creator = new NarrativeImprovHITCreator(configFile, textFiles);
        		break;
        	default:
        		throw new Exception("Task Type not recognized.\n Has to one of: translation, "
        		+"summary, narrative, translationimprove, summaryimprove or narrativeimprove");
        }
        
        creator.createHIT();
        return;
        
        
	}

}
