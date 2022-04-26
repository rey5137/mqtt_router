package com.rey.mqtt.router;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;

public class WindowService {

    private static volatile RouterService routerService;

    private static void start(String[] args) {
        new Thread(() -> {
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

            try {
                cmd = parser.parse(options, args);
                String propertyFile = cmd.getOptionValue("p");
                routerService = new RouterService();
                routerService.run(propertyFile);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void stop() {
        if(routerService != null) {
            routerService.stop();
            routerService = null;
        }
    }

    public static void main(String[] lines) {
        String[] args = lines[0].split("\\s+");
        String command = args[0];
        if ("start".equals(command)) {
            start(Arrays.copyOfRange(args, 1, args.length));
        } else {
            stop();
        }
    }

}
