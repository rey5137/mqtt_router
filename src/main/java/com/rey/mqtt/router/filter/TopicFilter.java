package com.rey.mqtt.router.filter;

public interface TopicFilter {

    boolean isValid(String topic);

}
