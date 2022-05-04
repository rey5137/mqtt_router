package com.rey.mqtt.router.router;

public interface OutboundAdapter {

    boolean start();

    void stop();

    void publish(String topic, Object message) throws Exception;

}
