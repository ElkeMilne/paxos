# Assignment 3: Adelaide Suburbs Council Election - Paxos Consensus Implementation

## Overview
This year, Adelaide Suburbs Council is holding elections for council president. Any member of its nine person council is eligible to become council president.

### Member Characteristics:
1. **Member 1 (M1):** Highly responsive and experienced in running for president
2. **Member 2 (M2):** Interested in becoming president, however due to his remote location will have very slow response time
3. **Member 3 (M3):** Also keen candidate to become president, can go offline because of their camping trips
4. **Members 4 to 9 (M4-M9):** Neutral participants who vote fairly

On the day of the vote, one of the councillors will send out an email/message to all councillors with a proposal for a president. A majority (half+1) is required for somebody to be elected president. The election protocol involves a proposer sending a candidate's name for the presidency. To achieve consensus, the majority of members must agree on a single candidate.

### Objective
Develop a Paxos-based distributed system where members communicate through sockets. The system must handle delays, failures, and disconnections while ensuring consensus.

---

## Paxos Algorithm Overview
Paxos is a consensus algorithm that ensures distributed systems agree on a single value or decision, even in the presence of faults or unreliable communication.

### Detailed Breakdown
## **Overview of Paxos**

### **Roles in Paxos**
- **Proposer**: Sends proposals for consensus.
- **Acceptor**: Votes on proposals and ensures agreement.
- **Learner**: Learns the decided value (not explicitly required here).

### **Phases of Paxos**
1. **Prepare Phase**:
   - The proposer sends a `Prepare` message with a unique proposal ID to all acceptors.
   - Acceptors respond with the highest proposal ID they have seen and any previously accepted value.
2. **Accept Phase**:
   - The proposer sends an `Accept` message with a value based on responses from Phase 1.
   - Acceptors respond only if the proposal ID matches or exceeds the highest seen proposal ID.
3. **Decision**:
   - If a majority (quorum) of acceptors agree, the value is finalized.

### **Fault Tolerance**
- **Majority-based Quorum**: Progress continues as long as a majority of nodes are functional.
- **Resilience to Failures**: Handles message delays, loss, and inconsistent states effectively.

---

## Testing Framework

### `PaxosTesting`
Simulates various scenarios to ensure Paxos implementation robustness:
1. **Concurrent Proposals:** Validates the system's ability to handle competing proposals.
2. **Immediate Responses:** Ensures quick resolution when members respond without delay.
3. **Member Failure:** Tests the system's fault tolerance when a member goes offline.

### `VotingServerTest`
Verifies the functionalities of the `VotingServer` class:
- Manages members (`testSetMembers`).
- Broadcasts messages (`testBroadcast`).
- Compares proposal IDs (`testCompareProposalNumbers`).

### `MemberTesting`
Validates the behavior of the `Member` class:
- Generates and tracks proposal IDs.
- Simulates delays and offline states.
- Ensures correct handling of messages like `PROMISE` and `ACCEPTED`.

---

## Running the Application

### Compile and Run
make all

### Running tests
make test

### CLean
Remove compiled class:
make clean
