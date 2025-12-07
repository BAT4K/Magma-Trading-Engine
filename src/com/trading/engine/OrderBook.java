package com.trading.engine;

import com.trading.model.LimitOrder;
import com.trading.model.Side;

import java.util.*;

public class OrderBook {
    private final TreeMap<Double, List<LimitOrder>> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Double, List<LimitOrder>> asks = new TreeMap<>();

    public void addOrder(LimitOrder incomingOrder) {
        // Step 1: Match against existing orders
        match(incomingOrder);

        // Step 2: If quantity remains, add to book
        // OPTIMIZATION: Accessing field '.quantity' directly instead of .getQuantity()
        if (incomingOrder.quantity > 0) {
            TreeMap<Double, List<LimitOrder>> bookSide =
                    (incomingOrder.side == Side.BUY) ? bids : asks;

            bookSide.computeIfAbsent(incomingOrder.price, k -> new LinkedList<>())
                    .add(incomingOrder);
        }
    }

    private void match(LimitOrder incoming) {
        TreeMap<Double, List<LimitOrder>> oppositeBook =
                (incoming.side == Side.BUY) ? asks : bids;

        while (incoming.quantity > 0 && !oppositeBook.isEmpty()) {
            Double bestOppositePrice = oppositeBook.firstKey();

            // Direct field access for price comparison
            boolean isMatch = (incoming.side == Side.BUY && bestOppositePrice <= incoming.price) ||
                    (incoming.side == Side.SELL && bestOppositePrice >= incoming.price);

            if (!isMatch) break;

            List<LimitOrder> ordersAtLevel = oppositeBook.get(bestOppositePrice);
            Iterator<LimitOrder> it = ordersAtLevel.iterator();

            while (incoming.quantity > 0 && it.hasNext()) {
                LimitOrder resting = it.next();

                // Direct field access for quantity match
                int tradeQty = Math.min(incoming.quantity, resting.quantity);

                // Execute Trade (commented out for pure benchmark speed, uncomment to see logs)
                executeTrade(incoming, resting, tradeQty, bestOppositePrice);

                // Update quantities directly
                incoming.quantity -= tradeQty;
                resting.quantity -= tradeQty;

                if (resting.quantity == 0) {
                    it.remove();
                }
            }

            if (ordersAtLevel.isEmpty()) {
                oppositeBook.remove(bestOppositePrice);
            }
        }
    }

    private void executeTrade(LimitOrder incoming, LimitOrder resting, int qty, double price) {
        System.out.println(" [TRADE] " + qty + " @ " + price);
    }
}