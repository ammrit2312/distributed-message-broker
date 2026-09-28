package com.broker.protocol;

import java.util.List;

import com.broker.partition.Message;

public class MessagesResponse {

    public MessageType type = MessageType.MESSAGES;
    public String topic;
    public int partition;
    public List<Message> messages;
}
