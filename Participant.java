import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Participant {

    public static void main(String args []) throws IOException {

        int coordinatorPort = Integer.parseInt(args[0]);
        int loggerPort      = Integer.parseInt(args[1]);
        int participantPort = Integer.parseInt(args[2]);
        int timeout         = Integer.parseInt(args[3]);

        try {
            InetAddress hostName;
            hostName = InetAddress.getLocalHost();

            System.out.println("Server Port is: " +coordinatorPort);
            System.out.println("Participant Port is: " +participantPort);
            System.out.println("Local Host IP is: " +hostName.getHostAddress());
            System.out.println("Local Host Name is: " +hostName);

            Socket participantSocket = new Socket(hostName,coordinatorPort);
            participantSocket.setSoTimeout(timeout);

            OutputStream outToCoordinator = participantSocket.getOutputStream();
            DataOutputStream outToCoordinatorStream = new DataOutputStream(outToCoordinator);

            InputStream inFromCoordinator = participantSocket.getInputStream();
            DataInputStream inFromCoordinatorStream = new DataInputStream(inFromCoordinator);


            while (true) {

                if (inFromCoordinatorStream.available() > 0)
                receiveMessage(inFromCoordinatorStream);
                try {
                    sendMessage("Join", outToCoordinatorStream, participantPort);
                    sendMessage("Vote", outToCoordinatorStream, participantPort);
                    //TimeUnit.SECONDS.sleep(3);
                    //receiveMessage(inFromCoordinatorStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void sendMessage(String messageType, DataOutputStream dataOutputStream, int myPortNumber)
    {
        String messageString = new String();
        try {
            switch(messageType) {
                case "Join":
                    messageString = "JOIN " +myPortNumber;
                    break;
                case "Vote":
                    messageString = "VOTE " +myPortNumber +" A";
                    break;
            }
            byte[] message = messageString.getBytes();
            dataOutputStream.writeInt(message.length);
            dataOutputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveMessage(DataInputStream dataInputStream)
    {
        int messageLength = 0;
        try {
            messageLength = dataInputStream.readInt();
            if (messageLength > 0)
            {
                byte[] inputData = new byte[messageLength];
                dataInputStream.readFully(inputData, 0, messageLength);
                String receivedMessage = new String(inputData, StandardCharsets.UTF_8);
                System.out.println("Server Says: " +receivedMessage);
                messageType(inputData);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    private static String getMessageType(byte[] message) {

        String stringMessage = new String(message, StandardCharsets.UTF_8);
        stringMessage.split(" ");
        return stringMessage;
    }

    public static void messageType(byte[] messageType) {
        List<String> otherClientPorts = new ArrayList<String>();
        List<String> voteOptions = new ArrayList<String>();

        String stringMessage = new String(messageType, StandardCharsets.UTF_8);
        String[] strings = stringMessage.split(" ", 4);

        switch (strings[0]) {
            case "DETAILS":

                for (String s : strings) {
                    otherClientPorts.add(s);
                }
                otherClientPorts.remove(0);
                otherClientPorts.remove(otherClientPorts.size() - 1);
                break;

            case "VOTE_OPTIONS":
                for (String s : strings) {
                    voteOptions.add(s);
                }
                voteOptions.remove(0);
                voteOptions.remove(voteOptions.size() - 1);
                break;
        }
    }
}