package main.member;

import main.communicate.Communicate;
import main.votingServer.VotingServer;

public class Member {

    private final String memberId; // Unique ID for this member.
    private final Communicate communicate; // Handles communication for the member.
    private String latestProposalId = null; // Tracks the highest proposal number seen.
    private int count = 0; // Counts proposals generated by this member.
    private AcceptedProposal acceptedProposalPair; // Stores the accepted proposal.
    private final VotingServer votingServer; // Reference to the vote server.
    private final String proposalValue; // Default proposal value (set to memberId).
    private int delayTime; // Delay time for simulating network delays.
    private boolean isForcedOffline; // Whether the member is forced offline.
    private boolean isRandomOffline; // Whether the member can go offline randomly.

    // constructor: Sets up the member with default delay times based on ID.
    public Member(String memberId, Communicate communicate, VotingServer votingServer) {
        this.memberId = memberId;
        this.communicate = communicate;
        this.votingServer = votingServer;
        this.proposalValue = memberId;

        // assign delay time based on member ID.
        switch (Integer.parseInt(memberId)) {
            case 1:
                this.delayTime = 0; // No delay for member 1.
                break;
            case 2:
                this.delayTime = 10000; // 10 second delay for member 2.
                break;
            case 3:
                this.delayTime = (int) (Math.random() * 5000) + 4000; // Random 4-9 seconds for member 3.
                break;
            default:
                this.delayTime = (int) (Math.random() * 5000) + 4000; // Default random delay.
                break;
        }
    }

    // Getters for member attributes.
    public String getMemberId() {
        return this.memberId;
    }

    public Communicate getCommunicate() {
        return this.communicate;
    }

    public String getMemberProposalValue() {
        return this.proposalValue;
    }

    public AcceptedProposal getAcceptedProposal() {
        return this.acceptedProposalPair;
    }

    // Generate a new proposal number unique to each member
    public String generateProposalNumber() {
        count++;
        return memberId + ":" + count;
    }

    // Update the highest proposal number seen.
    public void setHighestSeenProposalNumber(String proposalNumber) {
        this.latestProposalId = proposalNumber;
    }

    public String getHighestSeenProposalNumber() {
        return this.latestProposalId;
    }

    // Set the accepted proposal if it's better than the current one.
    public void setAcceptedProposal(String currentProposalNumber, AcceptedProposal acceptedProposalPair) {
        if (this.acceptedProposalPair == null
                || votingServer.compareProposalNumbers(acceptedProposalPair.getProposalNumber(), currentProposalNumber)) {
            this.acceptedProposalPair = acceptedProposalPair;
        }
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    // Check if the member should go offline.
    private boolean goOffline() {
        if (isForcedOffline) {
            return true; // Always offline if forced.

        }
        if (isRandomOffline && Integer.parseInt(this.memberId) == 3) {
            // Random chance for member 3 to go offline.
            return (Math.random() * 100 <= 5);
        }
        return false; // Default to online.
    }

    // Force the member offline.
    public void forceOffline() {
        this.isForcedOffline = true;
    }

    // Enable random offline behavior.
    public void turnOnRandomOffline() {
        this.isRandomOffline = true;
    }

    public int getDelayTime() {
        return this.delayTime;
    }

    // Send a PREPARE request to all members.
    // Send a PREPARE request to all members with improved logging and error handling.
    public void sendPrepareRequest() {
        if (goOffline()) {
            System.out.println("Member " + memberId + " is offline. Cannot send PREPARE request.");
            return; // Skip if offline.
        }

        try {
            Thread.sleep(delayTime); // Simulate network delay.
        } catch (InterruptedException e) {
            System.err.println("Error during delay simulation for member " + memberId);
            Thread.currentThread().interrupt(); // Restore interrupted status.
            return;
        }

        String proposalNumber = generateProposalNumber();
        String prepareMessage = "PREPARE " + proposalNumber;

        try {
            votingServer.broadcast(prepareMessage); // Broadcast the PREPARE message to all members.
            System.out.println("Member " + memberId + " sent PREPARE request: " + prepareMessage);
        } catch (Exception e) {
            System.err.println("Failed to broadcast PREPARE request from member " + memberId);
            e.printStackTrace();
        }
    }

    // Send a PROMISE message to a proposer.
    public void sendPromise(String proposerId, String proposalNumber, AcceptedProposal acceptedProposalPair) {
        if (goOffline()) {
            return; // Skip if offline.

        }
        try {
            Thread.sleep(delayTime); // Simulate delay.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (acceptedProposalPair == null) {
            communicate.sendMessage(proposerId, "PROMISE " + proposalNumber);
        } else {
            communicate.sendMessage(proposerId, "PROMISE " + proposalNumber + " "
                    + acceptedProposalPair.getProposalNumber() + " " + acceptedProposalPair.getProposalValue());
        }
    }

    // Send an ACCEPT request to all members.
    public synchronized void sendAcceptRequest(String proposalNumber, String proposalValue, String currentMemberId) {
        if (goOffline()) {
            return; // Skip if offline.

        }
        try {
            Thread.sleep(delayTime); // Simulate delay.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (proposalValue == null) {
            proposalValue = currentMemberId; // Default value.

        }
        votingServer.broadcast("ACCEPT " + proposalNumber + " " + proposalValue);
    }

    // Send an ACCEPTED message to a proposer.
    public void sendAccepted(String proposerId, String proposalNumber, String comingAcceptedValue) {
        if (goOffline()) {
            return; // Skip if offline.

        }
        try {
            Thread.sleep(delayTime); // Simulate delay.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communicate.sendMessage(proposerId, "ACCEPTED " + proposalNumber + " " + comingAcceptedValue);
    }

    // Send a REJECT message.
    public void sendReject(String proposalNumber) {
        if (goOffline()) {
            return; // Skip if offline.

        }
        try {
            Thread.sleep(delayTime); // Simulate delay.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communicate.sendMessage(memberId, "REJECT " + proposalNumber);
    }

    // Broadcast the RESULT of a vote.
    public void sendResult(String proposalNumber, String proposalValue) {
        if (goOffline()) {
            return; // Skip if offline.

        }
        try {
            Thread.sleep(delayTime); // Simulate delay.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        votingServer.broadcast("RESULT " + proposalNumber + " " + proposalValue);
    }
}
