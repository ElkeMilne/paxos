package main.votingServer;

import java.net.Socket;
import java.util.Map;

public interface MessageHandler {

    // handles incoming messages from members
    void handleMessage(String message, String memberId);

    // gets the map of connected sockets for all members
    public Map<String, Socket> getSocketMap();

    // gets the map of ports assigned to each member
    public Map<String, Integer> getPortMap();
}
