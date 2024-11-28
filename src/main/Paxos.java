package main;

import java.util.ArrayList;
import java.util.List;
import main.communicate.Communicate;
import main.member.Member;
import main.votingServer.VotingServer;

public class Paxos {

    public static void main(String[] args) {
        // Step 1: Create the voting server
        // The voting server acts as the hub for coordinating all Paxos-related activities
        VotingServer votingServer = new VotingServer();

        // Step 2: Set up the communication system for the voting server
        // This enables the server to send and receive messages from members
        Communicate serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        // Step 3: Initialize the members participating in the voting process
        // Members act as individual nodes in the Paxos distributed system
        List<Member> multipleMembers = new ArrayList<>();
        List<Integer> multiPorts = new ArrayList<>(); // Ports assigned for communication
        int basePort = 6000; // Starting port number for members

        // Step 4: Create members with unique IDs and assign ports
        for (int i = 1; i <= 9; i++) { // Loop to create 9 members
            String memberId = "" + i; // Generate a unique member ID
            Communicate communicate = new Communicate(votingServer); // Set up communication for this member
            Member member = new Member(memberId, communicate, votingServer); // Initialize the member
            int port = basePort + i; // Assign a unique port number to the member
            multiPorts.add(port); // Add the port to the list of ports
            member.getCommunicate().startServer(port, member.getMemberId()); // Start the member's communication server
            multipleMembers.add(member); // Add the member to the list of members
        }

        // Step 5: Attach the members to the voting server
        // The server uses this to manage and communicate with all members
        votingServer.setMembers(multipleMembers, multiPorts);

        // Step 6: Start the Paxos algorithm
        // The first member initiates the algorithm by sending a prepare request
        multipleMembers.get(0).sendPrepareRequest();

    }
}
