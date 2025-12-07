package com.trading;

import com.trading.engine.TradingEngine;
import com.trading.network.GatewayServer;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Engine (1024 slots is enough for manual testing)
        TradingEngine engine = new TradingEngine(1024);
        Thread engineThread = new Thread(engine);
        engineThread.start();

        // 2. Start TCP Gateway
        GatewayServer server = new GatewayServer(9090, engine);
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("System Ready. Connect via 'telnet localhost 9090'");
    }
}