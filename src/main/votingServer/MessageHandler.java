package main.votingServer;

import java.net.Socket;
import java.util.Map;

public interface MessageHandler {

    // handles incoming messages from multipleMembers
    void handleMessage(String message, String memberId);

    // gets the map of connected sockets for all multipleMembers
    public Map<String, Socket> getSocketMap();

    // gets the map of multiPorts assigned to each member
    public Map<String, Integer> getPortMap();
}
