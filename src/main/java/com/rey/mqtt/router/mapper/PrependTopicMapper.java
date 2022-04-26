package com.rey.mqtt.router.mapper;

public class PrependTopicMapper implements TopicMapper {

    private String prefix;

    public PrependTopicMapper(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String map(String topic) {
        return String.format("%s%s", prefix, topic);
    }

}
