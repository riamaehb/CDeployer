package jp.ac.keio.ics.db.bCrowd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class EvalHIT {
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();

        Option config = new Option("c", "config", true, "Path to the configuration file");
        config.setRequired(true);
        options.addOption(config);
        
        Option taskType = new Option("tt", "tasktype", true, "Type of task to be created (translation, "
        		+"summary or narrative");
        taskType.setRequired(true);
        options.addOption(taskType);
        
        Option textfiles = new Option("tf", "textfile", true, "Space separated list of text files to populate the HIT");
        textfiles.setRequired(true);
        options.addOption(textfiles);
        
        Option hitIDOpt = new Option("h", "hitID", true, "ID of the HIT to review");
        hitIDOpt.setRequired(true);
        options.addOption(hitIDOpt);
        
        Option action = new Option("a", "action", true, "Should be Eval");
        action.setRequired(false);
        options.addOption(action);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;     
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("EvalHIT", options);
            System.exit(1);
            return;
        }
        
        
        HITCreator creator = null;
        
        String configFile = cmd.getOptionValue("config");
        String textFile[] = cmd.getOptionValues("textfile");
        String hitID = cmd.getOptionValue("hitID");
        
        switch(cmd.getOptionValue("tasktype")){
        	case "translation":
        		creator = new TranslationEvalHITCreator(configFile, textFile, hitID);
        		break;
        	case "summary":
        		creator = new SummaryEvalHITCreator(configFile, textFile, hitID);
        		break;
        	case "narrative":
        		creator = new NarrativeEvalHITCreator(configFile, textFile, hitID);
        		break;
        	default:
        		throw new Exception("Task Type not recognized.\nHas to be one of the following:"
        				+ " translation, summary or narrative.");
        }
        
        creator.createHIT();
        return;
        
        
	}

}
