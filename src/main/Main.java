package main;

import main.communicate.Communicate;
import main.member.Member;
import main.votingServer.VotingServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Initialize the vote server
        VotingServer votingServer = new VotingServer();

        // Initialize communicate
        Communicate serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        // Create some members with unique IDs
        List<Member> members = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();
        int basePort = 6000;
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            ports.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            members.add(member);
        }

        // Attach members to the vote server
        votingServer.setMembers(members, ports);

        // Start Paxos algorithm by sending Prepare request from one of the members
        members.get(0).sendPrepareRequest();
    }
}
