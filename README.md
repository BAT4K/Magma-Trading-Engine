# NioKvStore: High-Performance Distributed Key-Value Store

**NioKvStore** is a lightweight, distributed, in-memory key-value database built from scratch in Java. Inspired by Redis, it uses **Non-blocking I/O (Java NIO)** to handle high concurrency on a single thread.

It features **Master-Slave Replication** for scalability, **AOF Persistence** for durability, and achieves **over 100,000 Requests Per Second (RPS)** on standard hardware.

---

## 🚀 Key Features

* **Non-Blocking I/O Architecture:** Uses `java.nio.channels.Selector` (Reactor Pattern) to manage thousands of concurrent connections efficiently without the overhead of thread-per-client models.
* **High Performance:** Benchmarked at **101,399 RPS** (Requests Per Second) using buffered I/O batching and pipelined network reads.
* **Distributed Replication:** Supports **Master-Slave** architecture. Writes to the Master are asynchronously propagated to Slaves for read scaling and redundancy.
* **Persistence (AOF):** Implements **Append-Only File** logging with configurable fsync strategies to ensure data durability across restarts.
* **TTL & Expiration:** Supports temporary keys via `EXPIRE` command. Uses a hybrid **Lazy Expiration** (on access) and **Active Expiration** (probabilistic background sampling) strategy to manage memory.
* **Redis-Compatible Protocol:** Uses a simplified text-based protocol similar to RESP (Redis Serialization Protocol), making it compatible with basic telnet/netcat clients.

---

## 🛠️ Architecture

The server runs on a **Single-Threaded Event Loop**:
1.  **Selector:** Monitors socket channels for `OP_ACCEPT` (new connections) and `OP_READ` (incoming data).
2.  **Command Processor:** Parses the raw byte stream into commands (`SET`, `GET`, etc.).
3.  **In-Memory Data Structure:** Uses `ConcurrentHashMap` to store data, ensuring O(1) access time.
4.  **Persistence Layer:** Writes commands to `magma.aof`. To optimize disk I/O, writes are buffered in memory (64KB chunks) and flushed asynchronously.

---

## ⚡ Performance Benchmarks

**Environment:** Fedora Linux, Ryzen 7 5700U, OpenJDK 21.

| Metric | Result |
| :--- | :--- |
| **Concurrency** | 50 Threads |
| **Total Requests** | 500,000 |
| **Time Taken** | 4.93 seconds |
| **Throughput** | **101,399.31 req/sec** |

*Optimized using buffered output streams and batching system calls to reduce kernel context switching.*

---

## 💻 Getting Started

### Prerequisites
* Java Development Kit (JDK) 17 or higher.
* Maven or IntelliJ IDEA (optional, for building).

### Installation
1.  Clone the repository:
    ```bash
    git clone [https://github.com/yourusername/NioKvStore.git](https://github.com/yourusername/NioKvStore.git)
    cd NioKvStore
    ```
2.  Compile the source code:
    ```bash
    javac -d out src/*.java
    ```

### Usage

#### 1. Start the Master Server
By default, the server listens on port 6379.
```bash
java -cp out KvServer -port 6379