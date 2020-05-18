import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
//Not to be edited
public class ParticipantLogger {
	
	private static ParticipantLogger logger = null;
	
	private final PrintStream ps;
	private final UDPLoggerClient udpLoggerClient;
	private final int thisParticipant;
	
	public static void initLogger(int loggerServerPort, int processId, int timeout) throws IOException {
		if (logger == null)
			logger = new ParticipantLogger(loggerServerPort, processId, timeout);
		else
			throw new RuntimeException("ParticipantLogger already initialised");
	}
	
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

	public void joinSent(int coordinator) {
		logMessage("[P" + thisParticipant + "] JOIN sent to Coordinator on port " + coordinator);
	}
	
	public void detailsReceived(List<Integer> participants) {
		logMessage("[P" + thisParticipant + "] received participant ports: " + participants.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void voteOptionsReceived(List<String> voteOptions) {
		logMessage("[P" + thisParticipant + "] received vote options: " + voteOptions.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void beginRound(int round) {
		logMessage("[P" + thisParticipant + "] begin round " + round);
	}
	
	public void endRound(int round) {
		logMessage("[P" + thisParticipant + "] end round " + round);
	}
	
	public void votesSent(int destinationParticipant, List<Vote> votes) {
		logMessage("[P" + thisParticipant + "] votes sent to " + destinationParticipant + ": " + votes.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void votesReceived(int senderParticipant, List<Vote> votes) {
		logMessage("[P" + thisParticipant + "] votes received from " + senderParticipant + ": " + votes.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void outcomeDecided(String vote) {
		logMessage("[P" + thisParticipant + "] outcome vote decided: " + vote);
	}
	
	public void outcomeNotified(String vote) {
		logMessage("[P" + thisParticipant + "] outcome vote sent to Coordinator: " + vote);
	}
	
	public void participantCrashed(int crashedParticipant) {
		logMessage("[P" + thisParticipant + "] participant crashed: " + crashedParticipant);
	}
	
	public void startedListening() {
		logMessage("[P" + thisParticipant + "] started listening on port " + thisParticipant);
	}
	
	public void connectionAccepted(int otherPort) {
		logMessage("[P" + thisParticipant + "] accepted connection from port " + otherPort);
	}
	
	public void connectionEstablished(int otherPort) {
		logMessage("[P" + thisParticipant + "] connection established to port " + otherPort);
	}
	
	public void messageSent(int destinationPort, String message) {
		logMessage("[P" + thisParticipant + "] message sent to " + destinationPort + ": \"" + message + "\"");
	}
	
	public void messageReceived(int senderPort, String message) {
		logMessage("[P" + thisParticipant + "] message received from " + senderPort + ": \"" + message + "\"");
	}
}
