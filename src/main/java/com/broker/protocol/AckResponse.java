package com.broker.protocol;

public class AckResponse {

    public MessageType type = MessageType.ACK;
    public String topic;
    public int partition;
    public long offset;
}
