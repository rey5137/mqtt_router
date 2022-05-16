package com.rey.mqtt.router.router;

import com.rey.mqtt.router.config.MqttConnectionProperties;
import com.rey.mqtt.router.config.MqttOutConnectionProperties;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class OutboundV5Adapter implements OutboundAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OutboundV5Adapter.class);

    private String name;
    private MqttOutConnectionProperties mqttConnectionProperties;
    private Integer clientIndex;

    private MqttAsyncClient v5Client;
    private boolean isRunning = false;

    public OutboundV5Adapter(String name, MqttOutConnectionProperties mqttConnectionProperties, Integer clientIndex) {
        this.name = name;
        this.mqttConnectionProperties = mqttConnectionProperties;
        this.clientIndex = clientIndex;
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
            if (clientIndex != null) {
                clientId = String.format("%s#%s", clientId, clientIndex);
            }
            v5Client = new MqttAsyncClient(mqttConnectionProperties.host(), clientId, MqttConnectionProperties.getPersistence(mqttConnectionProperties));
            v5Client.setCallback(new Callback());

            IMqttToken connectToken = v5Client.connect(MqttConnectionProperties.getConnectionOptions(mqttConnectionProperties));
            connectToken.waitForCompletion();
            logger.debug("[{}] Outbound Adapter - client started", name);
            return true;
        } catch (MqttException ex) {
            logger.debug("[{}] Outbound Adapter - Error when start client", name);
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
            logger.debug("[{}] Outbound Adapter - client stopped", name);
        }
    }

    public void publish(String topic, Object message) throws Exception {
        if (message instanceof org.eclipse.paho.mqttv5.common.MqttMessage)
            publish(topic, (org.eclipse.paho.mqttv5.common.MqttMessage) message);
        else if (message instanceof org.eclipse.paho.client.mqttv3.MqttMessage)
            publish(topic, buildMessage((org.eclipse.paho.client.mqttv3.MqttMessage) message));
    }

    private void publish(String topic, MqttMessage message) throws MqttException {
        if (isRunning) {
            logger.debug("[{}] Outbound Adapter - Sending message [{}] to topic [{}]", name, message.toDebugString(), topic);
            IMqttToken token = v5Client.publish(topic, message);
            logger.debug("[{}] Outbound Adapter - Message token is [{}]", name, token.hashCode());
        }
    }

    private MqttMessage buildMessage(org.eclipse.paho.client.mqttv3.MqttMessage v3Message) {
        MqttMessage message = new MqttMessage();
        message.setPayload(v3Message.getPayload());
        message.setQos(v3Message.getQos());
        message.setRetained(v3Message.isRetained());
        message.setDuplicate(v3Message.isDuplicate());
        return message;
    }

    private void disconnectClient() {
        if (v5Client == null)
            return;

        try {
            IMqttToken disconnectToken = v5Client.disconnect();
            disconnectToken.waitForCompletion();
        } catch (MqttException ex) {
            logger.debug("[{}] Outbound Adapter - Error when disconnect client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private void closeClient() {
        if (v5Client == null)
            return;

        try {
            v5Client.close();
        } catch (MqttException ex) {
            logger.debug("[{}] Outbound Adapter - Error when close client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private class Callback implements MqttCallback {

        @Override
        public void disconnected(MqttDisconnectResponse disconnectResponse) {
            logger.debug("[{}] Outbound Adapter - disconnected: {}", name, disconnectResponse);
            logger.debug("Exception: ", disconnectResponse.getException());
        }

        @Override
        public void mqttErrorOccurred(MqttException exception) {
            logger.debug("[{}] Outbound Adapter - error occurred", name);
            logger.debug("Exception: ", exception);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.debug("[{}] Outbound Adapter - received message", name);
        }

        @Override
        public void deliveryComplete(IMqttToken token) {
            logger.debug("[{}] Outbound Adapter - Message with token [{}] delivered", name, token.hashCode());
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            logger.debug("[{}] Outbound Adapter - connected: {}", name, reconnect);
        }

        @Override
        public void authPacketArrived(int reasonCode, MqttProperties properties) {
        }
    }

}
