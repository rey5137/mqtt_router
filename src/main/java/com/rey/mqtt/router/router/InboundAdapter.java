package com.rey.mqtt.router.router;

public interface InboundAdapter {

    boolean start();

    void stop();

    interface OnMessageArrivedCallback {

        void onMessageArrived(String topic, Object message);

    }

}
