Got it! Let me break down all 4 labs clearly and concisely.

---

## Lab DA-1: RPC Framework

**Core Concept:** Client calls a function that runs on a *remote* server, as if it were local. Three layers: Client → Marshalling (serialize/deserialize + type check) → Server.

**What to say in analysis:**
- Marshalling converts Python objects to JSON for network transfer
- `validate_types()` catches bad data *before* business logic runs
- Separation of concerns: validation in marshalling layer, not in the function itself

**Bare minimum code (3 files):**

`server.py`
```python
import socket, json
from marshalling import unmarshal

def calculate_grade_average(profile):
    return sum(profile['grades']) / len(profile['grades'])

server = socket.socket()
server.bind(('localhost', 9999))
server.listen(1)
print("Server listening...")

while True:
    conn, _ = server.accept()
    data = conn.recv(4096).decode()
    try:
        profile = unmarshal(data)
        result = calculate_grade_average(profile)
        conn.send(json.dumps({"status": "success", "result": result}).encode())
    except TypeError as e:
        conn.send(json.dumps({"status": "error", "error": str(e)}).encode())
    conn.close()
```

`marshalling.py`
```python
import json

def validate_types(data):
    if not isinstance(data['name'], str):
        raise TypeError("'name' must be str")
    if not isinstance(data['id'], int):
        raise TypeError("'id' must be int")
    if not isinstance(data['grades'], list):
        raise TypeError("'grades' must be list")
    for i, g in enumerate(data['grades']):
        if not isinstance(g, int):
            raise TypeError(f"grade[{i}] must be int, got {type(g).__name__}")

def unmarshal(raw):
    data = json.loads(raw)
    validate_types(data)
    return data

def marshal(obj):
    return json.dumps(obj)
```

`client.py`
```python
import socket, json

def call_rpc(name, student_id, grades):
    payload = json.dumps({"name": name, "id": student_id, "grades": grades})
    s = socket.socket()
    s.connect(('localhost', 9999))
    s.send(payload.encode())
    response = json.loads(s.recv(4096).decode())
    s.close()
    print(response)

# Test 1: valid
call_rpc("Alice", 123, [85, 90, 78])

# Test 2: invalid id (string instead of int)
call_rpc("Bob", "456", [75, 80])

# Test 3: invalid grade (string in list)
call_rpc("Charlie", 789, [90, "95", 88])
```

**Run:** Open two terminals → `python server.py` then `python client.py`

**Expected output:** Test 1 returns average, Tests 2 & 3 return TypeError messages.

---

## Lab DA-2: Lamport's Mutual Exclusion

**Core Concept:** N processes share one Critical Section (CS). No central coordinator. They use message timestamps to decide who goes first.

**3 Phases (must know):**
1. **REQUEST** — process increments clock, broadcasts REQUEST(timestamp, pid) to all, adds itself to priority queue
2. **WAIT** — blocks until: (a) its request is at the *front* of the queue AND (b) it has a REPLY from every other process
3. **RELEASE** — exits CS, broadcasts RELEASE, peers remove it from their queues

**Lamport Clock Rule:**
- On send: `clock = clock + 1`
- On receive: `clock = max(local, received) + 1`

**Message complexity:** `3(N-1)` per CS entry — N-1 REQUESTs + N-1 REPLYs + N-1 RELEASEs

**Safety property:** At most one process in CS at a time (verify by checking no time intervals overlap in Gantt chart)

**Bare minimum code:**
```python
import simpy, random

class Process:
    def __init__(self, env, pid, n, mailboxes):
        self.env = env
        self.pid = pid
        self.n = n
        self.clock = 0
        self.queue = []
        self.replies = 0
        self.mailboxes = mailboxes
        env.process(self.run())

    def tick(self, received=None):
        if received:
            self.clock = max(self.clock, received) + 1
        else:
            self.clock += 1

    def run(self):
        while True:
            yield self.env.timeout(random.uniform(1, 3))
            # REQUEST
            self.tick()
            ts = self.clock
            self.queue.append((ts, self.pid))
            self.queue.sort()
            self.replies = 0
            for j in range(self.n):
                if j != self.pid:
                    self.mailboxes[j].put(('REQUEST', ts, self.pid))
            # WAIT
            while not (self.queue[0] == (ts, self.pid) and self.replies == self.n - 1):
                msg = yield self.mailboxes[self.pid].get()
                self.handle(msg, ts)
            # ENTER CS
            print(f"t={self.env.now:.1f} P{self.pid} ENTERS CS (clock={self.clock})")
            yield self.env.timeout(random.uniform(0.5, 1.5))
            print(f"t={self.env.now:.1f} P{self.pid} EXITS CS")
            # RELEASE
            self.queue = [x for x in self.queue if x != (ts, self.pid)]
            self.tick()
            for j in range(self.n):
                if j != self.pid:
                    self.mailboxes[j].put(('RELEASE', ts, self.pid))

    def handle(self, msg, my_ts):
        kind, ts, sender = msg
        self.tick(ts)
        if kind == 'REQUEST':
            self.queue.append((ts, sender))
            self.queue.sort()
            self.mailboxes[sender].put(('REPLY', self.clock, self.pid))
        elif kind == 'REPLY':
            self.replies += 1
        elif kind == 'RELEASE':
            self.queue = [x for x in self.queue if x[1] != sender]

N = 3
env = simpy.Environment()
boxes = [simpy.Store(env) for _ in range(N)]
procs = [Process(env, i, N, boxes) for i in range(N)]
env.run(until=20)
```

