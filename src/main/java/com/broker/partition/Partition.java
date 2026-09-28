package com.broker.partition;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Partition {

    private final int id;
    private final ConcurrentLinkedQueue<Message> log;
    private final AtomicLong nextOffset;

    public Partition(int id) {
        this.id = id;
        this.log = new ConcurrentLinkedQueue<>();
        this.nextOffset = new AtomicLong(0);
    }

    public long append(String key, String value) {
        long offset = this.nextOffset.getAndIncrement();
        Message message = new Message(offset, key, value);
        log.add(message);
        return offset;
    }

    public List<Message> fetch(long fromOffset, int maxMessages) {
        return log.stream()
                .filter(m -> m.getOffset() >= fromOffset)
                .limit(maxMessages)
                .toList(); // toList returns an immutable list (we want that) and .collect(Collectors.toList()) -> this returns a mutable list 
    }

    public int getId() {
        return this.id;
    }

    public long getCurrentOffset() {
        return this.nextOffset.get();
    }

    public int size() {
        return log.size();
    }
}
