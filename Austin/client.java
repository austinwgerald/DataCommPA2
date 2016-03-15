
/*****************************************************************************************
* Austin Gerald - Data Comm - Spring 2016 - awg62
* Sources:
* https://systembash.com/a-simple-java-tcp-server-and-tcp-client/
* http://tutorials.jenkov.com/java-networking/udp-datagram-sockets.html
* https://systembash.com/a-simple-java-udp-server-and-udp-client/#sthash.V35fQ4a0.dpuf
******************************************************************************************/

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class client {
   
    public static void main(String[] args) throws IOException {
        //System.out.println("client");
        // Argument Error Checking
        if(args.length != 3){
            System.out.println("Error. Wrong input format.");
            System.exit(1);
        }
        File file = fileExists(args[2]);
        //Convert IP String to InetAddress
        InetAddress IP = IPtoInet(args[0]);
        //TCP connection that returns the random server port number
        int serverPort = startConnection(IP, args[1]);
        //UDP connection to transfer the files
        udpCConnection(IP, serverPort, file);
    }

    private static int startConnection(InetAddress IP, String sPort) {
        int port = Integer.parseInt(sPort);
        int serverPort = 0;
        String serverPortStr = "Null";
        try {
            Socket tcpSocket = new Socket(IP,port);
            DataOutputStream tcpClientDataOut = new DataOutputStream(tcpSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            //Validation String sent to server
            tcpClientDataOut.writeBytes("117\n");
            serverPortStr = inFromServer.readLine();
            System.out.println("Random Port: " + serverPortStr);
            serverPort= Integer.parseInt(serverPortStr);
            tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverPort;
    }

    private static void udpCConnection(InetAddress IP, int serverPort, File file) throws IOException{

        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = new byte[4];
        byte[] receiveData = new byte[4];
        String str = textInput(file);

        //Send the bits of data
        for(int i =0; i <=(str.length()/4);i++){
            String strpacket = null;
            if (str.length() <= (i * 4 + 4)) {
                strpacket = str.substring((i*4),str.length());
            }else{
                strpacket = str.substring((i*4),(i*4+4));
            }
            sendData = strpacket.getBytes();
            //Sending Data
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, serverPort);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            //Receiving Ack
            //System.out.println("Receiving Ack...");
            clientSocket.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData());
            System.out.println(modifiedSentence);
        }

        //Terminate Transfer
        byte[] eofByte = "$EOF".getBytes();
        DatagramPacket eofPacket = new DatagramPacket(eofByte, eofByte.length, IP, serverPort);
        clientSocket.send(eofPacket);
        DatagramPacket eofAckPacket = new DatagramPacket(receiveData, receiveData.length);

        //Receive final ack
        //System.out.println("Receiving Ack...");
        clientSocket.receive(eofAckPacket);
        String finalAck = new String(eofAckPacket.getData());
        System.out.println(finalAck);
        clientSocket.close();

    }

    private static InetAddress IPtoInet(String IP){
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return IPAddress;
    }
    private static File fileExists(String filename){
        //Source: http://www.java2s.com/Code/Java/File-Input-Output/Readfilecharacterbycharacter.htm
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println(filename + " does not exist.");
            System.exit(1);
        }
        if (!(file.isFile() && file.canRead())) {
            System.out.println(file.getName() + " cannot be read from.");
            System.exit(1);
        }
        return file;
    }
    private static String textInput(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";
        while((line = reader.readLine()) != null) {
            text += line + "\n";
        }
        reader.close();
        return text;
    }
}