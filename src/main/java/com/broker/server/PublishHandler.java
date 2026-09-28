package com.broker.server;

import com.broker.partition.Partition;
import com.broker.partition.Topic;
import com.broker.protocol.AckResponse;
import com.broker.protocol.PublishRequest;

public class PublishHandler extends MessageHandler {

    public PublishHandler(BrokerServer broker) {
        super(broker);
    }

    public AckResponse handle(PublishRequest request) {
        Topic topic = getTopic(request.topic);
        Partition partition = topic.routeToPartition(request.key);
        long offset = partition.append(request.key, request.value);

        AckResponse ackResponse = new AckResponse();
        ackResponse.topic = request.topic;
        ackResponse.partition = partition.getId();
        ackResponse.offset = offset;
        return ackResponse;
    }
}
