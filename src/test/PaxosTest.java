package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import main.communicate.Communicate;
import main.member.Member;
import main.votingServer.VotingServer;

import java.util.ArrayList;
import java.util.List;

public class PaxosTest {

    private List<Member> multipleMembers; // list of multipleMembers in the voting system
    private VotingServer votingServer; // central voting server
    private List<Integer> multiPorts; // multiPorts assigned to multipleMembers
    Communicate serverCommunicate; // communication object for the server

    // test: verify consensus when multiple proposals are sent simultaneously
    @Test
    public void testMultipleProposalsAtOnce() throws InterruptedException {
        votingServer = new VotingServer();

        // set up communication for the server
        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 7000;

        // initialize multipleMembers and their communication multiPorts
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        // attach multipleMembers to the voting server
        votingServer.setMembers(multipleMembers, multiPorts);

        // simulate three multipleMembers sending prepare requests at the same time
        Thread proposer1Thread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        Thread proposer2Thread = new Thread(() -> multipleMembers.get(1).sendPrepareRequest());
        Thread proposer3Thread = new Thread(() -> multipleMembers.get(2).sendPrepareRequest());

        proposer1Thread.start();
        proposer2Thread.start();
        proposer3Thread.start();

        // wait for all threads to complete
        proposer1Thread.join();
        proposer2Thread.join();
        proposer3Thread.join();

        // give time for consensus resolution
        Thread.sleep(30000);

        // verify that a president is elected
        String electedPresident = votingServer.getPresident();
        assertNotNull(electedPresident, "No consensus was reached for the president.");
        assertTrue(electedPresident.equals("1") || electedPresident.equals("2") || electedPresident.equals("3"),
                "Unexpected president elected.");

        System.out.println("President elected: Member " + electedPresident);
    }

    // test: ensure system handles offline multipleMembers correctly
    @Test
    public void testHandleOfflineMembers() throws InterruptedException {
        votingServer = new VotingServer();

        // set up communication for the server
        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 2000;

        // initialize multipleMembers and their communication multiPorts
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        // attach multipleMembers to the voting server
        votingServer.setMembers(multipleMembers, multiPorts);

        // simulate member 3 going offline and others sending prepare requests
        Thread member1Thread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        Thread member2Thread = new Thread(() -> multipleMembers.get(1).sendPrepareRequest());
        Thread member3Thread = new Thread(() -> multipleMembers.get(2).sendPrepareRequest());
        multipleMembers.get(2).forceOffline(); // member 3 is offline

        member1Thread.start();
        member2Thread.start();
        member3Thread.start();

        // wait for all threads to complete
        member1Thread.join();
        member2Thread.join();
        member3Thread.join();

        // allow time for consensus resolution
        Thread.sleep(60000);

        // if no consensus, retry with member 1
        if (votingServer.getPresident() == null) {
            member1Thread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
            member1Thread.start();
            member1Thread.join();
            Thread.sleep(60000);
        }

        // verify that member 1 is elected as president
        assertEquals("1", votingServer.getPresident(), "Failed to handle offline multipleMembers correctly.");
    }

    // test: validate concurrent proposals do not break consensus
    @Test
    public void testConcurrentProposals() throws InterruptedException {
        votingServer = new VotingServer();

        // set up communication for the server
        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 9000;

        // initialize multipleMembers and their communication multiPorts
        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        // attach multipleMembers to the voting server
        votingServer.setMembers(multipleMembers, multiPorts);

        // simulate two multipleMembers sending prepare requests at the same time
        Thread member1Thread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        Thread member2Thread = new Thread(() -> multipleMembers.get(1).sendPrepareRequest());

        member1Thread.start();
        member2Thread.start();

        // wait for threads to complete
        member1Thread.join();
        member2Thread.join();

        // give time for consensus resolution
        Thread.sleep(30000);

        // verify that member 1 is elected as president
        assertEquals("1", votingServer.getPresident(), "Concurrent proposal resolution failed.");
    }

    // test: validate immediate responses
    @Test
    public void testImmediateResponses() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 3000;

        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.setDelayTime(0); // no delay for immediate responses
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        votingServer.setMembers(multipleMembers, multiPorts);

        Thread proposerThread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        proposerThread.start();
        proposerThread.join();

        Thread.sleep(30000);

        assertEquals("1", votingServer.getPresident(), "Immediate response test failed.");
    }

    // test: validate behavior with small delays
    @Test
    public void testSmallDelays() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 4000;

        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.setDelayTime((int) (Math.random() * 1000)); // small delays
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        votingServer.setMembers(multipleMembers, multiPorts);

        Thread proposerThread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        proposerThread.start();
        proposerThread.join();

        Thread.sleep(30000);

        assertEquals("1", votingServer.getPresident(), "Small delay test failed.");
    }

    // test: validate behavior with large delays
    @Test
    public void testLargeDelays() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 5000;

        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);
            member.setDelayTime((int) (Math.random() * 5000 + 5000)); // large delays
            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        votingServer.setMembers(multipleMembers, multiPorts);

        Thread proposerThread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        proposerThread.start();
        proposerThread.join();

        Thread.sleep(60000);

        assertEquals("1", votingServer.getPresident(), "Large delay test failed.");
    }

    // test: validate mixed response profiles
    @Test
    public void testMixedResponseProfiles() throws InterruptedException {
        votingServer = new VotingServer();

        serverCommunicate = new Communicate(votingServer);
        votingServer.setCommunicate(serverCommunicate);

        multipleMembers = new ArrayList<>();
        multiPorts = new ArrayList<>();
        int basePort = 6000;

        for (int i = 1; i <= 9; i++) {
            String memberId = "" + i;
            Communicate communicate = new Communicate(votingServer);
            Member member = new Member(memberId, communicate, votingServer);
            int port = basePort + i;
            multiPorts.add(port);

            // Assign different delays based on the member ID
            if (i <= 3) {
                member.setDelayTime(0); // immediate responses
            } else if (i <= 6) {
                member.setDelayTime((int) (Math.random() * 2000)); // small delays
            } else {
                member.setDelayTime((int) (Math.random() * 5000 + 5000)); // large delays
            }

            member.getCommunicate().startServer(port, member.getMemberId());
            multipleMembers.add(member);
        }

        votingServer.setMembers(multipleMembers, multiPorts);

        Thread proposerThread = new Thread(() -> multipleMembers.get(0).sendPrepareRequest());
        proposerThread.start();
        proposerThread.join();

        Thread.sleep(60000);

        assertEquals("1", votingServer.getPresident(), "Mixed response profiles test failed.");
    }

}
