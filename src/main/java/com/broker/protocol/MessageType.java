package com.broker.protocol;

public enum MessageType {
    PUBLISH, // producer → broker: send a message
    ACK, // broker → producer: message received at this offset
    FETCH, // consumer → broker: give me messages from this offset
    MESSAGES, // broker → consumer: here are your messages
    ERROR      // broker → anyone: something went wrong
}
