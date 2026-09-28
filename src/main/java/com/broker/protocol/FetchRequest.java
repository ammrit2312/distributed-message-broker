package com.broker.protocol;

public class FetchRequest {

    public MessageType type = MessageType.FETCH;
    public String topic;
    public int partition;
    public String group;
    public String consumerId;
    public long fromOffset;
    public int maxMessages;
}