package com.rey.mqtt.router;

import com.rey.mqtt.router.config.RouterConfig;
import com.rey.mqtt.router.config.TopicMapperProperties;
import com.rey.mqtt.router.mapper.TopicMapper;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Router implements InboundAdapter.OnMessageArrivedCallback {

    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private RouterConfig routerConfig;
    private String name;

    private InboundAdapter inboundAdapter;
    private OutboundAdapter[] outboundAdapters;
    private TopicMapper topicMapper;
    private HashMap<String, Integer> topicMap;
    private int nextIndex = 0;

    public Router(RouterConfig routerConfig) {
        this.routerConfig = routerConfig;
        name = routerConfig.getRouterProperties().name();
        if (name == null)
            name = Integer.toHexString(hashCode());
        inboundAdapter = new InboundAdapter(name, routerConfig.getInConnectionProperties(), this);
        int num = routerConfig.getOutConnectionProperties().numberOfConnections();
        outboundAdapters = new OutboundAdapter[num];
        if (num > 1) {
            topicMap = new HashMap<>();
            for (int i = 0; i < num; i++) {
                outboundAdapters[i] = new OutboundAdapter(String.format("%s#%s", name, i + 1), routerConfig.getOutConnectionProperties(), i + 1);
            }
        } else {
            outboundAdapters[0] = new OutboundAdapter(name, routerConfig.getOutConnectionProperties(), null);
        }
        topicMapper = TopicMapperProperties.buildTopicMapper(routerConfig.getTopicMapperProperties());
    }

    public String getName() {
        return name;
    }

    public boolean start() {
        if (!inboundAdapter.start())
            return false;
        for (OutboundAdapter outboundAdapter : outboundAdapters) {
            if (!outboundAdapter.start()) {
                stop();
                return false;
            }
        }
        return true;
    }

    public void stop() {
        inboundAdapter.stop();
        for (OutboundAdapter outboundAdapter : outboundAdapters) {
            outboundAdapter.stop();
        }
    }

    @Override
    public void onMessageArrived(String topic, MqttMessage message) {
        int index = 0;
        if (routerConfig.getOutConnectionProperties().numberOfConnections() > 1) {
            index = topicMap.computeIfAbsent(topic, key -> {
                int i = nextIndex;
                nextIndex = (nextIndex + 1) % outboundAdapters.length;
                return i;
            });
        }
        try {
            outboundAdapters[index].publish(topicMapper.map(topic), message);
        } catch (Exception e) {
            logger.debug("Error when publish message [{}]", message);
            logger.debug("Exception: ", e);
        }
    }
}
