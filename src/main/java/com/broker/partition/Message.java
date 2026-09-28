package com.broker.partition;

public class Message {
    private final long offset; // the id for a message
    private final String key;
    private final String value;
    private final long timestamp;

    public Message(long offset, String key, String value) {
        this.offset = offset;
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public long getOffset() {
        return offset;
    }
    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{offset=" + offset + ", key='" + key + "', value='" +
                value + "', timestamp=" + timestamp + "}";  
    }
}