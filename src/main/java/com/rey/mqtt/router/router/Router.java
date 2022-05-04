package com.rey.mqtt.router.router;

import com.rey.mqtt.router.config.MqttConnectionProperties;
import com.rey.mqtt.router.config.MqttInConnectionProperties;
import com.rey.mqtt.router.config.MqttOutConnectionProperties;
import com.rey.mqtt.router.config.RouterConfig;
import com.rey.mqtt.router.config.TopicMapperProperties;
import com.rey.mqtt.router.mapper.TopicMapper;
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
        inboundAdapter = buildInboundAdapter(routerConfig.getInConnectionProperties());
        int num = routerConfig.getOutConnectionProperties().numberOfConnections();
        outboundAdapters = new OutboundAdapter[num];
        if (num > 1) {
            topicMap = new HashMap<>();
            for (int i = 0; i < num; i++) {
                outboundAdapters[i] = buildOutboundAdapter(String.format("%s#%s", name, i + 1), routerConfig.getOutConnectionProperties(), i + 1);
            }
        } else {
            outboundAdapters[0] = buildOutboundAdapter(name, routerConfig.getOutConnectionProperties(), null);
        }
        topicMapper = TopicMapperProperties.buildTopicMapper(routerConfig.getTopicMapperProperties());
    }

    private InboundAdapter buildInboundAdapter(MqttInConnectionProperties properties) {
        if(MqttConnectionProperties.VERSION_3.equals(properties.version()))
            return new InboundV3Adapter(name, properties, this);
        return new InboundV5Adapter(name, properties, this);
    }

    private OutboundAdapter buildOutboundAdapter(String name, MqttOutConnectionProperties properties, Integer index) {
        if(MqttConnectionProperties.VERSION_3.equals(properties.version()))
            return new OutboundV3Adapter(name, properties, index);
        return new OutboundV5Adapter(name, properties, index);
    }

    public String getName() {
        return name;
    }

    public boolean start() {
        for (OutboundAdapter outboundAdapter : outboundAdapters) {
            if (!outboundAdapter.start()) {
                stop();
                return false;
            }
        }
        if (!inboundAdapter.start()) {
            stop();
            return false;
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
    public void onMessageArrived(String topic, Object message) {
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
            logger.debug("[{}] Route - Error when publish message [{}]", name, message);
            logger.debug("Exception: ", e);
        }
    }
}
