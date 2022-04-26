package com.rey.mqtt.router.config;

import com.rey.mqtt.router.mapper.AppendTopicMapper;
import com.rey.mqtt.router.mapper.DefaultTopicMapper;
import com.rey.mqtt.router.mapper.PrependTopicMapper;
import com.rey.mqtt.router.mapper.TopicMapper;
import org.aeonbits.owner.Config;

public interface TopicMapperProperties extends Config {

    String TYPE_PREPEND = "prepend";
    String TYPE_APPEND = "append";

    String prefix();

    @Key("${prefix}.type")
    String type();

    @Key("${prefix}.value")
    String value();

    static TopicMapper buildTopicMapper(TopicMapperProperties properties) {
        String type = properties.type();
        if(TYPE_PREPEND.equalsIgnoreCase(type))
            return new PrependTopicMapper(properties.value());
        if(TYPE_APPEND.equalsIgnoreCase(type))
            return new AppendTopicMapper(properties.value());
        return new DefaultTopicMapper();
    }

}
