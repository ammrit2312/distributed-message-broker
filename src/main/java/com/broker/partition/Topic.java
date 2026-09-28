package com.broker.partition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Topic {
    private final String name;
    private final List<Partition> partitions;
    private final AtomicInteger roundRobinCounter;

    public Topic(String name, int partitionCount) {
        this.name = name;
        this.partitions = new ArrayList<>();
        for(int i=0; i<partitionCount; i++) {
            this.partitions.add(new Partition(i));
        }
        this.roundRobinCounter = new AtomicInteger(0);
    }

    public Partition routeToPartition(String key) { // this will take each message key and tell which partition it belongs to
        if(key==null) {
            int index = Math.abs(roundRobinCounter.getAndIncrement()) % partitions.size(); // .getAndIncrement() always returns the old value and then increments the exisiting value
            return partitions.get(index);
        }
        int index = Math.abs(key.hashCode()) % partitions.size();
        return partitions.get(index);
    }

    public Partition getPartition(int partitionId) {
        return partitions.get(partitionId);
    }

    public String getName() {
        return name;
    }

    public int getPartitionCount() {
        return partitions.size();
    }

    public List<Partition> getPartitions() {
        return partitions;
    }
}