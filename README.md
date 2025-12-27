# Traffic Management Simulation 

## Title
- **Assignment :** 1 
- **Name:** Nabin Pandey
- **Roll Number:** 48
- **Course/Section:** COMP202 / CS-I
- **Submitted To:** Rupak Ghimire
- **Date:** 2025-12-27

---

## Summary of Work
This project is a **4-way intersection traffic simulation** built using **Java + JavaFX**. It visualizes queued vehicles on Roads **A, B, C, D**, controls traffic lights, and releases/animates vehicles through the junction based on a scheduling policy.

**Work performed :**
- Implemented/validated JavaFX traffic-light state updates (RED/YELLOW/GREEN) in a thread-safe way.
- Implemented and documented a scheduler-based approach where **Road A lane 2 (AL2)** can be treated as the **priority lane** during congestion.
- Integrated file polling so the simulator reads only newly appended vehicle events from lane files and updates the in-memory queues and UI.
- Implemented vehicle queue visualization and animation using JavaFX transitions.

---

## Time Complexity Analysis (Queue)

| Operation | Time Complexity | Explanation | Usage in Traffic Simulation |
|----------|----------------|-------------|-----------------------------|
| Enqueue  | O(1) | Insertion at the rear of the queue | Vehicle arrival at lane |
| Dequeue  | O(n) | Removal from the front of the queue | Vehicle passes signal |
| Peek    | O(1) | Access front element without removal | Check next vehicle |
| isEmpty | O(1) | Checks if queue has elements | Signal decision logic |
| Traverse | O(n) | Each element visited once | Debug / display |

---

## Data Structures

| Data structure | Where used | Implementation (concept) | Purpose |
|---|---|---|---|
| PriorityQueue | `TrafficScheduler` | Java `PriorityQueue<LaneEntry>` ordered by `LaneEntry.compareTo()` | Select the next road to serve based on priority ordering |
| HashMap | `TrafficSimulator` | `Map<String, Long>` mapping file name → last read offset | Read only *new* lines appended to each lane file |
| HashMap | `TrafficGenerator` | `Map<String, List<Rectangle>>` mapping `road+lane` → list of cars | Maintain per-road/per-lane waiting queues in the UI |
| ArrayList | `TrafficGenerator` | `List<Rectangle>` per lane queue | Stores queued vehicles for release + reposition |
| Comparable (ordering) | `LaneEntry` | `LaneEntry implements Comparable` | Defines ordering for the scheduler’s priority queue |
| JavaFX Timeline / Transitions | `TrafficSimulator`, `TrafficGenerator` | Timelines for polling/cycle timing; transitions for movement | Periodic updates + animation |

---

### Use of HashMap

Although Queue is the core data structure for managing vehicle order, a HashMap was used to efficiently map lane identifiers to their corresponding vehicle queues.

Queue alone does not provide fast access when multiple independent lanes are present. Using a HashMap enables constant-time (O(1)) access to the required lane queue, improving scalability and code clarity.

This separation allows each lane to maintain its own FIFO queue while enabling centralized traffic scheduling logic.

### Use of Array / List

Arrays and Lists were used to store fixed or iterable collections such as lane configurations and signal sequences.

Queue is unsuitable for indexed access or iteration without modification. Lists provide sequential access while preserving order and supporting traversal without removing elements.

---

## Functions That Use the Data Structures

### Scheduler (PriorityQueue)
- `TrafficScheduler(Collection<LaneEntry>)` — initializes the priority queue with all roads.
- `CheckandUpdatePriority(LaneEntry entry, int incomingCount, int priorityLaneCount)` — updates entry counts and reorders the queue.
- `serverAndRotateLane()` — selects a lane using the queue and returns the selected road.
- `getNextLaneToServe()` — peeks at the next lane to serve.

### LaneEntry (Comparable)
- `compareTo(LaneEntry other)` — ordering rule for the scheduler.

### Simulator (HashMap + polling)
- `resetFilePositionsToEnd()` — sets initial offsets.
- `startFilePolling()` — runs periodic reads.
- `readVehicles(file, lane, roadId)` — reads new appended lines only.
- `processTrafficCycle()` — updates scheduler, updates lights, releases vehicles.

### Generator (HashMap + List)
- `addWaitingCar(roadId, laneNumber, vehicleId)` — adds vehicle to a lane queue.
- `releaseWaitingCars(roadId, laneNumber, count)` — removes vehicles from queue and animates them.
- `reposition(roadId, laneNumber)` — repositions remaining vehicles in the queue.

---

## Algorithm Used for Processing the Traffic

### Inputs
The simulator reads vehicle events from 4 text files:
- `lanea.txt`, `laneb.txt`, `lanec.txt`, `laned.txt`

Each line follows:
- `vehicleId,laneNumber`

Only source lanes **2** and **3** are accepted as outgoing lanes (lane 1 is treated as accept/incoming).

## Algorithm Used for Traffic Processing

The traffic simulator uses the **Round Robin scheduling algorithm** to process vehicles from multiple lanes fairly.

