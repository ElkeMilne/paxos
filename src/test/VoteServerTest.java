package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import main.communicate.Communicate;
import main.member.Member;
import main.votingServer.VotingServer;

class VotingServerTest {

    // test: verify multipleMembers and their multiPorts are set correctly in the voting server
    @Test
    void testSetMembersCorrectly() {
        VotingServer server = new VotingServer();
        List<Member> multipleMembers = new ArrayList<>();
        List<Integer> multiPorts = new ArrayList<>();

        multipleMembers.add(new Member("1", mock(Communicate.class), server));
        multipleMembers.add(new Member("2", mock(Communicate.class), server));
        multiPorts.add(4567);
        multiPorts.add(4568);

        server.setMembers(multipleMembers, multiPorts);

        Map<String, Integer> portMap = server.getPortMap();
        assertEquals(2, portMap.size()); // ensure two multipleMembers are added
        assertEquals(4567, portMap.get("1")); // check correct port for member 1
        assertEquals(4568, portMap.get("2")); // check correct port for member 2
    }

    // test: ensure a new member can be added with the correct socket mapping
    @Test
    void testAddingMember() {
        VotingServer server = new VotingServer();
        Socket mockSocket = mock(Socket.class);

        server.addMember("123", mockSocket);

        Map<String, Socket> socketMap = server.getSocketMap();
        assertEquals(1, socketMap.size()); // ensure one member is added
        assertEquals(mockSocket, socketMap.get("123")); // verify socket is mapped correctly
    }

    // test: validate setting the communication object for the server
    @Test
    void testSetCommunicateObject() {
        VotingServer server = new VotingServer();
        Communicate mockCommunicate = mock(Communicate.class);
        server.setCommunicate(mockCommunicate);
    }

    // test: check broadcasting a message to all multipleMembers
    @Test
    void testBroadcastingMessages() {
        VotingServer server = new VotingServer();
        Communicate mockCommunicate = mock(Communicate.class);
        server.setCommunicate(mockCommunicate);

        Member member1 = mock(Member.class);
        when(member1.getMemberId()).thenReturn("1");

        Member member2 = mock(Member.class);
        when(member2.getMemberId()).thenReturn("2");

        server.setMembers(Arrays.asList(member1, member2), Arrays.asList(8080, 8081));
        server.broadcast("testMessage");

        verify(mockCommunicate, times(1)).sendMessage("1", "testMessage"); // verify message sent to member 1
        verify(mockCommunicate, times(1)).sendMessage("2", "testMessage"); // verify message sent to member 2
    }

    // test: validate the comparison logic for proposal numbers
    @Test
    void testProposalNumberComparison() {
        VotingServer server = new VotingServer();
        assertTrue(server.compareProposalNumbers("2:1", "1:1")); // proposal 2:1 > 1:1
        assertFalse(server.compareProposalNumbers("1:1", "2:1")); // proposal 1:1 < 2:1
    }

    // test: check that the president is null initially
    @Test
    void testInitialPresidentIsNull() {
        VotingServer server = new VotingServer();
        assertNull(server.getPresident(), "Initially, president should be null");
    }

    // test: ensure handleMessage processes PREPARE messages correctly
    @Test
    void testHandlePrepareMessage() {
        VotingServer server = new VotingServer();
        Communicate mockCommunicate = mock(Communicate.class);
        server.setCommunicate(mockCommunicate);
        Member member = new Member("1", mockCommunicate, server);

        List<Member> multipleMembers = Arrays.asList(member);
        server.setMembers(multipleMembers, Arrays.asList(8080));

        server.handleMessage("PREPARE 1:1", "1"); // handle a prepare message
    }

    // test: ensure handleMessage processes PROMISE messages correctly
    @Test
    void testHandlePromiseMessage() {
        VotingServer server = new VotingServer();
        Communicate mockCommunicate = mock(Communicate.class);
        server.setCommunicate(mockCommunicate);
        Member member = new Member("1", mockCommunicate, server);

        List<Member> multipleMembers = Arrays.asList(member);
        server.setMembers(multipleMembers, Arrays.asList(8080));

        server.handleMessage("PROMISE 1:1", "1"); // handle a promise message
    }

    // test: validate that all sockets are closed correctly
    @Test
    void testClosingAllSockets() throws IOException {
        VotingServer server = new VotingServer();
        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);

        server.addMember("1", socket1);
        server.addMember("2", socket2);

        server.closeSockets();

        verify(socket1, times(1)).close(); // verify socket 1 is closed
        verify(socket2, times(1)).close(); // verify socket 2 is closed
    }
}
