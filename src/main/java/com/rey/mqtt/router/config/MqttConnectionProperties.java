package com.rey.mqtt.router.config;

import org.aeonbits.owner.Config;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;

public interface MqttConnectionProperties extends Config {

    String PERSISTENCE_TYPE_MEMORY = "memory";
    String PERSISTENCE_TYPE_FILE = "file";

    String VERSION_3 = "3";
    String VERSION_5 = "5";

    String prefix();

    @DefaultValue("3")
    @Key("${prefix}.version")
    String version();

    @Key("${prefix}.host")
    String host();

    @Key("${prefix}.client_id")
    String clientId();

    @DefaultValue("false")
    @Key("${prefix}.automatic_reconnect")
    boolean automaticReconnect();

    @Key("${prefix}.username")
    String username();

    @Key("${prefix}.password")
    String password();

    @DefaultValue("memory")
    @Key("${prefix}.persistence.type")
    String persistenceType();

    @Key("${prefix}.persistence.directory")
    String persistenceDirectory();

    static MqttConnectionOptions getConnectionOptions(MqttConnectionProperties properties) {
        MqttConnectionOptions options = new MqttConnectionOptions();
        if (properties.host() != null) {
            options.setServerURIs(new String[]{properties.host()});
        }
        if(properties.automaticReconnect()) {
            options.setAutomaticReconnect(true);
        }
        if(properties.username() != null) {
            options.setUserName(properties.username());
        }
        if(properties.password() != null) {
            options.setPassword(properties.password().getBytes());
        }
        return options;
    }

    static org.eclipse.paho.client.mqttv3.MqttConnectOptions getConnectionOptionsV3(MqttConnectionProperties properties) {
        org.eclipse.paho.client.mqttv3.MqttConnectOptions options = new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
        if (properties.host() != null) {
            options.setServerURIs(new String[]{properties.host()});
        }
        if(properties.automaticReconnect()) {
            options.setAutomaticReconnect(true);
        }
        if(properties.username() != null) {
            options.setUserName(properties.username());
        }
        if(properties.password() != null) {
            options.setPassword(properties.password().toCharArray());
        }
        return options;
    }

    static MqttClientPersistence getPersistence(MqttConnectionProperties properties) {
        if(PERSISTENCE_TYPE_FILE.equalsIgnoreCase(properties.persistenceType())) {
            return new MqttDefaultFilePersistence(properties.persistenceDirectory());
        }
        return new MemoryPersistence();
    }

    static org.eclipse.paho.client.mqttv3.MqttClientPersistence getPersistenceV3(MqttConnectionProperties properties) {
        if(PERSISTENCE_TYPE_FILE.equalsIgnoreCase(properties.persistenceType())) {
            return new org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence(properties.persistenceDirectory());
        }
        return new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence();
    }
}
