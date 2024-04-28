package com.hc.mq.client.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author hc
 */
public class UniqueIdGenerator {
    private static final AtomicInteger counter = new AtomicInteger();

    public static int generateId() {
        return counter.incrementAndGet();
    }
}
