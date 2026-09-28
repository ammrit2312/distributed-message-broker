package com.broker.server;

import java.util.List;

import com.broker.consumer.ConsumerGroup;
import com.broker.consumer.ConsumerGroupManager;
import com.broker.offset.OffsetManager;
import com.broker.partition.Message;
import com.broker.partition.Partition;
import com.broker.partition.Topic;
import com.broker.protocol.FetchRequest;
import com.broker.protocol.MessagesResponse;

public class FetchHandler extends MessageHandler {

    private final OffsetManager offsetManager;

    public FetchHandler(BrokerServer broker) {
        super(broker);
        this.offsetManager = new OffsetManager(broker.getDataSourceProvider().getDataSource());
    }

    public MessagesResponse handle(FetchRequest request) {
        Topic topic = getTopic(request.topic);

        int partitionId;
        if (request.group != null) {
            if (request.consumerId == null || request.consumerId.isBlank()) {
                throw new IllegalArgumentException("consumerId is required when group is set");
            }
            ConsumerGroupManager groupManager = broker.getConsumerGroupManager();
            ConsumerGroup consumerGroup = groupManager.join(request.group, request.consumerId);
            Integer assigned = consumerGroup.getPartitionForConsumer(request.consumerId);
            if (assigned == null) {
                // no partition for this consumer: probably oversubscribed
                MessagesResponse empty = new MessagesResponse();
                empty.messages = List.of();
                empty.topic = request.topic;
                empty.partition = -1;
                return empty;
            }
            partitionId = assigned;
        } else {
            partitionId = request.partition;
        }

        Partition partition = topic.getPartition(partitionId);

        long startOffset = (request.group != null) ? offsetManager.loadOffset(request.group, request.topic, partitionId) : request.fromOffset;

        List<Message> messages = partition.fetch(startOffset, request.maxMessages);

        if (request.group != null && !messages.isEmpty()) {
            long newOffset = messages.get(messages.size() - 1).getOffset() + 1;
            offsetManager.saveOffset(request.group, request.topic, partitionId, newOffset);
        }

        MessagesResponse response = new MessagesResponse();
        response.messages = messages;
        response.topic = request.topic;
        response.partition = partitionId;
        return response;
    }
}
