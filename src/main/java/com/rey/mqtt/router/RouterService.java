package com.rey.mqtt.router;

import com.rey.mqtt.router.config.ConfigLoader;
import com.rey.mqtt.router.config.RouterConfig;
import com.rey.mqtt.router.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RouterService {

    private static final Logger logger = LoggerFactory.getLogger(RouterService.class);

    private boolean isRunning = false;

    public void run(String propertyFile) {
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
            isRunning = true;
        } else {
            logger.info("Doesn't have any active route!");
        }

        while (isRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        routers.forEach(Router::stop);
    }

    public void stop() {
        isRunning = false;
    }
}
