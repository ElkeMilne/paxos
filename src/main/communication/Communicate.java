// Part of "main.communicate" package. Handles server comms.
package main.communicate;

import java.io.*;
import java.net.*;
import main.votingServer.MessageHandler;

public class Communicate {

    private final MessageHandler messageHandler; // Handles messages.
    private ServerSocket serverSocket; // Server socket for connections.

    // Constructor: Initialises with a MessageHandler.
    public Communicate(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    // Get the server socket.
    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    // Close the server socket if it's open.
    public void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        try {
            serverSocket.close();
            System.out.println("Election done. Server socket closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the server on a given port.
    public void startServer(int port, String memberId) {
        try {
            serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        addMember(memberId, clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a new member to the socket map.
    public void addMember(String memberId, Socket socket) {
        messageHandler.getSocketMap().put(memberId, socket);
        new Thread(() -> handleSocket(memberId, socket)).start();
    }

    // Handle incoming messages from a socket.
    private void handleSocket(String memberId, Socket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                messageHandler.handleMessage(message, memberId);
            }
        } catch (IOException e) {
            // Ignore errors for now.
        } finally {
            try {
                socket.close(); // Clean up.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send a message to a specific member.
    public synchronized void sendMessage(String memberId, String message) {
        try {
            Socket socket = new Socket("localhost", messageHandler.getPortMap().get(memberId));
            if (socket != null && !socket.isClosed()) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
            }
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Unknown host when sending message.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO error when sending message.");
        }
    }
}
