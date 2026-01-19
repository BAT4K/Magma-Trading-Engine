# Magma — Ultra-Low Latency Trading Engine

**Magma** is a high-performance, lock-free financial matching engine written in **Core Java**.  
It achieves **sub-microsecond latency** (**130ns median**) on commodity hardware by leveraging the **LMAX Disruptor pattern**, a **single-writer architecture**, and **GC-free memory management**.

This project demonstrates **low-latency systems engineering**, **high-performance concurrency**, and **deterministic execution** techniques used in real-world trading systems.

---

## Performance Benchmarks

**Environment:** AMD Ryzen 5 7530U (Single Thread, CPU Affinity Pinned)

- **Throughput:** ~1.2 million orders / second
- **Median Latency (p50):** **130 nanoseconds**
- **Tail Latency (p99):** **3.5 microseconds**

---

## Architecture

Magma is built around a **Single-Writer / Single-Reader** architecture to eliminate lock contention and minimize memory barriers.

### Core Components

1. **Gateway**
    - TCP server using `java.nio` (Selector-based)
    - Handles concurrent client connections with non-blocking I/O

2. **Ring Buffer**
    - Custom power-of-two circular buffer
    - O(1) slot claiming
    - Cache-friendly layout inspired by the LMAX Disruptor

3. **Matching Engine**
    - Price-Time Priority (FIFO) limit order book
    - `TreeMap` for price levels
    - `LinkedList` for strict time ordering

4. **Object Pool**
    - Pre-allocated pool for **1M+ orders**
    - Zero allocations on the hot path
    - Eliminates GC pauses

---

## Tech Stack

- **Language:** Java 21 (Core Java)
- **Concurrency:** `Unsafe`, `AtomicLong`, `volatile` semantics
- **Networking:** `java.nio` (Non-blocking I/O)
- **Data Structures:** Custom RingBuffer, `TreeMap`, `LinkedList`

---

## How to Run

### 1. Start the Server

Run `Main.java` to start the matching engine and TCP gateway.

```bash
# Example output
Gateway Server listening on port 9090
```

---

### 2. Connect via Telnet

```bash
telnet localhost 9090
```

Example session:

```text
> BUY 100 10
> SELL 100 10
< OK: SELL 10 @ 100.0
```

---

### 3. Run Benchmarks

Run `BenchmarkMain.java` to:
- Flood the engine with **1 million orders**
- Generate throughput and latency statistics
- Produce a latency histogram (p50 / p99)

---

## Project Structure

```text
src/
├── gateway/        # NIO TCP server and protocol parsing
├── engine/         # Matching engine core
├── orderbook/      # Limit order book implementation
├── ringbuffer/     # Lock-free ring buffer
├── pool/           # Object pooling & memory reuse
├── benchmark/      # Load & latency benchmarks
└── Main.java       # Entry point
```

---

## Design Goals

- Zero locks on the hot path
- Single-writer determinism
- Cache-friendly memory layout
- Predictable latency under load
- No GC activity during trading

---

## Git Setup & Push Instructions

```bash
git init
git add .
git commit -m "Initial commit: ultra-low latency matching engine"
git branch -M main
git remote add origin https://github.com/BAT4K/Magma-Trading-Engine.git
git push -u origin main
```

---

## Author

**Hans James**

---

## License

This project is released under the **MIT License**.
