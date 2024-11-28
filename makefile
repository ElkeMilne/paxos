# Assignment 3: Adelaide Suburbs Council Election - Paxos Consensus Implementation

## Overview
The Adelaide Suburbs Council is organizing elections to select its next president. Nine council members, each with distinct responsiveness and availability, are eligible for the role. The Paxos algorithm will be used to implement a distributed voting protocol that ensures fault-tolerance, even in challenging network conditions.

### Member Characteristics:
1. **Member 1 (M1):** Highly responsive and experienced in council matters.
2. **Member 2 (M2):** Interested in leadership but experiences slow communication due to geographic challenges.
3. **Member 3 (M3):** Occasionally goes offline due to personal activities but is actively campaigning.
4. **Members 4 to 9 (M4-M9):** Neutral participants who vote impartially.

The election protocol involves a proposer sending a candidate's name for the presidency. To achieve consensus, the majority of members must agree on a single candidate.

### Objective
Develop a Paxos-based distributed system where members communicate through sockets. The system must handle delays, failures, and disconnections while ensuring consensus.

---

## Paxos Algorithm Overview
Paxos is a widely used protocol for achieving agreement in distributed systems. It operates in phases to ensure consensus on a single value, even when some nodes fail.

### Key Steps
1. **Proposal Initiation:** A proposer generates a unique ID for their proposal and checks if the acceptors have seen any higher proposal IDs.
2. **Consensus Achievement:** If most acceptors agree, the proposer can finalize the value.

### Detailed Breakdown
#### Phase 1: Preparation & Promises
- **Proposer Action:** Sends a `PREPARE` message with a unique proposal ID to acceptors.
- **Acceptor Response:**
  - Ignores the proposal if the ID is not the highest it has seen.
  - Sends a `PROMISE` if the proposal ID is the highest, including details of any previously accepted value.

#### Phase 2: Proposal Acceptance
- **Proposer Action:** Sends an `ACCEPT` message with the chosen value to acceptors after receiving majority promises.
- **Acceptor Action:**
  - Accepts if the proposal ID is the highest.
  - Sends an `ACCEPTED` message to all learners to confirm consensus.

---

## Class Implementations

### `Member` Class
Represents a participant in the Paxos protocol. Each `Member` acts as a proposer, acceptor, and learner.

#### Features
- **Identity & Communication:** Uniquely identified by a `memberId` and uses the `Communicate` object for interactions.
- **Proposal Management:** Tracks and generates proposal IDs and stores the highest proposal seen.
- **Voting Logic:** Handles `PREPARE`, `PROMISE`, `ACCEPT`, `REJECT`, and `ACCEPTED` messages.
- **Delay Simulation:** Introduces response delays based on member behavior for realistic testing.
- **Offline Management:** Simulates forced or random disconnections.

---

### `VotingServer` Class
Centralized component coordinating the Paxos protocol. Manages communication and consensus logic.

#### Features
1. **Message Processing:** Handles messages like `PREPARE`, `PROMISE`, `ACCEPT`, and `ACCEPTED`.
2. **Node Management:** Keeps track of all `Member` objects and their communication ports.
3. **Timeout Handling:** Uses scheduled tasks to handle timeouts for proposals.
4. **Consensus Election:** Decides on the council president based on majority agreement.

---

## Testing Framework

### `PaxosTest`
Simulates various scenarios to ensure Paxos implementation robustness:
1. **Concurrent Proposals:** Validates the system's ability to handle competing proposals.
2. **Immediate Responses:** Ensures quick resolution when members respond without delay.
3. **Member Failure:** Tests the system's fault tolerance when a member goes offline.

### `VotingServerTest`
Verifies the functionalities of the `VotingServer` class:
- Manages members (`testSetMembers`).
- Broadcasts messages (`testBroadcast`).
- Compares proposal IDs (`testCompareProposalNumbers`).

### `MemberTest`
Validates the behavior of the `Member` class:
- Generates and tracks proposal IDs.
- Simulates delays and offline states.
- Ensures correct handling of messages like `PROMISE` and `ACCEPTED`.

---

## Running the Application

### Compile and Run
To compile the code, run:
```bash
javac -d bin src/main/**/*.java
