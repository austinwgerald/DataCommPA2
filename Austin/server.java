
/*****************************************************************************************
 * Austin Gerald - Data Comm - Spring 2016 - awg62
 * Sources:
 * https://systembash.com/a-simple-java-tcp-server-and-tcp-client/
 * http://tutorials.jenkov.com/java-networking/udp-datagram-sockets.html
 * https://systembash.com/a-simple-java-udp-server-and-udp-client/#sthash.V35fQ4a0.dpuf
 ******************************************************************************************/

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;

public class server {

    public static void main(String[] args) throws IOException {
        //System.out.println("server");
        int tcpPort = Integer.parseInt(args[0]);
        //Wait for the initial connection and return the File transfer port number
        int udpPort = tcp(tcpPort);
        //Establish the Datagram socket and recieve the file
        udp(udpPort);

    }

    private static int tcp(int tcpPort) throws IOException {
        //Select the random port for the transfer stage
        Random rand = new Random();
        int serverPort = 65535;
        serverPort = rand.nextInt(64510) + 1024;

        ServerSocket welcomeSocket = new ServerSocket(tcpPort);
        Socket connectionSocket = welcomeSocket.accept();
        BufferedReader inFromClient =
                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        String validate = inFromClient.readLine();

        //Validate that the correct client is connecting via the given String
        if(Integer.parseInt(validate) == 117) {
            System.out.println("Negotiation Detected. Selected random port "+serverPort);
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String serverPortStr = String.valueOf(serverPort);
            //Add newline to the port to prevent deadlock of the client waiting for the newline
            serverPortStr = serverPortStr.concat("\n");
            outToClient.writeBytes(serverPortStr);
            welcomeSocket.setSoTimeout(50);
            connectionSocket.setSoTimeout(50);
            return serverPort;
        }
        else{
            System.out.println("Error connecting...");
            welcomeSocket.close();
            connectionSocket.close();
            System.exit(1);
        }
        return 0;
    }

    private static void udp(int udpPort) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(udpPort);
        byte[] receiveData = new byte[4];
        byte[] sendData = new byte[4];
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("received.txt")));

        while(true)
        {
            //Receive the data
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //System.out.println("Receiving...");
            serverSocket.receive(receivePacket);
            String data = new String( receivePacket.getData());
            //System.out.println("RECEIVED: " + data);

            //EOF condition
            if(data.equals("$EOF")){
                //Send last ack
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = data.toUpperCase();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress, port);
                //System.out.println("Sending Ack...");
                serverSocket.send(sendPacket);
                
                //Close the Socket and the file
                serverSocket.close();
                out.close();
                System.exit(1);
            }
            //Append to file
            out.write(data);

            //Resolve return IP
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = data.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            //System.out.println("Sending Ack...");
            serverSocket.send(sendPacket);

        }
    }
}