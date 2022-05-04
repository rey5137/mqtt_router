package com.rey.mqtt.router.router;

import com.rey.mqtt.router.config.MqttConnectionProperties;
import com.rey.mqtt.router.config.MqttOutConnectionProperties;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class OutboundV3Adapter implements OutboundAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OutboundV3Adapter.class);

    private String name;
    private MqttOutConnectionProperties mqttConnectionProperties;
    private Integer clientIndex;

    private MqttAsyncClient v3Client;
    private boolean isRunning = false;

    public OutboundV3Adapter(String name, MqttOutConnectionProperties mqttConnectionProperties, Integer clientIndex) {
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
            v3Client = new MqttAsyncClient(mqttConnectionProperties.host(), clientId, MqttConnectionProperties.getPersistenceV3(mqttConnectionProperties));
            v3Client.setCallback(new Callback());

            IMqttToken connectToken = v3Client.connect(MqttConnectionProperties.getConnectionOptionsV3(mqttConnectionProperties));
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
        if(message instanceof org.eclipse.paho.mqttv5.common.MqttMessage)
            publish(topic, buildMessage((org.eclipse.paho.mqttv5.common.MqttMessage)message));
        else if(message instanceof MqttMessage)
            publish(topic, (MqttMessage)message);
    }

    private void publish(String topic, MqttMessage message) throws MqttException {
        if (isRunning) {
            logger.debug("[{}] Outbound Adapter - Sending message [{}] to topic [{}]", name, toDebugString(message), topic);
            IMqttToken token = v3Client.publish(topic, message);
            logger.debug("[{}] Outbound Adapter - Message token is [{}]", name, token.hashCode());
        }
    }

    private String toDebugString(MqttMessage mqttMessage) {
        return "MqttMessage [payload=" + new String(mqttMessage.getPayload()) + ", qos=" + mqttMessage.getQos() + ", retained="
                + mqttMessage.isRetained() + ", dup=" + mqttMessage.isDuplicate() + ", messageId=" + mqttMessage.getId() + "]";
    }

    private MqttMessage buildMessage(org.eclipse.paho.mqttv5.common.MqttMessage v5Message) {
        MqttMessage message = new MqttMessage();
        message.setPayload(v5Message.getPayload());
        message.setQos(v5Message.getQos());
        message.setRetained(v5Message.isRetained());
        return message;
    }

    private void disconnectClient() {
        if (v3Client == null)
            return;

        try {
            IMqttToken disconnectToken = v3Client.disconnect();
            disconnectToken.waitForCompletion();
        } catch (MqttException ex) {
            logger.debug("[{}] Outbound Adapter - Error when disconnect client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private void closeClient() {
        if (v3Client == null)
            return;

        try {
            v3Client.close();
        } catch (MqttException ex) {
            logger.debug("[{}] Outbound Adapter - Error when close client", name);
            logger.debug("Exception: ", ex);
        }
    }

    private class Callback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {}

        @Override
        public void messageArrived(String topic, MqttMessage message) {}

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.debug("[{}] Outbound Adapter - Message with token [{}] delivered", name, token.hashCode());
        }

    }

}
