import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipantLogger {

    private static ParticipantLogger logger = null;

    private final PrintStream ps;
    private final UDPLoggerClient udpLoggerClient;
    private final int thisParticipant;

    /**
     * Initialises the Logger for this Participant
     *
     * @param loggerServerPort the UDP port where the Logger process is listening on
     * @param processId the ID of this Participant, i.e. the TCP port where this Participant is listening on
     * @param timeout the timeout in milliseconds for this Participant
     * @throws IOException
     */
    public static void initLogger(int loggerServerPort, int processId, int timeout) throws IOException {
        if (logger == null)
            logger = new ParticipantLogger(loggerServerPort, processId, timeout);
        else
            throw new RuntimeException("ParticipantLogger already initialised");
    }

    /**
     * @return the singleton instance of the Logger for this Participant
     */
    public static ParticipantLogger getLogger() {
        if (logger == null)
            throw new RuntimeException("ParticipantLogger not initialised yet");
        return logger;
    }

    private ParticipantLogger(int loggerServerPort, int processId, int timeout) throws IOException {
        thisParticipant = processId;
        udpLoggerClient = new UDPLoggerClient(loggerServerPort, processId, timeout);
        ps = new PrintStream("participant_" + thisParticipant + "_" + System.currentTimeMillis() + ".log");
    }

    protected void logMessage(String message) {
        ps.println(message);
        if (udpLoggerClient != null)
            try {
                udpLoggerClient.logToServer(message);
            } catch (IOException e) {
                ps.println("[P" + thisParticipant + "] Exception caught: " + e.getMessage());
                ps.println("[P" + thisParticipant + "] Stack trace: " + e.getStackTrace());
            }
    }

    /**
     * To be invoked when this Participant sends the JOIN message to the Coordinator
     *
     * @param coordinatorId the ID of the Coordinator, i.e. the port where the Coordinator is listening on
     */
    public void joinSent(int coordinatorId) {
        logMessage("[P" + thisParticipant + "] JOIN sent to Coordinator on port " + coordinatorId);
    }

    /**
     * To be invoked when this Participant receives the DETAILS message from the Coordinator
     *
     * @param participantIds the list of Participant IDs
     */
    public void detailsReceived(List<Integer> participantIds) {
        logMessage("[P" + thisParticipant + "] received participant ports: " + participantIds.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when this Participant receives the VOTE_OPTIONS message from the Coordinator
     *
     * @param votingOptions the list of voting options
     */
    public void voteOptionsReceived(List<String> votingOptions) {
        logMessage("[P" + thisParticipant + "] received vote options: " + votingOptions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when this Participant begins a new round
     *
     * @param round the number of the new round, e.g. 1 for the first round, 2 for the second and so on
     */
    public void beginRound(int round) {
        logMessage("[P" + thisParticipant + "] begin round " + round);
    }

    /**
     * To be invoked when this Participant ends a round
     *
     * @param round the number of the round, e.g. 1 for the first round, 2 for the second and so on
     */
    public void endRound(int round) {
        logMessage("[P" + thisParticipant + "] end round " + round);
    }

    /**
     * To be invoked when this Participant sends a VOTE message to another Participant
     *
     * @param destinationParticipantId the ID of the other Participant, the port where the Participant is listening on
     * @param votes the list of votes sent
     */
    public void votesSent(int destinationParticipantId, List<Vote> votes) {
        logMessage("[P" + thisParticipant + "] votes sent to " + destinationParticipantId + ": " + votes.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when this Participant receives a VOTE message from another Participant
     *
     * @param senderParticipantId the ID of the other Participant, the port where the Participant is listening on
     * @param votes the list of votes received
     */
    public void votesReceived(int senderParticipantId, List<Vote> votes) {
        logMessage("[P" + thisParticipant + "] votes received from " + senderParticipantId + ": " + votes.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when this Participant decides the outcome of the voting
     *
     * @param vote the vote decided by this Participant
     * @param participantIds the list of IDs of Participants that were taken into account in settling the vote
     */
    public void outcomeDecided(String vote, List<Integer> participantIds) {
        logMessage("[P" + thisParticipant + "] outcome vote decided: " + vote + "(based on votes of Participants " + participantIds.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")");
    }

    /**
     * To be invoked when this Participant sends the OUTCOME message to the Coordinator
     *
     * @param vote the vote decided by this Participant
     * @param participantIds the list of IDs of Participants that were taken into account in settling the vote
     */
    public void outcomeNotified(String vote, List<Integer> participantIds) {
        logMessage("[P" + thisParticipant + "] outcome vote sent to Coordinator: " + vote + "(based on votes of Participants " + participantIds.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")");
    }

    /**
     * To be invoked when this Participant detects the crash of another Participant.
     * This method must be invoked only if the ID of the crashed Participant is known.
     *
     * @param crashedParticipantId the ID of the crashed Participant, i.e. the port where the crashed Participant was listening on
     */
    public void participantCrashed(int crashedParticipantId) {
        logMessage("[P" + thisParticipant + "] participant crashed: " + crashedParticipantId);
    }

    /**
     * To be invoked when this Participant starts listening for incoming TCP connections
     */
    public void startedListening() {
        logMessage("[P" + thisParticipant + "] started listening on port " + thisParticipant);
    }

    /**
     * To be invoked when another Participant establishes a TCP connection with this Participant
     *
     * @param otherPort the remote port number to which this socket is connected; note that this is different from the ID of the other Participant
     */
    public void connectionAccepted(int otherPort) {
        logMessage("[P" + thisParticipant + "] accepted connection from port " + otherPort);
    }

    /**
     * To be invoked when this Participant establishes a TCP connection with another process
     *
     * @param otherPort the TCP port where the other process is listening on
     */
    public void connectionEstablished(int otherPort) {
        logMessage("[P" + thisParticipant + "] connection established to port " + otherPort);
    }

    /**
     * To be invoked when this Participant sends a TCP message to another process
     *
     * @param destinationPort the remote port number to which the message is sent
     * @param message the message sent
     */
    public void messageSent(int destinationPort, String message) {
        logMessage("[P" + thisParticipant + "] message sent to " + destinationPort + ": \"" + message + "\"");
    }

    /**
     * To be invoked when this Participant receives a TCP message from another process
     *
     * @param senderPort the remote port number from which the message is received
     * @param message the received message
     */
    public void messageReceived(int senderPort, String message) {
        logMessage("[P" + thisParticipant + "] message received from " + senderPort + ": \"" + message + "\"");
    }
}
