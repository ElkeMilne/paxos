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

    // test: verify member ID is returned correctly
    @Test
    public void testMemberIdRetrieval() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals("1", member.getMemberId());
    }

    // test: check generated proposal numbers increment correctly
    @Test
    public void testProposalNumberGeneration() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals("1:1", member.generateProposalNumber());
        assertEquals("1:2", member.generateProposalNumber());
    }

    // test: verify setting and getting the highest seen proposal number
    @Test
    public void testHighestProposalNumberTracking() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        member.setHighestSeenProposalNumber("1:3");
        assertEquals("1:3", member.getHighestSeenProposalNumber());
    }

    // test: ensure delay times are initialized correctly based on member ID
    @Test
    public void testDelayTimeInitialization() {
        Member member1 = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        assertEquals(0, member1.getDelayTime()); // no delay for member 1

        Member member2 = new Member("2", mock(Communicate.class), mock(VotingServer.class));
        assertEquals(10000, member2.getDelayTime()); // fixed delay for member 2

        Member member3 = new Member("3", mock(Communicate.class), mock(VotingServer.class));
        assertTrue(member3.getDelayTime() >= 4000 && member3.getDelayTime() <= 9000); // random delay for member 3

        Member memberDefault = new Member("999", mock(Communicate.class), mock(VotingServer.class));
        assertTrue(memberDefault.getDelayTime() >= 4000 && memberDefault.getDelayTime() <= 9000); // random delay for others
    }

    // test: verify delay time can be manually updated
    @Test
    public void testSetCustomDelayTime() {
        Member member = new Member("1", mock(Communicate.class), mock(VotingServer.class));
        member.setDelayTime(5000);
        assertEquals(5000, member.getDelayTime());
    }

    // test: validate prepare request broadcasting
    @Test
    public void testPrepareRequestBroadcasting() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);

        member.sendPrepareRequest();
        verify(mockServer).broadcast(anyString()); // ensure broadcast is called
    }

    // test: ensure no messages are sent when member is offline
    @Test
    public void testRejectMessageWhenOffline() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);
        member.forceOffline(); // simulate member going offline
        member.sendReject("1:4");

        verify(mockComm, never()).sendMessage(anyString(), anyString()); // verify no message sent
    }

    // test: ensure promise messages are sent when member is online
    @Test
    public void testPromiseMessageWhenOnline() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);

        member.sendPromise("2", "1:1", null);
        verify(mockComm).sendMessage(eq("2"), anyString()); // verify message sent to proposer
    }

    // test: validate accepted proposal is stored correctly
    @Test
    public void testAcceptedProposalStorage() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);

        AcceptedProposal proposal = new AcceptedProposal();
        proposal.setAcceptedProposal("1.2", "value2");
        member.setAcceptedProposal("1:1", proposal);

        assertEquals(proposal, member.getAcceptedProposal());
    }

    // test: ensure lower proposal numbers do not overwrite accepted proposals
    @Test
    public void testIgnoreLowerProposalNumbers() {
        Communicate mockComm = mock(Communicate.class);
        VotingServer mockServer = mock(VotingServer.class);
        Member member = new Member("1", mockComm, mockServer);

        AcceptedProposal proposal1 = new AcceptedProposal();
        proposal1.setAcceptedProposal("1.2", "value2");

        AcceptedProposal proposal2 = new AcceptedProposal();
        proposal2.setAcceptedProposal("1.1", "value1");

        member.setAcceptedProposal("1:1", proposal1);
        member.setAcceptedProposal("1:2", proposal2);

        assertEquals(proposal1, member.getAcceptedProposal()); // proposal1 should remain
    }
}
