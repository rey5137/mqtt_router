package com.rey.mqtt.router.config;

import org.aeonbits.owner.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

public class RouterConfig {

    private MqttInConnectionProperties inConnectionProperties;
    private MqttOutConnectionProperties outConnectionProperties;
    private RouterProperties routerProperties;
    private TopicMapperProperties topicMapperProperties;
    private TopicFilterProperties topicFilterProperties;

    private RouterConfig(MqttInConnectionProperties inConnectionProperties,
                         MqttOutConnectionProperties outConnectionProperties,
                         RouterProperties routerProperties,
                         TopicMapperProperties TopicMapperProperties,
                         TopicFilterProperties topicFilterProperties) {
        this.inConnectionProperties = inConnectionProperties;
        this.outConnectionProperties = outConnectionProperties;
        this.routerProperties = routerProperties;
        this.topicMapperProperties = TopicMapperProperties;
        this.topicFilterProperties = topicFilterProperties;
    }

    public MqttInConnectionProperties getInConnectionProperties() {
        return inConnectionProperties;
    }

    public MqttOutConnectionProperties getOutConnectionProperties() {
        return outConnectionProperties;
    }

    public RouterProperties getRouterProperties() {
        return routerProperties;
    }

    public TopicMapperProperties getTopicMapperProperties() {
        return topicMapperProperties;
    }

    public TopicFilterProperties getTopicFilterProperties() {
        return topicFilterProperties;
    }

    public static RouterConfig from(Map<?, ?> propertyMap) {
        Map<String, String> prefixMap = new HashMap<>();
        RouterProperties routerProperties = ConfigFactory.create(RouterProperties.class, propertyMap);

        prefixMap.put("prefix", "in");
        MqttInConnectionProperties mqttInConnectionProperties = ConfigFactory.create(MqttInConnectionProperties.class, prefixMap, propertyMap);

        prefixMap.put("prefix", "out");
        MqttOutConnectionProperties mqttOutConnectionProperties = ConfigFactory.create(MqttOutConnectionProperties.class, prefixMap, propertyMap);

        prefixMap.put("prefix", "topic-mapper");
        TopicMapperProperties topicMapperProperties = ConfigFactory.create(TopicMapperProperties.class, prefixMap, propertyMap);

        prefixMap.put("prefix", "topic-filter");
        TopicFilterProperties topicFilterProperties = ConfigFactory.create(TopicFilterProperties.class, prefixMap, propertyMap);

        return new RouterConfig(
                mqttInConnectionProperties,
                mqttOutConnectionProperties,
                routerProperties,
                topicMapperProperties,
                topicFilterProperties
        );
    }
}
