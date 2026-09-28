package com.broker.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broker.protocol.AckResponse;
import com.broker.protocol.ErrorResponse;
import com.broker.protocol.FetchRequest;
import com.broker.protocol.MessageType;
import com.broker.protocol.MessagesResponse;
import com.broker.protocol.PublishRequest;

public class ClientHandler implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final PublishHandler publishHandler;
    private final FetchHandler fetchHandler;
    private final BrokerServer broker; // we dont need this as publishHandler is instantiated in the start
    private String registeredGroup;
    private String registeredConsumer;

    public ClientHandler(Socket socket, BrokerServer broker) {
        this.socket = socket;
        // this.broker = broker;
        this.publishHandler = new PublishHandler(broker);
        this.fetchHandler = new FetchHandler(broker);
        this.broker = broker;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            String line;
            while ((line = reader.readLine()) != null) {
                handleMessage(line, writer);
            }
        } catch (IOException e) {
            logger.error("Connection error: {}", e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private void handleMessage(String json, PrintWriter writer) {
        try {
            MessageType type = MessageProtocol.peekType(json);

            switch (type) {
                case PUBLISH -> {
                    PublishRequest request = MessageProtocol.fromJson(json, PublishRequest.class);
                    AckResponse response = publishHandler.handle(request);
                    writer.println(MessageProtocol.toJson(response));
                }
                case FETCH -> {
                    FetchRequest request = MessageProtocol.fromJson(json, FetchRequest.class);
                    MessagesResponse response = fetchHandler.handle(request);
                    if (request.group != null && request.consumerId != null) {
                        this.registeredConsumer = request.consumerId;
                        this.registeredGroup = request.group;
                    }
                    writer.println(MessageProtocol.toJson(response));
                }
                default ->
                    sendError(writer, "Unkown message type: " + type);
            }
        } catch (Exception e) {
            logger.error("Error handling message: {}", e.getMessage());
            sendError(writer, "Failed to process message: " + e.getMessage());
        }
    }

    private void sendError(PrintWriter writer, String errorMessage) {
        try {
            ErrorResponse error = new ErrorResponse();
            error.errorMessage = errorMessage;
            writer.println(MessageProtocol.toJson(error));
        } catch (Exception e) {
            logger.error("Failed to send error response: {}", e.getMessage());
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket connection: {}", e.getMessage());
        } finally {
            this.broker.getConsumerGroupManager().leave(registeredGroup, registeredConsumer);
        }
    }

}
