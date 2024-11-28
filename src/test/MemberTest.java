package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import main.communicate.Communicate;
import main.member.AcceptedProposal;
import main.member.Member;
import main.votingServer.VotingServer;

public class MemberTest {

    @Test
    public void testGetMemberId() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals("1", member.getMemberId());
    }

    @Test
    public void testGenerateProposalNumber() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals("1:1", member.generateProposalNumber());
        assertEquals("1:2", member.generateProposalNumber());
    }

    @Test
    public void testSetAndGetHighestSeenProposalNumber() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        member.setHighestSeenProposalNumber("1:3");
        assertEquals("1:3", member.getHighestSeenProposalNumber());
    }

    @Test
    public void testSendPrepareRequest() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);

        member.sendPrepareRequest();
        verify(mockServer).broadcast(anyString()); // Update as necessary
    }

    @Test
    public void testDelayTimeInitialization() {
        // Member ID 1
        Member member1 = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals(0, member1.getDelayTime());

        // Member ID 2
        Member member2 = new Member("2", mock(Communicate.class), mock(VotingServer.class));
        assertEquals(10000, member2.getDelayTime());

        // Member ID 3
        Member member3 = new Member("3", mock(Communicate.class), mock(VotingServer.class));
        assertTrue(member3.getDelayTime() >= 4000 && member3.getDelayTime() <= 9000);

        // Default case
        Member memberDefault = new Member("999", mock(Communicate.class), mock(VotingServer.class));
        assertTrue(memberDefault.getDelayTime() >= 4000 && memberDefault.getDelayTime() <= 9000);
    }

    @Test
    public void testSendRejectWhenOffline() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        member.forceOffline();
        member.sendReject("1:4");

        verify(mockComm, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testSendPromiseWhenOnline() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        member.sendPromise("2", "1:1", null);

        // Verifying that a message is sent when the member is online
        verify(mockComm).sendMessage(eq("2"), anyString());
    }

    @Test
    public void testSetAcceptedProposal() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        AcceptedProposal pair = new AcceptedProposal();
        pair.setAcceptedProposal("1.2", "value2");
        member.setAcceptedProposal("1:1", pair);

        assertEquals(pair, member.getAcceptedProposal());
    }

    @Test
    public void testSetAcceptedProposalWithLowerProposalNumber() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        AcceptedProposal pair1 = new AcceptedProposal();
        pair1.setAcceptedProposal("1.2", "value2");
        AcceptedProposal pair2 = new AcceptedProposal();
        pair2.setAcceptedProposal("1.1", "value1");
        member.setAcceptedProposal("1:1", pair1);
        member.setAcceptedProposal("1:2", pair2);

        assertNotEquals("1:1", member.getAcceptedProposal().getProposalNumber());
    }

    @Test
    public void testSetDelayTime() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        member.setDelayTime(5000);
        assertEquals(5000, member.getDelayTime());
    }

}
