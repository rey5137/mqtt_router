package com.rey.mqtt.router;

import com.rey.mqtt.router.config.ConfigLoader;
import com.rey.mqtt.router.config.RouterConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static boolean isRunning = false;

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
            start(propertyFile);
        } catch (ParseException e) {
            helper.printHelp("Usage: ", options);
            System.exit(0);
        }
    }

    private static void start(String propertyFile) {
        Properties prop = new Properties();
        try (InputStream inputStream = new FileInputStream(propertyFile)) {
            prop.load(inputStream);
        } catch (Exception e) {
            logger.error("Cannot load property file: {}", propertyFile);
            return;
        }

        List<RouterConfig> routerConfigs = ConfigLoader.load(prop);
        List<Router> routers = new ArrayList<>();

        for (RouterConfig routerConfig : routerConfigs) {
            Router router = new Router(routerConfig);
            if (!router.start())
                logger.warn("Cannot start router with name [{}]", router.getName());
            else
                routers.add(router);
        }

        if (!routers.isEmpty()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                routers.forEach(Router::stop);
                isRunning = false;
            }));
            isRunning = true;
        } else {
            logger.info("Doesn't have any active route. App will stop!");
        }

        while (isRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

}