**Run:** `pip install simpy` then `python lamport.py`

---

## Lab DA-3: Distributed Deadlock Detection (Wait-For Graph)

**Core Concept:** Each site tracks which processes are waiting for which others in a local Wait-For Graph (WFG). A deadlock = a *cycle* in the WFG. A probe message chases edges across sites to detect cycles.

**Probe-based algorithm (edge-chasing):**
- When P_i waits for P_j, it sends a probe `(initiator, sender, receiver)`
- Each process that receives a probe forwards it along its own outgoing wait edges
- If the probe returns to the *initiator* → deadlock detected

**Bare minimum code:**
```python
import simpy, random

# Wait-For Graph: who is each process waiting for?
# Example: 0 waits for 1, 1 waits for 2, 2 waits for 0 (deadlock!)
WFG = {0: [1], 1: [2], 2: [0], 3: []}

deadlocks_found = set()

class Site:
    def __init__(self, env, pid, mailboxes):
        self.env = env
        self.pid = pid
        self.mailboxes = mailboxes
        self.initiated = False
        env.process(self.run())

    def run(self):
        yield self.env.timeout(self.pid * 0.5)
        # Initiate probe if waiting
        if WFG[self.pid]:
            for blocked_on in WFG[self.pid]:
                probe = (self.pid, self.pid, blocked_on)
                print(f"t={self.env.now:.1f} P{self.pid} sends probe {probe}")
                self.mailboxes[blocked_on].put(probe)

        while True:
            probe = yield self.mailboxes[self.pid].get()
            initiator, sender, receiver = probe
            print(f"t={self.env.now:.1f} P{self.pid} received probe {probe}")
            if initiator == self.pid:
                if initiator not in deadlocks_found:
                    deadlocks_found.add(initiator)
                    print(f"*** DEADLOCK DETECTED involving P{initiator} ***")
            else:
                for next_proc in WFG[self.pid]:
                    new_probe = (initiator, self.pid, next_proc)
                    self.mailboxes[next_proc].put(new_probe)

N = 4
env = simpy.Environment()
boxes = [simpy.Store(env) for _ in range(N)]
sites = [Site(env, i, boxes) for i in range(N)]
env.run(until=10)
print("Deadlocks found:", deadlocks_found if deadlocks_found else "None")
```

---

## Lab DA-4: Chord P2P Lookup Protocol

**Core Concept:** N nodes arranged in a *ring* (0 to 2^m - 1). Each node stores a **finger table** of m entries pointing to nodes `node + 2^i` positions ahead. Key lookup takes **O(log N)** hops instead of O(N).

**Key rules:**
- A key is stored at the first node whose ID ≥ key (its "successor")
- Finger table entry i points to: `successor(node_id + 2^i) mod 2^m`
- Lookup: forward to the largest finger that doesn't overshoot the key, repeat

**Bare minimum code:**
```python
import simpy

M = 4          # bits → ring size 2^4 = 16
RING_SIZE = 2 ** M
NODES = sorted([0, 3, 6, 10, 13])   # node IDs on ring

def successor(key):
    """Find which node is responsible for this key."""
    for n in NODES:
        if n >= key % RING_SIZE:
            return n
    return NODES[0]  # wrap around

def build_finger_table(node):
    return {i: successor(node + 2**i) for i in range(M)}

def lookup(start_node, key, log=True):
    hops = 0
    node = start_node
    finger = build_finger_table(node)
    if log:
        print(f"Looking up key {key} starting from node {start_node}")
    while successor(key) != node:
        target = successor(key)
        # Find best finger: largest finger <= target
        best = node
        for i in range(M):
            f = finger[i]
            if node < f <= target or (target < node and (f > node or f <= target)):
                best = f
        if best == node:
            break
        node = best
        finger = build_finger_table(node)
        hops += 1
        if log:
            print(f"  hop {hops}: forwarded to node {node}")
    responsible = successor(key)
    if log:
        print(f"  Key {key} found at node {responsible} in {hops} hops")
    return hops

# Print finger tables
for n in NODES:
    ft = build_finger_table(n)
    print(f"Node {n} finger table: {ft}")

print()
# Test lookups
for key in [5, 9, 12, 15, 1]:
    lookup(NODES[0], key)
    print()
```

