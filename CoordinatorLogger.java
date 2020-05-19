import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinatorLogger {

    private static CoordinatorLogger logger = null;

    private final PrintStream ps;
    private final UDPLoggerClient udpLoggerClient;

    /**
     * Initialises the Logger for the Coordinator
     *
     * @param loggerServerPort the UDP port where the Logger process is listening on
     * @param processId the ID of the Coordinator, i.e. the TCP port where the Coordinator is listening on
     * @param timeout the timeout in milliseconds for the Coordinator
     * @throws IOException
     */
    public static void initLogger(int loggerServerPort, int processId, int timeout) throws IOException {
        if (logger == null)
            logger = new CoordinatorLogger(loggerServerPort, processId, timeout);
        else
            throw new RuntimeException("CoordinatorLogger already initialised");
    }

    /**
     * @return the singleton instance of the Logger for the Coordinator
     */
    public static CoordinatorLogger getLogger() {
        if (logger == null)
            throw new RuntimeException("CoordinatorLogger not initialised yet");
        return logger;
    }

    private CoordinatorLogger(int loggerServerPort, int processId, int timeout) throws IOException {
        udpLoggerClient = new UDPLoggerClient(loggerServerPort, processId, timeout);
        ps = new PrintStream("coordinator_" + System.currentTimeMillis() + ".log");
    }

    protected void logMessage(String message) {
        ps.println(message);
        if (udpLoggerClient != null)
            try {
                udpLoggerClient.logToServer(message);
            } catch (IOException e) {
                ps.println("[C] Exception caught: " + e.getMessage());
                ps.println("[C] Stack trace: " + e.getStackTrace());
            }
    }

    /**
     * To be invoked when the Coordinator starts listening for incoming TCP connections
     *
     * @param port the port where the Coordinator is listening on
     */
    public void startedListening(int port) {
        logMessage("[C] started listening on port " + port);
    }

    /**
     * To be invoked when the Coordinator receives a JOIN message
     *
     * @param participantId the ID of the Participant that joined, i.e. the port where the Participant is listening on
     */
    public void joinReceived(int participantId) {
        logMessage("[C] JOIN received from " + participantId);
    }

    /**
     * To be invoked when the Coordinator sends a DETAILS message to a Participant
     *
     * @param destinationParticipantId the ID of destination Participant, i.e. the port where the Participant is listening on
     * @param participantIds the list of IDs of the Participants that joined
     */
    public void detailsSent(int destinationParticipantId, List<Integer> participantIds) {
        logMessage("[C] details sent to " + destinationParticipantId + ": " + participantIds.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when the Coordinator sends a VOTE_OPTIONS message to a Participant
     *
     * @param destinationParticipantId the ID of destination Participant, i.e. the port where the Participant is listening on
     * @param votingOptions the list of voting options
     */
    public void voteOptionsSent(int destinationParticipantId, List<String> votingOptions) {
        logMessage("[C] vote options sent to " + destinationParticipantId + ": " + votingOptions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * To be invoked when the Coordinator receives an OUTCOME message
     *
     * @param participantId the ID of the Participant that sent the OUTCOME message, i.e. the port where the Participant is listening on
     * @param vote the voting option sent by the Participant
     */
    public void outcomeReceived(int participantId, String vote) {
        logMessage("[C] outcome " + vote + " received from " + participantId);
    }

    /**
     * To be invoked when a Participant establishes a TCP connection with the Coordinator
     *
     * @param otherPort the remote port number to which this socket is connected; note that this is different from the Participant ID
     */
    public void connectionAccepted(int otherPort) {
        logMessage("[C] accepted connection from port " + otherPort);
    }

    /**
     * To be invoked when the Coordinator sends a TCP message to a Participant
     *
     * @param destinationPort the remote port number to which the message is sent; note that this is different from the Participant ID
     * @param message the message sent
     */
    public void messageSent(int destinationPort, String message) {
        logMessage("[C] message sent to " + destinationPort + ": \"" + message + "\"");
    }

    /**
     * To be invoked when the Coordinator receives a TCP message from a Participant
     *
     * @param senderPort the remote port number from which the message is received; note that this is different from the Participant ID
     * @param message the message received
     */
    public void messageReceived(int senderPort, String message) {
        logMessage("[C] message received from " + senderPort + ": \"" + message + "\"");
    }

    /**
     * To be invoked when the Coordinator detects the crash of a Participant.
     * This method must be invoked only if the ID of the crashed Participant is known.
     *
     * @param crashedParticipantId the ID of the crashed Participant, i.e. the port where the crashed Participant was listening on
     */
    public void participantCrashed(int crashedParticipantId) {
        logMessage("[C] participant crashed: " + crashedParticipantId);
    }
}
