package com.broker.server;

import com.broker.partition.Topic;

public abstract class MessageHandler {

    protected final BrokerServer broker;

    public MessageHandler(BrokerServer broker) {
        this.broker = broker;
    }

    public Topic getTopic(String topicName) {
        Topic topic = broker.getTopic(topicName);
        if (topic == null) {
            throw new IllegalArgumentException("Topic not found: " + topicName);
        }
        return topic;
    }
}