Steps:
1. Initialize a queue for each lane.
2. Assign a time slice (green signal duration) for each lane.
3. Vehicles are dequeued from the current lane until the time slice ends.
4. Move to the next lane in circular order.
5. Repeat until all vehicles have passed.

This ensures **fair distribution of signal time** among all lanes and prevents starvation of any lane.


### Core processing loop
1. **Poll lane files** on a fixed interval and read only newly appended lines using file offsets.
2. **Enqueue vehicles** into the appropriate in-memory lane and UI waiting list.
3. **Compute counts** per road (especially Road A lane 2 priority count).
4. **Update scheduler** with counts.
5. **Select next road** (using priority queue ordering).
6. **Update traffic lights**: set all RED, selected GREEN, then YELLOW, then RED.
7. **Release vehicles** from the selected road’s queues and animate them through the junction.

### Priority behavior (Road A only)
If Road A’s **priority lane (AL2)** reaches the defined trigger threshold **n**, the scheduler can switch to priority mode and serve Road A more frequently until AL2 is reduced to the required target level (e.g., 5).

---

## Time Complexity (with explanation)

Let:
- **R** = number of roads (fixed at 4)
- **N** = number of scheduler entries (≈ 4)
- **K** = number of newly appended vehicle records read in one polling tick
- **M** = number of waiting vehicles in a particular lane queue

### Scheduler (PriorityQueue)
- `peek()` is **O(1)**
- `add()` / `poll()` are **O(log N)**
- `remove(Object)` is **O(N)** (linear search)

In `CheckandUpdatePriority`, one update per road performs:
- `remove(entry)` → O(N)
- `add(entry)` → O(log N)

Total per cycle (all roads): **O(R·(N + log N))**.
Since N and R are small constants (4), this behaves as near-constant time in practice.

### File polling
Reading new appended lines is **O(K)** per polling tick.
HashMap get/put operations are average **O(1)** assuming good key distribution.

### Queue release + reposition
Repositioning a queue of size M is **O(M)** because each remaining vehicle must be moved into its updated UI position.

---

## Source Code
- **Main JavaFX class:** `com.traffic.gui.TrafficSimulator`
- All Source code related to the project will be available in this repo. 

---

## How to Run (README Process)

### Clone Repo
```bash
git clone https://github.com/nabin-pandey/DSA_Queue_Simulator
```
and  run ###TrafficGeneratorProces.java after than

### Option A — Run with Maven (recommended)
If  `pom.xml` includes the OpenJFX dependencies and the `javafx-maven-plugin`, you can run the app with:
```bash
mvn clean javafx:run
OR error is displayed run : 
mvn clean javafx:run -X
```

This approach downloads the required JavaFX modules automatically and launches the configured `mainClass`.

### Option B — Run with JavaFX SDK (manual)
If you are not using Maven to fetch JavaFX, install the JavaFX SDK and run with:

**Linux/macOS**
```bash
export PATH_TO_FX=/path/to/javafx-sdk/lib
java --module-path $PATH_TO_FX --add-modules javafx.controls -cp target/classes com.traffic.gui.TrafficSimulator
```

**Windows (CMD)**
```bat
set PATH_TO_FX="C:\\path\\to\\javafx-sdk\\lib"
java --module-path %PATH_TO_FX% --add-modules javafx.controls -cp target\\classes com.traffic.gui.TrafficSimulator
```

### Running both programs (Generator + Simulator)
1. Start your **vehicle generator process** so it creates/appends events into:
    - `lanea.txt`, `laneb.txt`, `lanec.txt`, `laned.txt`
2. Start the **JavaFX simulator**.
3. Click **Start Simulation**.

> The simulator reads from the current end of each file at startup (so you see new vehicles after pressing Start).

---

## Demo (GIF / Video)\
Because of some issues playback video has been played from Google Drive.
- **Video:** https://drive.google.com/file/d/13bweX8KoTm0RSfD277WH3fGKjfqFbdbY/view?usp=sharing
  
<details>
  <summary><b>Demo Video (click to expand)</b></summary>

  https://github.com/user-attachments/assets/c1ce22fb-b76d-4e5e-ad3f-59bd7fd851ae

</details>




---

## References
- OpenJFX Maven guide (`mvn javafx:run` and Maven setup): https://openjfx.io/openjfx-docs/maven
- OpenJFX SDK install & CLI flags (`--module-path`, `--add-modules`): https://openjfx.io/openjfx-docs/install-javafx
- JavaFX Maven plugin usage (`javafx-maven-plugin`): https://github.com/openjfx/javafx-maven-plugin
- Java `PriorityQueue` complexity notes (Oracle Javadoc): https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/PriorityQueue.html
- Java `HashMap` performance notes (Oracle Javadoc): https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
- GeeksforGeeks – Queue Data Structure:  https://www.geeksforgeeks.org/queue-data-structure/

---

## Note
- Keep lane files in the working directory where the simulator runs.
