package cn.turingmoon;

import org.apache.commons.cli.*;

public class Cli {
    static Options options = new Options();

    static {
        options.addOption("h","help", false, "The command help");
        options.addOption("f","file", false, "filename");
    }

    static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("Network Abnormal Traffic Detector", options);
    }

    public static void main(String[] args){
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("h")) {
                printHelp();
                return;
            }

            String filename = cl.getOptionValue("file");
            System.out.println(filename);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
