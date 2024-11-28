package main.votingServer;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import main.communicate.Communicate;
import main.member.AcceptedProposal;
import main.member.Member;

public class VotingServer implements MessageHandler {

    private List<Member> multipleMembers; // list of all multipleMembers
    private Map<String, Integer> promiseCount; // counts promises for proposals
    private Map<String, Integer> acceptedCount; // counts accepted proposals
    Communicate communicate; // handles communication
    Map<String, Socket> socketMap = new HashMap<>(); // tracks sockets by member id
    Map<String, Integer> portMap = new HashMap<>(); // tracks multiPorts by member id
    private final Object lock = new Object(); // lock for sync
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // timeout scheduler
    private final Map<String, ScheduledFuture<?>> timeouts = new ConcurrentHashMap<>(); // tracks proposal timeouts
    private String president = null; // elected president

    public VotingServer() {
        this.promiseCount = new HashMap<>(); // init promise count
        this.acceptedCount = new HashMap<>(); // init accepted count
    }

    public void setMembers(List<Member> multipleMembers, List<Integer> port) {
        this.multipleMembers = multipleMembers; // save multipleMembers
        for (int i = 0; i < multipleMembers.size(); i++) {
            portMap.put(multipleMembers.get(i).getMemberId(), port.get(i)); // map member ids to multiPorts
        }
    }

    public void setCommunicate(Communicate communicate) {
        this.communicate = communicate; // set communication handler
    }

    @Override
    public Map<String, Integer> getPortMap() {
        return this.portMap;    // return port map
    }

    @Override
    public void handleMessage(String message, String memberId) {
        if (president != null) {
            closeSockets();  // stop if president decided
            return;
        }
        String[] parts = message.split(" ");
        String messageType = parts[0].trim();
        if (messageType.equals("PREPARE")) {
            handlePrepareRequest(message, memberId);
        } else if (messageType.equals("PROMISE")) {
            handlePromise(message, memberId);
        } else if (messageType.equals("ACCEPT")) {
            handleAcceptRequest(message, memberId);
        } else if (messageType.equals("ACCEPTED")) {
            handleAccepted(message, memberId);
        } else {
            handleRejection(message, memberId);
        }
    }

    public void broadcast(String message) {
        for (Member member : multipleMembers) {
            communicate.sendMessage(member.getMemberId(), message); // send message to all multipleMembers
        }
    }

    @Override
    public Map<String, Socket> getSocketMap() {
        return this.socketMap;  // return socket map
    }

    public void addMember(String memberId, Socket socket) {
        socketMap.put(memberId, socket);    // add member socket
    }

    private void handlePrepareRequest(String message, String memberId) {
        String[] parts = message.split(" ");
        String proposalNum = parts[1];
        String proposerId = parts[1].split(":")[0];
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = now.format(formatter);
        System.out
                .println("[" + formattedDateTime + "]" + " Member " + memberId + " received prepareRequest from proposer "
                        + proposerId
                        + " with proposalNum " + proposalNum);
        Member currentMember = multipleMembers.stream().filter(m -> m.getMemberId().equals(memberId)).findFirst().get();
        if (currentMember.getHighestSeenProposalNumber() == null
                || compareProposalNumbers(proposalNum, currentMember.getHighestSeenProposalNumber())) {
            currentMember.setHighestSeenProposalNumber(proposalNum);
            // send a promise to accept this proposal number
            if (currentMember.getAcceptedProposal() == null) { //check for null
                currentMember.sendPromise(proposerId, proposalNum, null);
            } else {
                currentMember.sendPromise(proposerId, proposalNum, currentMember.getAcceptedProposal()); // reject proposal
            }
        } else {
            // Send a reject message
            currentMember.sendReject(proposalNum);
        }
        scheduleTimeoutForProposal(proposalNum);
    }

    private void handlePromise(String message, String memberId) {
        synchronized (lock) {
            Member currentMember = multipleMembers.stream().filter(m -> m.getMemberId().equals(memberId)).findFirst().get();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = now.format(formatter);
            System.out.println(
                    "[" + formattedDateTime + "]" + " Proposer " + memberId + " received promise from member " + memberId);
            String[] parts = message.split(" ");
            String proposalNum = parts[1];
            promiseCount.put(proposalNum, promiseCount.getOrDefault(proposalNum, 0) + 1);

            if (parts.length > 2) {     // check for attached proposal pair
                AcceptedProposal newPair = new AcceptedProposal();
                newPair.setAcceptedProposal(parts[2], parts[3]);
                currentMember.setAcceptedProposal(parts[1], newPair);
            }

            if (promiseCount.get(proposalNum) == 5) {
                now = LocalDateTime.now();
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                formattedDateTime = now.format(formatter);
                System.out
                        .println("[" + formattedDateTime + "]" + " Proposal " + proposalNum + " IS PROMISED BY THE MAJORITY");

                cancelTimeout("proposal:" + proposalNum);  // cancel timeout
                String proposalVal;
                if (currentMember.getAcceptedProposal() != null) {
                    proposalVal = currentMember.getAcceptedProposal().getProposalValue();
                } else {
                    proposalVal = null;
                }
                currentMember.sendAcceptRequest(proposalNum, proposalVal, currentMember.getMemberId());
            }
        }
    }