---

## Quick Exam Cheat Sheet

| Lab | Algorithm | Key Property | Complexity |
|---|---|---|---|
| DA-1 | RPC + marshalling | Type safety via validate_types | O(1) per call |
| DA-2 | Lamport Mutex | Safety: 1 process in CS at a time | 3(N-1) msgs |
| DA-3 | WFG + probes | Deadlock = cycle in graph | O(N) probes |
| DA-4 | Chord DHT | Lookup via finger table | O(log N) hops |

**For analysis section** on any lab, always mention: what the algorithm guarantees, what could go wrong without it, and how it scales with N. That gets you the 15 analysis marks.

Good luck on your exam! 🎯

## Analysis Sections

---

### DA-1: RPC Framework — Analysis

**What the algorithm guarantees:**
- Type safety is enforced *before* business logic executes — invalid data never reaches `calculate_grade_average()`
- Clear separation of concerns: marshalling layer handles serialization + validation, server handles computation
- Descriptive error messages pinpoint exactly which field failed and why

**What could go wrong without it:**
- A string `"95"` passed as a grade would cause a silent wrong result or a cryptic crash deep in the calculation
- Without marshalling, you'd need to validate inside every function — messy and error-prone
- No type enforcement = brittle system that breaks unpredictably in production

**How it scales:**
- Each RPC call is independent, so the framework scales horizontally — run multiple server instances
- Marshalling overhead is O(n) where n = size of the payload (grades list length)
- The validate_types check is O(k) where k = number of grades — negligible

---

### DA-2: Lamport's Mutual Exclusion — Analysis

**What the algorithm guarantees:**
- **Safety:** At most one process in CS at any time — proved because a process only enters when its request has the *globally* lowest timestamp AND all peers have replied
- **Liveness:** Every request is eventually granted — no starvation, because the priority queue is FIFO by timestamp
- **Fairness:** Requests served in Lamport clock order — older requests always win ties broken by PID

**What could go wrong without it:**
- Without logical clocks, two processes with no common time reference could enter the CS simultaneously → data corruption
- Without the REPLY mechanism, a process couldn't know if a later request had already arrived at a peer before its own REQUEST

**How it scales:**
- Message complexity is `3(N-1)` per CS entry — grows *linearly* with N
- At N=10, each CS entry costs 27 messages — becomes expensive in large systems
- This is why Ricart-Agrawala (an optimization) reduces it to `2(N-1)` by piggybacking REPLY on RELEASE

---

### DA-3: Deadlock Detection — Analysis

**What the algorithm guarantees:**
- **Completeness:** Every deadlock is eventually detected — probes propagate along all wait-for edges
- **Correctness:** A deadlock is reported only when a probe returns to its initiator — no false positives
- **Distributed:** No central coordinator needed — each site runs the algorithm locally

**What could go wrong without it:**
- Deadlocked processes wait forever, holding resources and blocking other processes
- In a distributed system you can't just "look at the WFG" globally — there's no shared memory
- Without probe-based detection, you'd need a coordinator (single point of failure) or timeouts (imprecise)

**How it scales:**
- Probe message complexity: O(N²) in the worst case (every process sends probes to every other)
- Detection latency = number of hops in the deadlock cycle × message delay
- False deadlock detection can happen if processes abort between probe send and receive — need careful timestamping in production

---

### DA-4: Chord Lookup — Analysis

**What the algorithm guarantees:**
- **O(log N) lookup hops** — finger table halves the remaining search space each hop (like binary search on a ring)
- **Load balancing** — keys are distributed uniformly across nodes using consistent hashing
- **Fault tolerance** — with successor lists maintained, lookup still works when nodes fail

**What could go wrong without it:**
- Naive ring traversal (no finger table) = O(N) hops — unusable at scale
- Without consistent hashing, adding/removing one node would require remapping all keys
- Stale finger tables (after node joins/leaves) cause incorrect routing — stabilization protocol must run periodically

**How it scales:**

