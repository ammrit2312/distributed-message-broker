package com.broker.consumer;

import java.util.concurrent.ConcurrentHashMap;

public class ConsumerGroupManager {

    private final ConcurrentHashMap<String, ConsumerGroup> groups;
    private final int partitionCount;

    public ConsumerGroupManager(int partitionCount) {
        this.groups = new ConcurrentHashMap<>();
        this.partitionCount = partitionCount;
    }

    public ConsumerGroup getOrCreate(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group must not be blank");
        }
        return groups.computeIfAbsent(groupName, name -> new ConsumerGroup(name, partitionCount));
    }

    public ConsumerGroup join(String group, String consumerId) {
        ConsumerGroup consumerGroup = getOrCreate(group);
        consumerGroup.addMember(consumerId);
        return consumerGroup;
    }

    public void leave(String group, String consumerId) {
        if (group == null || consumerId == null) {
            return;
        }
        ConsumerGroup consumerGroup = groups.get(group);
        if (consumerGroup == null) {
            return;
        }
        consumerGroup.removeMember(consumerId);
    }
}
