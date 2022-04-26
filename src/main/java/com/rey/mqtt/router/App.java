package com.rey.mqtt.router;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(
                Option.builder("p").longOpt("properties")
                        .argName("properties")
                        .hasArg()
                        .required(true)
                        .desc("Location of properties file").build()
        );

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
            String propertyFile = cmd.getOptionValue("p");
            RouterService routerService = new RouterService();
            routerService.run(propertyFile);
            Runtime.getRuntime().addShutdownHook(new Thread(routerService::stop));
        } catch (ParseException e) {
            helper.printHelp("Usage: ", options);
            System.exit(0);
        }
    }

}