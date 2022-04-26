package com.rey.mqtt.router.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String PATTERN_ROUTER = "router\\[\\s*(\\d+)\\s*\\]\\.(.*)";

    public static List<RouterConfig> load(Properties properties) {
        Pattern pattern = Pattern.compile(PATTERN_ROUTER);
        List<RouterConfig> routerConfigs = new ArrayList<>();
        Map<String, Map<String, String>> configMap = new HashMap<>();
        properties.forEach((key, value) -> {
            Matcher matcher = pattern.matcher(key.toString());
            if(matcher.matches()) {
                Map<String, String> propertyMap = configMap.computeIfAbsent(matcher.group(1), k -> new HashMap<>());
                propertyMap.put(matcher.group(2), value.toString());
            }
        });
        configMap.forEach((key, map) -> {
            RouterConfig config = RouterConfig.from(map);
            if(isValid(config))
                routerConfigs.add(config);
            else
                logger.info("Remove route with [{}] index due to invalid config", key);
        });
        return routerConfigs;
    }

    private static boolean isValid(RouterConfig config) {
        if(config.getInConnectionProperties().host() == null)
            return false;
        if(config.getOutConnectionProperties().host() == null)
            return false;
        return true;
    }
}
