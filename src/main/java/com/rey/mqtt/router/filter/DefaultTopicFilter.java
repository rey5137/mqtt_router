package com.rey.mqtt.router.filter;

public class DefaultTopicFilter implements TopicFilter {
    @Override
    public boolean isValid(String topic) {
        return true;
    }
}
