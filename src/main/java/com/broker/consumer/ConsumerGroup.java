package com.broker.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumerGroup {

    private final String groupName;
    private final List<String> members;
    private final Map<Integer, String> assignments; // we use a normal hashmap and not threadsafe version, we take care of thread safety in writing synchronized methods. Why not use ConcurrentHashMap? - it is a good suggestion but during rebalance two threads might run at the same time and some data can be lost. 
    private final int partitionCount;

    public ConsumerGroup(String groupName, int partitionCount) {
        this.groupName = groupName;
        this.partitionCount = partitionCount;
        this.members = new ArrayList<>();
        this.assignments = new HashMap<>();
    }

    public synchronized void addMember(String consumerId) {
        if (members.contains(consumerId)) {
            return;
        }
        members.add(consumerId);
        rebalance(partitionCount);
    }

    public synchronized void removeMember(String consumerId) {
        if (!members.contains(consumerId)) {
            return;
        }
        members.remove(consumerId);
        rebalance(partitionCount);
    }

    private void rebalance(int partitionCount) {
        assignments.clear();

        if (members.isEmpty() || partitionCount == 0) {
            return;
        }

        List<String> ordered = new ArrayList<>(members);
        Collections.sort(ordered);

        int membersCount = ordered.size();

        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            assignments.put(partitionId, ordered.get(partitionId % membersCount));
        }
    }

    public synchronized List<Integer> getAssignedPartitions(String consumerId) {
        List<Integer> owned = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : assignments.entrySet()) {
            if (entry.getValue().equals(consumerId)) {
                owned.add(entry.getKey());
            }
        }
        Collections.sort(owned);
        return owned;
    }

    public synchronized Integer getPartitionForConsumer(String consumerId) {
        List<Integer> owned = getAssignedPartitions(consumerId);
        if (owned.isEmpty()) {
            return null;
        }
        return owned.get(0);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public synchronized int getMemberCount() {
        return this.members.size();
    }

}
