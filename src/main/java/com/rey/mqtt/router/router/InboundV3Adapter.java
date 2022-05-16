package com.rey.mqtt.router.router;

import com.rey.mqtt.router.config.MqttConnectionProperties;
import com.rey.mqtt.router.config.MqttInConnectionProperties;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class InboundV3Adapter implements InboundAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InboundV3Adapter.class);

    private String name;
    private MqttInConnectionProperties mqttConnectionProperties;
    private OnMessageArrivedCallback onMessageArrivedCallback;

    private MqttAsyncClient v3Client;
    private boolean isRunning = false;

    public InboundV3Adapter(String name, MqttInConnectionProperties mqttConnectionProperties, OnMessageArrivedCallback onMessageArrivedCallback) {
        this.name = name;
        this.mqttConnectionProperties = mqttConnectionProperties;
        this.onMessageArrivedCallback = onMessageArrivedCallback;
    }

    public boolean start() {
        if (isRunning)
            return true;

        isRunning = true;
        try {
            String clientId = mqttConnectionProperties.clientId();
            if (clientId == null || clientId.isEmpty()) {
                clientId = String.format("client-%s", UUID.randomUUID());
            }
            v3Client = new MqttAsyncClient(mqttConnectionProperties.host(), clientId, MqttConnectionProperties.getPersistenceV3(mqttConnectionProperties));
            v3Client.setCallback(new Callback());

            IMqttToken connectToken = v3Client.connect(MqttConnectionProperties.getConnectionOptionsV3(mqttConnectionProperties));
            connectToken.waitForCompletion();

            IMqttToken subToken = v3Client.subscribe(mqttConnectionProperties.topic(), mqttConnectionProperties.qos());
            subToken.waitForCompletion();
            logger.debug("[{}] Inbound Adapter - client started", name);
            return true;
        } catch (MqttException ex) {
            logger.debug("[{}] Inbound Adapter - Error when start client", name);
            logger.debug("Exception: ", ex);
            closeClient();
            isRunning = false;
            return false;
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            disconnectClient();
            closeClient();
            logger.debug("[{}] Inbound Adapter - client stopped", name);
        }
    }

    private void disconnectClient() {
        if (v3Client == null)
            return;

        try {
            IMqttToken disconnectToken = v3Client.disconnect();
            disconnectToken.waitForCompletion();
        } catch (MqttException ex) {
            logger.debug("[{}] Inbound Adapter - Error when disconnect client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private void closeClient() {
        if (v3Client == null)
            return;

        try {
            v3Client.close();
        } catch (MqttException ex) {
            logger.debug("[{}] Inbound Adapter - Error when close client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private class Callback implements MqttCallbackExtended {

        @Override
        public void connectionLost(Throwable cause) {
            logger.debug("[{}] Inbound Adapter - connection lost", name);
            logger.debug("Exception: ", cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.debug("[{}] Inbound Adapter - Receive message [{}] from topic [{}] ", name, message, topic);
            onMessageArrivedCallback.onMessageArrived(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            logger.debug("[{}] Inbound Adapter - connected: {}", name, reconnect);
            if(reconnect) {
                try {
                    IMqttToken subToken = v3Client.subscribe(mqttConnectionProperties.topic(), mqttConnectionProperties.qos());
                    subToken.waitForCompletion();
                    logger.debug("[{}] Inbound Adapter - resubscribe successfully", name);
                } catch (MqttException e) {
                    logger.debug("[{}] Inbound Adapter - Error when resubscribe topic", name);
                    logger.debug("Exception: ", e);
                }
            }
        }
    }

}
