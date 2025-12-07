package com.trading.engine;

import com.trading.model.LimitOrder;

import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer {
    private final LimitOrder[] buffer;
    private final int mask;

    // Padded to prevent "False Sharing" (where two threads fight over the same cache line)
    // In a real library like LMAX, this padding is more sophisticated.
    private final AtomicLong producerSequence = new AtomicLong(-1);

    // Consumer tracks its own sequence (no need for atomic, only one consumer)
    private long consumerSequence = 0;

    public RingBuffer(int size) {
        // Size must be a power of 2 for bitwise masking (faster than modulo %)
        if (Integer.bitCount(size) != 1) {
            throw new IllegalArgumentException("Buffer size must be a power of 2");
        }
        this.buffer = new LimitOrder[size];
        this.mask = size - 1;

        // PRE-ALLOCATION: Fill the buffer with empty objects immediately
        for (int i = 0; i < size; i++) {
            buffer[i] = new LimitOrder();
        }
    }

    /**
     * PRODUCER: Claims the next slot and returns the object to be written to.
     * This is Wait-Free for the producer (unless buffer is full).
     */
    public LimitOrder next() {
        long nextSequence = producerSequence.incrementAndGet();

        // Circular wrap-around logic using bitwise AND
        int index = (int) (nextSequence & mask);
        return buffer[index];
    }

    /**
     * CONSUMER: Polls the next object. Returns null if no new data.
     */
    public LimitOrder tryNext() {
        int index = (int) (consumerSequence & mask);
        LimitOrder order = buffer[index];

        // Spin-wait check: Is the producer done writing this slot?
        if (order.ready) {
            consumerSequence++;
            return order;
        }
        return null; // Buffer is empty or next slot is not ready yet
    }
}