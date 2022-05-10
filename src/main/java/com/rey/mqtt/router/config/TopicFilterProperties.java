package com.rey.mqtt.router.config;

import com.rey.mqtt.router.filter.BlacklistTopicFilter;
import com.rey.mqtt.router.filter.DefaultTopicFilter;
import com.rey.mqtt.router.filter.TopicFilter;
import com.rey.mqtt.router.filter.WhitelistTopicFilter;
import org.aeonbits.owner.Config;

import java.util.Arrays;

public interface TopicFilterProperties extends Config {

    String TYPE_WHITELIST = "whitelist";
    String TYPE_BLACKLIST = "blacklist";

    String prefix();

    @Key("${prefix}.type")
    String type();

    @Key("${prefix}.value")
    String value();

    static TopicFilter buildTopicFilter(TopicFilterProperties properties) {
        String type = properties.type();
        if(TYPE_WHITELIST.equalsIgnoreCase(type))
            return new WhitelistTopicFilter(Arrays.asList(properties.value().split(";")));
        if(TYPE_BLACKLIST.equalsIgnoreCase(type))
            return new BlacklistTopicFilter(Arrays.asList(properties.value().split(";")));
        return new DefaultTopicFilter();
    }

}
