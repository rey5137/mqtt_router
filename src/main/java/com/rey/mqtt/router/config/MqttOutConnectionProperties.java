package com.rey.mqtt.router.config;

public interface MqttOutConnectionProperties extends MqttConnectionProperties {

    @DefaultValue("1")
    @Key("${prefix}.number_of_connections")
    int numberOfConnections();

}
