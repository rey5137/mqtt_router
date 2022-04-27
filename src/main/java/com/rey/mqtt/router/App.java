package com.rey.mqtt.router;

import com.rey.mqtt.router.config.LogConfig;
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
        options.addOption(
                Option.builder("l").longOpt("log")
                        .argName("log")
                        .hasArg()
                        .required(false)
                        .desc("Logback config file").build()
        );

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
            if(cmd.hasOption("l")) {
                String logbackConfigFile = cmd.getOptionValue("l");
                LogConfig.setupLog(logbackConfigFile);
            }
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