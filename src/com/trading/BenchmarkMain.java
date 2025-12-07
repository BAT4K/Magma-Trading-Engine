package com.trading;

import com.trading.engine.TradingEngine;
import com.trading.model.LimitOrder;
import com.trading.model.Side;

import java.util.Arrays;

public class BenchmarkMain {
    private static final int ITERATIONS = 1_000_000;
    private static final int BUFFER_SIZE = 1024 * 1024;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Initializing Engine...");
        TradingEngine engine = new TradingEngine(BUFFER_SIZE);
        Thread engineThread = new Thread(engine);
        engineThread.start();

        System.out.println("Warmup phase (JVM JIT optimization)...");
        Thread.sleep(1000);

        System.out.println("Starting Benchmark for " + ITERATIONS + " orders...");
        long startTime = System.nanoTime();

        // Flood the engine with orders
        for (int i = 0; i < ITERATIONS; i++) {
            LimitOrder order = engine.ringBuffer.next();
            Side side = (i % 2 == 0) ? Side.BUY : Side.SELL;
            double price = 100.0 + (i % 5);
            order.set("ORD" + i, side, price, 10);
        }

        // Wait for engine to catch up
        while (engine.processedCount < ITERATIONS) {
            Thread.onSpinWait();
        }

        long endTime = System.nanoTime();
        engine.stop();
        engineThread.join();

        // --- CALCULATE STATS ---
        long durationNs = endTime - startTime;
        double durationSec = durationNs / 1_000_000_000.0;

        // Sort the collected latencies to find percentiles
        Arrays.sort(engine.latencies);

        long p50 = engine.latencies[(int) (ITERATIONS * 0.50)]; // Median
        long p99 = engine.latencies[(int) (ITERATIONS * 0.99)]; // 99% are faster than this
        long max = engine.latencies[ITERATIONS - 1];            // The absolute worst case

        System.out.println("------------------------------------------------");
        System.out.println("Done!");
        System.out.println("Throughput:    " + (long)(ITERATIONS / durationSec) + " orders/sec");
        System.out.println("Average Latency: " + (durationNs / ITERATIONS) + " ns");
        System.out.println("------------------------------------------------");
        System.out.println("LATENCY DISTRIBUTION (p99):");
        System.out.println("Median (p50): " + p50 + " ns");
        System.out.println("99%    (p99): " + p99 + " ns");
        System.out.println("Max          : " + max + " ns");
        System.out.println("------------------------------------------------");
    }
}