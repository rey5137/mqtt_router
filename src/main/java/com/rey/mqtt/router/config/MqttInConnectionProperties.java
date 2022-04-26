package com.rey.mqtt.router.config;

public interface MqttInConnectionProperties extends MqttConnectionProperties {

    @Key("${prefix}.topic")
    String topic();

    @DefaultValue("1")
    @Key("${prefix}.qos")
    int qos();
}
