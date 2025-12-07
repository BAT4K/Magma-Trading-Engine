package com.trading.network;

import com.trading.model.LimitOrder;
import com.trading.model.Side;
import com.trading.engine.TradingEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GatewayServer implements Runnable {
    private final int port;
    private final TradingEngine engine;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(256); // Reusable buffer

    public GatewayServer(int port, TradingEngine engine) {
        this.port = port;
        this.engine = engine;
    }

    @Override
    public void run() {
        try {
            // 1. Open Selector and Server Channel
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false); // NON-BLOCKING MODE

            // Register for incoming connections (ACCEPT)
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Gateway Server listening on port " + port);

            while (true) {
                // 2. Block until an event happens (Connect or Read)
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove(); // Must remove explicitly!

                    if (key.isAcceptable()) {
                        handleAccept();
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New Client Connected: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();

        int bytesRead = client.read(buffer);
        if (bytesRead == -1) {
            client.close(); // Connection closed
            return;
        }

        // Flip buffer to read mode
        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);

        // Simple parsing: "BUY 100 50" (Side Price Qty)
        String command = new String(data).trim();
        processCommand(client, command);
    }

    private void processCommand(SocketChannel client, String command) {
        try {
            System.out.println("Gateway Received: " + command);

            String[] parts = command.split(" ");
            if (parts.length != 3) return;

            Side side = parts[0].equalsIgnoreCase("BUY") ? Side.BUY : Side.SELL;
            double price = Double.parseDouble(parts[1]);
            int qty = Integer.parseInt(parts[2]);

            // 1. Submit to Engine
            LimitOrder order = engine.ringBuffer.next();
            order.set("NET-ORD", side, price, qty);

            // 2. Send ACK back to Client
            String response = "OK: " + side + " " + qty + " @ " + price + "\n";
            ByteBuffer respBuffer = ByteBuffer.wrap(response.getBytes());

            while(respBuffer.hasRemaining()) {
                client.write(respBuffer);
            }

        } catch (Exception e) {
            try {
                client.write(ByteBuffer.wrap(("ERR: " + e.getMessage() + "\n").getBytes()));
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
}