| Nodes (N) | Finger table size (m = log₂N) | Max lookup hops |
|---|---|---|
| 16 | 4 | 4 |
| 256 | 8 | 8 |
| 1024 | 10 | 10 |
| 1,000,000 | 20 | 20 |

This logarithmic scaling is why Chord is used in real P2P systems like BitTorrent.

---

## Visualization — Yes, you can! Here's what works with bare minimum code:

**DA-2 (Lamport)** — the SimPy simulation already prints an event log. You can paste that output into a **Gantt chart** manually, or pipe it into matplotlib:

```python
# Add this after env.run() in lamport.py
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

# Collect CS intervals during simulation - add to Process.run():
# cs_log.append((self.pid, env.now, 'enter'))  before yield timeout
# cs_log.append((self.pid, env.now, 'exit'))   after yield timeout

# Then plot:
fig, ax = plt.subplots(figsize=(10, 3))
colors = ['#4f46e5', '#e11d48', '#059669']
for pid, start, end in cs_intervals:   # build from cs_log pairs
    ax.barh(f"P{pid}", end - start, left=start, color=colors[pid], height=0.4)
ax.set_xlabel("Simulation Time")
ax.set_title("Critical Section Gantt Chart — no overlaps = safety verified")
plt.tight_layout()
plt.savefig("gantt.png")
plt.show()
```

**DA-4 (Chord)** — easiest to visualize, just matplotlib circle + finger table arrows:

```python
import matplotlib.pyplot as plt
import numpy as np

RING_SIZE = 16
NODES = [0, 3, 6, 10, 13]
M = 4

def successor(key):
    for n in NODES:
        if n >= key % RING_SIZE:
            return n
    return NODES[0]

fig, ax = plt.subplots(figsize=(7, 7))
ax.set_aspect('equal')
ax.axis('off')

# Draw ring
theta = np.linspace(0, 2 * np.pi, 300)
ax.plot(np.cos(theta), np.sin(theta), 'lightgray', lw=1.5)

# Draw nodes
def pos(node_id):
    angle = 2 * np.pi * node_id / RING_SIZE - np.pi / 2
    return np.cos(angle), np.sin(angle)

for n in NODES:
    x, y = pos(n)
    ax.plot(x, y, 'o', color='#4f46e5', ms=18, zorder=3)
    ax.text(x * 1.15, y * 1.15, str(n), ha='center', va='center',
            fontsize=11, fontweight='bold', color='#4f46e5')

# Draw finger table arrows for Node 0
finger_node = 0
ft = {i: successor(finger_node + 2**i) for i in range(M)}
colors = ['#e11d48', '#f97316', '#059669', '#0284c7']
for i, target in ft.items():
    x1, y1 = pos(finger_node)
    x2, y2 = pos(target)
    ax.annotate("", xy=(x2 * 0.9, y2 * 0.9), xytext=(x1 * 0.9, y1 * 0.9),
                arrowprops=dict(arrowstyle='->', color=colors[i], lw=1.5))
    mx, my = (x1 + x2) / 2 * 0.75, (y1 + y2) / 2 * 0.75
    ax.text(mx, my, f"2^{i}={2**i}", fontsize=8, color=colors[i])

ax.set_title(f"Chord Ring — finger table of Node {finger_node}", fontsize=13)
plt.tight_layout()
plt.savefig("chord_ring.png")
plt.show()
```

**Run:** `pip install matplotlib numpy simpy` and run either script directly. Both save a PNG too — perfect to paste into your submission.

**DA-3 (WFG)** is also easy to visualize — just draw the directed graph with `networkx`:

```python
import networkx as nx
import matplotlib.pyplot as plt

WFG = {0: [1], 1: [2], 2: [0], 3: []}  # 0→1→2→0 is the cycle

G = nx.DiGraph()
for src, targets in WFG.items():
    for tgt in targets:
        G.add_edge(f"P{src}", f"P{tgt}")

# Detect cycles
cycles = list(nx.simple_cycles(G))

pos = nx.circular_layout(G)
colors = ['#e11d48' if any(f"P{n}"[1:] in [str(x) for x in c] for c in cycles)
          else '#4f46e5' for n in WFG]
nx.draw(G, pos, with_labels=True, node_color=colors,
        node_size=1500, font_color='white', font_weight='bold',
        arrows=True, arrowsize=25, edge_color='gray')
plt.title(f"Wait-For Graph — Cycles (deadlocks): {cycles}")
plt.savefig("wfg.png")
plt.show()
```

**Install:** `pip install networkx matplotlib`

The red nodes are deadlocked, blue are fine. This alone covers your Output/Viz 15 marks very nicely.
