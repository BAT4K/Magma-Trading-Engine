package com.trading.engine;

import com.trading.model.LimitOrder;

import java.util.concurrent.atomic.AtomicBoolean;

public class TradingEngine implements Runnable {
    private final OrderBook orderBook;
    public final RingBuffer ringBuffer;
    private final AtomicBoolean running = new AtomicBoolean(true);

    // METRICS: Store latency for 1 Million orders (approx 8MB RAM)
    public final long[] latencies;
    public long processedCount = 0;

    public TradingEngine(int bufferSize) {
        this.orderBook = new OrderBook();
        this.ringBuffer = new RingBuffer(bufferSize);
        this.latencies = new long[1_000_000]; // Pre-allocate storage
    }

    @Override
    public void run() {
        while (running.get()) {
            LimitOrder order = ringBuffer.tryNext();

            if (order != null) {
                long processingStart = System.nanoTime();

                try {
                    orderBook.addOrder(order);

                    // --- CAPTURE LATENCY ---
                    long end = System.nanoTime();

                    if (processedCount < latencies.length) {
                        latencies[(int) processedCount] = end - processingStart;
                    }
                    // -----------------------

                    processedCount++;
                    order.clear();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Thread.onSpinWait();
            }
        }
    }

    public void stop() {
        running.set(false);
    }
}