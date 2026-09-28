package com.broker.protocol;

public class PublishRequest {

    public MessageType type = MessageType.PUBLISH;
    public String topic;
    public String key;
    public String value;
}
