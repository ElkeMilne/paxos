package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import main.communicate.Communicate;
import main.member.Member;
import main.votingServer.VotingServer;

import java.util.ArrayList;
import java.util.List;

public class PaxosTest {

    private List<Member> members;
    private VotingServer votingServer;
    private List<Integer> ports;
    Communicate serverCommunicate;

    @Test
    public void testConcurrentVotingProposals() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        members = new ArrayList<>();
        ports = new ArrayList<>();
        int basePort = 9000;
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            ports.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            members.add(member);
        }

        votingServer.setMembers(members, ports);
        Thread member1Thread = new Thread(() -> members.get(0).sendPrepareRequest());
        Thread member2Thread = new Thread(() -> members.get(1).sendPrepareRequest());

        member1Thread.start();
        member2Thread.start();

        member1Thread.join();
        member2Thread.join();

        // No delay time for all members
        // sleep for 30 seconds
        Thread.sleep(30000);

        assertEquals("1", votingServer.getPresident(), "Concurrent proposal resolution failed");
    }

    @Test
    public void testMultipleConcurrentProposals() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        members = new ArrayList<>();
        ports = new ArrayList<>();
        int basePort = 7000;

        // Initialize members
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            ports.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            members.add(member);
        }

        votingServer.setMembers(members, ports);

        // Concurrently send proposals from multiple members
        Thread proposer1Thread = new Thread(() -> members.get(0).sendPrepareRequest());
        Thread proposer2Thread = new Thread(() -> members.get(1).sendPrepareRequest());
        Thread proposer3Thread = new Thread(() -> members.get(2).sendPrepareRequest());

        proposer1Thread.start();
        proposer2Thread.start();
        proposer3Thread.start();

        proposer1Thread.join();
        proposer2Thread.join();
        proposer3Thread.join();

        // Wait for resolution
        Thread.sleep(30000);

        // Assert that only one proposer achieves consensus
        String electedPresident = votingServer.getPresident();
        assertNotNull(electedPresident, "No consensus was reached for the president.");
        assertTrue(electedPresident.equals("1") || electedPresident.equals("2") || electedPresident.equals("3"),
                "Unexpected president elected.");

        System.out.println("President elected: Member " + electedPresident);
    }

    @Test
    public void testIfM2OrM3isOffline() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        members = new ArrayList<>();
        ports = new ArrayList<>();
        int basePort = 2000;
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            ports.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            members.add(member);
        }

        votingServer.setMembers(members, ports);

        // No delay time for all members
        Thread member1Thread = new Thread(() -> members.get(0).sendPrepareRequest());
        Thread member2Thread = new Thread(() -> members.get(1).sendPrepareRequest());
        Thread member3Thread = new Thread(() -> members.get(2).sendPrepareRequest());
        members.get(2).forceOffline();

        member1Thread.start();
        member2Thread.start();
        member3Thread.start();

        member1Thread.join();
        member2Thread.join();
        member3Thread.join();

        // sleep for 90 seconds
        Thread.sleep(60000);
        if (votingServer.getPresident() == null) {
            member1Thread = new Thread(() -> members.get(0).sendPrepareRequest());
            member1Thread.start();
            member1Thread.join();
            Thread.sleep(60000);
        }
        assertEquals("1", votingServer.getPresident(), "Immediate response failed");
    }

}
