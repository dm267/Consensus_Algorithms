import java.io.IOException;

public class UDPLoggerClient {

    private final int loggerServerPort;
    private final int processId;
    private final int timeout;

    /**
     * @param loggerServerPort the UDP port where the Logger process is listening o
     * @param processId the ID of the Participant/Coordinator, i.e. the TCP port where the Participant/Coordinator is listening on
     * @param timeout the timeout in milliseconds for this process
     */
    public UDPLoggerClient(int loggerServerPort, int processId, int timeout) {
        this.loggerServerPort = loggerServerPort;
        this.processId = processId;
        this.timeout = timeout;
    }

    public int getLoggerServerPort() {
        return loggerServerPort;
    }

    public int getProcessId() {
        return processId;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Sends a log message to the Logger process
     *
     * @param message the log message
     * @throws IOException
     */
    public void logToServer(String message) throws IOException {

        // YOUR IMPLEMENTATION HERE!!

    }
}
