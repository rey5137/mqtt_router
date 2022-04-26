package com.rey.mqtt.router.mapper;

public class DefaultTopicMapper implements TopicMapper {

    @Override
    public String map(String topic) {
        return topic;
    }

}
