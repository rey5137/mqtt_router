package com.rey.mqtt.router.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WhitelistTopicFilter implements TopicFilter {

    private Set<String> topics;

    public WhitelistTopicFilter(Collection<String> topics) {
        this.topics = new HashSet<>(topics);
    }

    @Override
    public boolean isValid(String topic) {
        return topics.contains(topic);
    }
}
