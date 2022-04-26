package com.rey.mqtt.router;

import com.rey.mqtt.router.config.MqttConnectionProperties;
import com.rey.mqtt.router.config.MqttInConnectionProperties;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class InboundAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InboundAdapter.class);

    private String name;
    private MqttInConnectionProperties mqttConnectionProperties;
    private OnMessageArrivedCallback onMessageArrivedCallback;

    private MqttAsyncClient v5Client;
    private boolean isRunning = false;

    public InboundAdapter(String name, MqttInConnectionProperties mqttConnectionProperties, OnMessageArrivedCallback onMessageArrivedCallback) {
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
            v5Client = new MqttAsyncClient(mqttConnectionProperties.host(), clientId, MqttConnectionProperties.getPersistence(mqttConnectionProperties));
            v5Client.setCallback(new Callback());

            IMqttToken connectToken = v5Client.connect(MqttConnectionProperties.getConnectionOptions(mqttConnectionProperties));
            connectToken.waitForCompletion(mqttConnectionProperties.actionTimeoutInMillis());

            IMqttToken subToken = v5Client.subscribe(mqttConnectionProperties.topic(), mqttConnectionProperties.qos());
            subToken.waitForCompletion(mqttConnectionProperties.actionTimeoutInMillis());
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
        if (v5Client == null)
            return;

        try {
            IMqttToken disconnectToken = v5Client.disconnect();
            disconnectToken.waitForCompletion(mqttConnectionProperties.actionTimeoutInMillis());
        } catch (MqttException ex) {
            logger.debug("[{}] Inbound Adapter - Error when disconnect client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private void closeClient() {
        if (v5Client == null)
            return;

        try {
            v5Client.close();
        } catch (MqttException ex) {
            logger.debug("[{}] Inbound Adapter - Error when close client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private class Callback implements MqttCallback {

        @Override
        public void disconnected(MqttDisconnectResponse disconnectResponse) {
        }

        @Override
        public void mqttErrorOccurred(MqttException exception) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.debug("[{}] Inbound Adapter - Receive message [{}] from topic [{}] ", name, message, topic);
            onMessageArrivedCallback.onMessageArrived(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttToken token) {
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
        }

        @Override
        public void authPacketArrived(int reasonCode, MqttProperties properties) {
        }
    }

    public interface OnMessageArrivedCallback {

        void onMessageArrived(String topic, MqttMessage message);

    }
}
