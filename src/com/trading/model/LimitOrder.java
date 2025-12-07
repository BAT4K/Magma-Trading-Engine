package com.trading.model;

public class LimitOrder {
    // Fields are not final so we can reuse the object
    public String id;
    public Side side;
    public double price;
    public int quantity;
    public long timestamp;

    // Coordination flag for the Ring Buffer
    // volatile ensures memory visibility across threads without locks
    public volatile boolean ready = false;

    public LimitOrder() { }

    // Fast setter
    public void set(String id, Side side, double price, int quantity) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
        this.ready = true; // Mark as ready to be read
    }

    // Reset method for the consumer
    public void clear() {
        this.ready = false;
    }
}