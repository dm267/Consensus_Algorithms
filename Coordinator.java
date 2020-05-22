import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class Coordinator{

    //Communication basically completed, start working on timed delays/ deadlocks/cyclic barriers ?/ consensus algorithm

    public List<participantThread> participants = new ArrayList<participantThread>();
    public static int numberOfParticipantsToConnect;
    public static int numberOfParticipantsConnected;
    //public static boolean isTooManyClients = false;
    public static int coordinatorPort;
    public static List<Integer> participantPorts = Collections.synchronizedList(new ArrayList<>());
    //public static HashMap<Integer, participantThread> listOfParticipants;
    public String participantMessage;

    public static List<String> voteOptions = Collections.synchronizedList(new ArrayList<>());

    //static Queue<MessagePacket> MsgQueue = new Bl<MessagePacket>();

    public static void main(String args[]) {

        if (args.length < 3) {
            System.out.println("Wrong number of arguments passed!" +"\nPlease try again.");
        } else {
            Coordinator.numberOfParticipantsToConnect = Integer.parseInt(args[1]);

                for (int i = 2; i < args.length; i++){
                    voteOptions.add(args[i]);
                }
                System.out.println("Voting Options Include: " +voteOptions);

                int coordinatorPort = Integer.parseInt(args[0]);
                new Coordinator().coordinatorStart(coordinatorPort);
            }
        }

    public void coordinatorStart(int coordinatorPort) {

        System.out.println("Server Port is: " + coordinatorPort);
        System.out.println("Number of expected participants to join is: " + numberOfParticipantsToConnect);
        List<participantThread> participants = this.participants;
        CyclicBarrier threadBarrier = new CyclicBarrier(2, new BarrierReached());
        int threadNumber = 0;

        try {
            ServerSocket coordinatorSocket = new ServerSocket(coordinatorPort);

            while (true)
            {
                Socket participantSocket;
                try
                {
                    participantSocket = coordinatorSocket.accept();
                    System.out.println("Number of participants currently connected: " +participants.size()); //tells us how many participants there are e.g how many threads
                    DataInputStream participantInputStream = new DataInputStream(participantSocket.getInputStream());
                    DataOutputStream particpantOutputStream = new DataOutputStream(participantSocket.getOutputStream());

                    if (numberOfParticipantsToConnect == numberOfParticipantsConnected)
                    {
                        System.out.println("Too many clients already connected!" +"\n");
                    } else {
                        System.out.println("Assigning New Thread To Client.");
                        participantThread newParticipantThread = new participantThread(participantSocket, participantInputStream, particpantOutputStream, threadBarrier, participants);
                        participants.add(newParticipantThread);
                        newParticipantThread.setName("Thread: " +threadNumber);
                        threadNumber++;
                        newParticipantThread.start();
                    }
                } catch (IOException e) {
                    coordinatorSocket.close();
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doSomethingOnAllThreads() {
        for (participantThread participant : participants) {
            //participant.otherMethod();
        }
    }

    class participantThread extends Thread implements Runnable{ //client handler

        final Socket participantSocket;
        final DataInputStream participantInputStream;
        final DataOutputStream participantOutputStream;

        int participantPort;
        int particpantCount;
        String participantMessage;
        CyclicBarrier participantThresholdBarrier = new CyclicBarrier(numberOfParticipantsToConnect);
        public List<participantThread> participants = new ArrayList<participantThread>();


        public participantThread(Socket participantSocket, DataInputStream participantInputStream, DataOutputStream participantOutputStream, CyclicBarrier participantThresholdBarrier, List<participantThread> particpiants){
            this.participantThresholdBarrier = participantThresholdBarrier;
            this.participantSocket = participantSocket;
            this.participantInputStream = participantInputStream;
            this.participantOutputStream = participantOutputStream;
            this.participants = particpiants;
        }

        public void run() {

            String thisThreadName = Thread.currentThread().getName();
            System.out.println("This thread is called: " +thisThreadName);

            while (true) {

                receiveMessage(this.participantInputStream,0, this, participants);

                try {
                    participantThresholdBarrier.await();
                    sendMessage("Details");
                    sendMessage("Vote");

                    TimeUnit.SECONDS.sleep(2);
                    receiveMessage(this.participantInputStream,0,this, participants);
                    sendMessage("Vote");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        synchronized void receiveMessage(DataInputStream participantInputStream, int threadID, participantThread client, List<participantThread> participants) //if VOTE then check the port number and remove this specific port from arraylist then forward msg onto other clients
        {
            //String messageString = new String();
            List<participantThread> localThreadPositions = new ArrayList<>();

            localThreadPositions.addAll(participants);
            localThreadPositions.remove(this);

            String whichAction = new String();
            String[] incomingString;
            int messageLength = 0;

            try {
                messageLength = participantInputStream.readInt();

                if (messageLength > 0)
                {
                    byte[] inputData = new byte[messageLength];
                    participantInputStream.readFully(inputData, 0, messageLength);
                    String message = new String(inputData, StandardCharsets.UTF_8);
                    incomingString = message.split(" ");
                    whichAction = incomingString[0];

                    switch(whichAction) {
                        case "JOIN":    //JOIN 12346
                            System.out.println("Client has joined");
                            participantPort = Integer.parseInt(incomingString[1]);
                            Coordinator.participantPorts.add(participantPort);
                            System.out.println("New Participant Listening on Port: " + participantPort);

                            break;
                        case "VOTE":    //VOTE 12346 A
                            //create temporary array list to copy class port numbers array list
                            // remove passed port number
                            // then iterate over list of client port numbers and
                            // send specific client ports the answer from this port

                            Map<String, String> mymap = new HashMap<String, String>();
                            mymap.put(incomingString[1],incomingString[2]);
                            String vote = incomingString[2];
                            //System.out.println(mymap.get(1)[0]);
                            System.out.println(participantPort +" hhas voted for : " +vote);

                            for (String i : mymap.keySet())
                            {
                                System.out.println(i +" has voted for: " +mymap.get(i));
                            }

                            long threadNumber = Thread.currentThread().getId();

                            sendMessage("Test");

                            //sendClientSpecificMessage(participants.get(1), "Sending to other clients from Thread: " + threadNumber);

                            System.out.println("Client has voted");

                    }
                }
            }

            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }


        public void sendMessage(String messageType)
        {
            String messageString = new String();
            try {
                switch(messageType) {
                    case "Details":
                        messageString = "DETAILS " +otherClientPorts(this); //works but must wait for all clients to connect to send message otherwise wont work correctly
                        break;
                    case "Vote":
                        messageString = "VOTE_OPTIONS " +convertVoteOptionsToString();
                        break;
                    case "portVote":
                        //messageString = "VOTE" +participantPort +vote;
                    default:
                        messageString = messageType;
            }
                byte[] message = messageString.getBytes();
                participantOutputStream.writeInt(message.length);
                participantOutputStream.write(message);
                String sentMessage = getStringMessage(message);
                System.out.println("Server sending: " + sentMessage); //Add thread id to this so we know who gets what message
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendClientSpecificMessage(participantThread client, String messageToSend)
    {

        try {
            TimeUnit.SECONDS.sleep(3); //Used as client.particpantPort doesnt derive correctly if no delays in creation of client thread

            int clientPortNumber = client.participantPort;
            //System.out.println("Portnumber : " +clientPortNumber);

            byte[] message = messageToSend.getBytes();
            client.participantOutputStream.writeInt(message.length);
            client.participantOutputStream.write(message);
            System.out.println("Server Sending to  " +clientPortNumber +": " +messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     }


    public String otherClientPorts(participantThread client)
    {
        List<Integer> participantPorts = new ArrayList<>();
        participantPorts.addAll(this.participantPorts);
        participantPorts.remove(Integer.valueOf(client.participantPort));
        //String port = " " + ;
        String otherClients = convertIntListToString2(client);

        return otherClients;
    }

    public String sendClientOtherVotes(participantThread client, HashMap map)
    {
        List<Integer> participantPorts = new ArrayList<>();
        participantPorts.addAll(this.participantPorts);
        participantPorts.remove(Integer.valueOf(client.participantPort));
        //String port = " " + ;
        String otherClients = convertIntListToString2(client);

        return otherClients;
    }

    public void sendMessageToAllClients(String message)
    {
        for (int i = 0; i < participants.size(); i++)
        {
            String messageToSend = "You are Thread Number: " +participants.get(i).getId();
            sendClientSpecificMessage(participants.get(i),messageToSend);
            sendClientSpecificMessage(participants.get(i),message);

        }
    }



    public String getStringMessage(byte[] message)
    {
        String stringMessage = new String(message, StandardCharsets.UTF_8);
        return stringMessage;
    }

    public String getPortString(int portNumber)
    {
        String portString = Integer.toString(portNumber);
        return portString;
    }

    public int getPort(byte[] message)
    {
        String stringMessage = getStringMessage(message);
        int portNumber = Integer.parseInt(stringMessage.split(" ")[1]);
        System.out.println(portNumber);
        return portNumber;
    }

    public String convertIntListToString()
    {
        String portNumber = new String();

        for (int i = 0; i < participantPorts.size(); i++)
        {
            portNumber += String.valueOf(participantPorts.get(i) +" ");
        }
        return portNumber;
    }

    public String convertIntListToString2(participantThread client) //Correctly provides other port numbers to eachother, need to clean up names...
    {
        String portNumber = new String();
        List<Integer> participantPorts2 = new ArrayList<>();
        participantPorts2.addAll(this.participantPorts);
        participantPorts2.remove(Integer.valueOf(client.participantPort));
        for (int i = 0; i < participantPorts2.size(); i++)
        {
            portNumber += String.valueOf(participantPorts2.get(i) +" ");
        }
        return portNumber;
    }


    public String convertVoteOptionsToString()
    {
        String voteOptionsString = new String();

        for (int i = 0; i < voteOptions.size(); i++)
        {
            voteOptionsString += String.valueOf(voteOptions.get(i) +" ");
        }
        return voteOptionsString;
    }

    private class BarrierReached implements Runnable {
        @Override
        public void run() {
            System.out.println("Barrier reached");
        }
    }

    public String getRandomVote()
    {
        String[] names = {"1", "2"};
        String[] adjs = {"h", "f"};

        String name = names[(int) (Math.random() * names.length)];
        String adj = adjs[(int) (Math.random() * adjs.length)];

        return name +" " +adj;
    }







    class MessagePacket
    {
        private byte[] data;
        private int length;

        public MessagePacket(int len, byte[] aData)
        {
            this.length = len;
            data = new byte[len];
        }
        public int Length()
        {
            return this.length;
        }
        public byte[] Data()
        {
            return this.data;
        }
    }





    class MessageQueue
    {
        private int capacity;

        /**
         * The queue itself, all incoming messages are stored in here.
         */
        private Vector<String> queue = new Vector<String>(capacity);

        /**
         * Constructor, initializes the queue.
         *
         * @param capacity The number of messages allowed in the queue.
         */
        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        /**
         * Adds a new message to the queue. If the queue is full, it waits until a message is released.
         *
         * @param message
         */
        public synchronized void send(String message) {
        }

        /**
         * Receives a new message and removes it from the queue.
         *
         * @return
         */
        public synchronized String receive() {
            return "0";
        }
    }




}