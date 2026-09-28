package com.broker.server;

import com.broker.protocol.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageProtocol {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object message) throws Exception {
        return mapper.writeValueAsString(message);
    }

    public static MessageType peekType(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        return MessageType.valueOf(node.get("type").asText());
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return mapper.readValue(json, clazz);
    }
}
