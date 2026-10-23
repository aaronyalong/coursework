# Process & Resource Manager

A Java simulation of an OS kernel's process and resource management: process creation and destruction, multi-unit resource allocation, and priority scheduling, driven by a command shell.

## Design

| Structure | Description |
|---|---|
| **PCB[16]** (`ProcessCB`) | Process descriptors: state, priority, parent/child links, held resources |
| **RCB[4]** (`ResourceCB`) | Multi-unit resources, each with an inventory and a waiting list |
| **Ready list** (`ReadyList`) | Multi-level priority queue; the number of levels is set at init |

Scheduling is **preemptive priority with round-robin within a level**. The highest-priority ready process runs, and `to` (timeout) moves the running process to the back of its level.

Destroying a process recursively destroys its children and releases everything they hold, which may unblock waiting processes.

## Commands

| Command | Action |
|---|---|
| `in <levels> <u0> <u1> <u2> <u3>` | Initialize with the given ready-list levels and resource inventories |
| `id` | Initialize with defaults |
| `cr <priority>` | Create a child of the running process |
| `de <pid>` | Destroy a process and all of its descendants |
| `rq <rid> <units>` | Request units of a resource (blocks if unavailable) |
| `rl <rid> <units>` | Release units of a resource |
| `to` | Timeout: move the running process to the back of its priority level |

After each command, the shell prints the ID of the currently running process, or `-1` on an error. A blank line in the input separates test sequences and starts a new output line.

## Run

From the project folder:

```bash
javac -d out src/*.java
java -cp out Shell
```

This reads commands from `input.txt` and writes the results to `output.txt`.

## Files

| File | Purpose |
|---|---|
| `src/Shell.java` | Command parser and entry point |
| `src/ProcessCB.java` | Process control block and the manager operations (create, destroy, request, release, timeout, scheduler) |
| `src/ResourceCB.java`, `src/Resource.java` | Resource descriptors and allocation records |
| `src/ReadyList.java` | Multi-level ready list |
| `input.txt` / `output.txt` | Test commands and expected output |