    private void handleAcceptRequest(String message, String memberId) {
        Member currentMember = multipleMembers.stream().filter(m -> m.getMemberId().equals(memberId)).findFirst().get();
        String[] parts = message.split(" ");
        String proposalNum = parts[1];
        String proposerId = parts[1].split(":")[0];
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = now.format(formatter);
        System.out.println(
                "[" + formattedDateTime + "]" + " Member " + memberId + " received acceptRequest from proposer " + proposerId
                + " with proposalNum " + proposalNum);
        String proposalVal = parts[2];
        if (currentMember.getHighestSeenProposalNumber() == null
                || compareProposalNumbers(proposalNum,
                        currentMember.getHighestSeenProposalNumber())) {
            currentMember.setHighestSeenProposalNumber(proposalNum);
            // Send a promise to accept this proposal number
            currentMember.sendAccepted(proposerId, proposalNum, proposalVal);  // accept proposal
        } else {
            currentMember.sendReject(proposalNum);   //reject proposal
        }
        scheduleTimeoutForAcceptRequest(proposalNum);
    }

    private void handleAccepted(String message, String memberId) {
        synchronized (lock) {
            String[] parts = message.split(" ");
            String proposalNum = parts[1];
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = now.format(formatter);
            System.out.println(
                    "[" + formattedDateTime + "]" + " Proposer " + memberId + " received accepted from member " + memberId);
            String proposerValue = parts[2];
            acceptedCount.put(proposalNum, acceptedCount.getOrDefault(proposalNum, 0) + 1);

            if (acceptedCount.get(proposalNum) == 5) {
                now = LocalDateTime.now();
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                formattedDateTime = now.format(formatter);
                System.out
                        .println("[" + formattedDateTime + "]" + " Proposal " + proposalNum + " IS ACCEPTED BY THE MAJORITY");
                cancelTimeout("accept:" + proposalNum);  // cancel timeout
                now = LocalDateTime.now();
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                formattedDateTime = now.format(formatter);
                System.out.println(
                        "[" + formattedDateTime + "]" + " Member " + proposerValue + " is decided to become a president");
                president = proposerValue;  // set president
                closeSockets();   // close sockets
                communicate.closeServerSocket();    // stop communication
            }
        }
    }

    private void scheduleTimeoutForProposal(String proposalNum) {
        ScheduledFuture<?> timeout = scheduler.schedule(() -> {
            // cancel proposal
            //start new
            promiseCount.remove(proposalNum);
        }, 20, TimeUnit.SECONDS);
        timeouts.put("proposal:" + proposalNum, timeout);
    }

    private void scheduleTimeoutForAcceptRequest(String proposalNum) {
        ScheduledFuture<?> timeout = scheduler.schedule(() -> {
            // cancel accept request
            // start new
            acceptedCount.remove(proposalNum);
        }, 20, TimeUnit.SECONDS);
        timeouts.put("accept:" + proposalNum, timeout);
    }

    private void cancelTimeout(String key) {
        ScheduledFuture<?> timeout = timeouts.remove(key);
        if (timeout != null) {
            timeout.cancel(false);
        }
    }

    public boolean compareProposalNumbers(String proposalNum1, String proposalNum2) {
        if (proposalNum1 == null) {
            return false;
        }
        if (proposalNum2 == null) {
            return true;
        }

        if (proposalNum1.equals(proposalNum2)) {
            return true;
        }

        String[] parts1 = proposalNum1.split(":");
        String[] parts2 = proposalNum2.split(":");

        int count1 = Integer.parseInt(parts1[1]);
        int count2 = Integer.parseInt(parts2[1]);

        if (count1 < count2) {
            return false;
        } else if (count1 > count2) {
            return true;
        } else {
            return Integer.parseInt(parts1[0]) >= Integer.parseInt(parts2[0]);
        }
    }

    private void handleRejection(String message, String memberId) {
        String[] parts = message.split(" ");
        String proposalNum = parts[1];
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = now.format(formatter);
        System.out
                .println("[" + formattedDateTime + "]" + " Proposal " + proposalNum + " is rejected by member " + memberId);
    }

    public void closeSockets() {
        synchronized (lock) {
            for (Socket socket : socketMap.values()) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        socketMap.clear();
    }

    public String getPresident() {      // return elected president
        return president;
    }

}
