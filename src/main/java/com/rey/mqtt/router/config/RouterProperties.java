package com.rey.mqtt.router.config;

import org.aeonbits.owner.Config;

public interface RouterProperties extends Config {

    @Key("name")
    String name();

}
