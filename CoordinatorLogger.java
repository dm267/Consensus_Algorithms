import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
//Not to be edited
public class CoordinatorLogger {
	
	private static CoordinatorLogger logger = null;
	
	private final PrintStream ps;
	private final UDPLoggerClient udpLoggerClient;
	
	public static void initLogger(int loggerServerPort, int processId, int timeout) throws IOException {
		if (logger == null)
			logger = new CoordinatorLogger(loggerServerPort, processId, timeout);
		else
			throw new RuntimeException("CoordinatorLogger already initialised");
	}
	
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

	public void startedListening(int port) {
		logMessage("[C] started listening on port " + port);
	}
	
	public void joinReceived(int participantPort) {
		logMessage("[C] JOIN received from " + participantPort);
	}
	
	public void detailsSent(int destinationPort, List<Integer> participantPorts) {
		logMessage("[C] details sent to " + destinationPort + ": " + participantPorts.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void voteOptionsSent(int destinationPort, List<String> voteOptions) {
		logMessage("[C] vote options sent to " + destinationPort + ": " + voteOptions.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
	
	public void outcomeReceived(int participantPort, String vote) {
		logMessage("[C] outcome " + vote + " received from " + participantPort);
	}
	
	public void connectionAccepted(int otherPort) {
		logMessage("[C] accepted connection from port " + otherPort);
	}
	
	public void messageSent(int destinationPort, String message) {
		logMessage("[C] message sent to " + destinationPort + ": \"" + message + "\"");
	}
	
	public void messageReceived(int senderPort, String message) {
		logMessage("[C] message received from " + senderPort + ": \"" + message + "\"");
	}
	
	public void participantCrashed(int crashedParticipant) {
		logMessage("[C] participant crashed: " + crashedParticipant);
	}
}
