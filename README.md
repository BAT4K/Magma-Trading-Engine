# Magma: Ultra-Low Latency Trading Engine

Magma is a high-performance, lock-free financial matching engine written in Core Java. It achieves **sub-microsecond latency** (130ns median) on commodity hardware by utilizing the **LMAX Disruptor** pattern and **GC-free** memory management.

## 🚀 Performance Benchmarks
Running on AMD Ryzen 7530U (Single Thread Pinned):
- **Throughput:** ~1.2 Million Orders / Second
- **Median Latency (p50):** 130 nanoseconds
- **Tail Latency (p99):** 3.5 microseconds

## 🏗 Architecture
The engine is built on a **Single-Writer / Single-Reader** architecture to eliminate lock contention.

1.  **Gateway:** NIO TCP Server (Selector-based) handles concurrent client connections.
2.  **Ring Buffer:** Custom implementation of a circular buffer (power-of-two size) for O(1) slot claiming.
3.  **Matching Engine:** Price-Time Priority (FIFO) Limit Order Book using `TreeMap` for price levels and `LinkedList` for time priority.
4.  **Object Pool:** Pre-allocated memory for 1M+ orders to prevent Garbage Collection (Stop-the-World) pauses.

## 🛠 Tech Stack
- **Language:** Java 21 (Core)
- **Concurrency:** `Unsafe`, `AtomicLong`, `Volatile` memory semantics
- **Networking:** `java.nio` (Non-blocking I/O)
- **Data Structures:** Custom RingBuffer, IntObjectHashMap (Optimization target)

## 💻 How to Run
### 1. Start the Server
Run `Main.java` to start the matching engine and TCP gateway.
```bash
# Output
Gateway Server listening on port 9090