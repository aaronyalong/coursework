# Virtual Memory Manager

A Java simulation of a segmented, paged virtual memory system that translates virtual addresses to physical addresses, **with and without demand paging**.

## Memory model

| Component | Size |
|---|---|
| Physical memory | 1024 frames × 512 words (524,288 integers) |
| Disk (demand paging) | 1024 blocks × 512 words |
| Virtual address | Split into segment `s`, page `p` and offset `w` |

- The **segment table** occupies frame 0. Each entry holds the segment's length and the frame (or disk block) of its page table.
- **Page tables** map pages to frames.
- A **negative** entry means the page table or page is on disk. With demand paging, it's loaded into a free frame on first access.

## How it works

1. **Initialization** reads two lines from an init file: triples `s z f` that fill the segment table, then triples `s p f` that fill the page tables.
2. **Commands** are then read from an input file:

| Command | Action |
|---|---|
| `TA <va>` | Translate a virtual address and print the physical address, or `-1` if it is outside the segment |
| `RP <pa>` | Read and print the contents of a physical address |
| `NL` | Start a new output line |

## Run

From the project folder:

```bash
javac -d out src/VirtualMemory.java
java -cp out VirtualMemory
```

This reads `init-dp.txt` / `input-dp.txt` (demand paging) and `init-no-dp.txt` / `input-no-dp.txt` (no demand paging), and writes `output-dp.txt` and `output-no-dp.txt`. Edit the input files to try your own address sequences.

## Files

| File | Purpose |
|---|---|
| `src/VirtualMemory.java` | Initialization, address translation, page-fault handling, command processing |
| `init-*.txt`, `input-*.txt` | Test configurations and command sequences |
| `output-*.txt` | Expected output for the included tests |
