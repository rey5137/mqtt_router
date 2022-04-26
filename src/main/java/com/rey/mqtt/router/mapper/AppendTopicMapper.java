package com.rey.mqtt.router.mapper;

public class AppendTopicMapper implements TopicMapper {

    private String suffix;

    public AppendTopicMapper(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String map(String topic) {
        return String.format("%s%s", topic, suffix);
    }

}
