import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
* Created by ubuntu on 3/1/16.
* Authors: Austin Gerald, Roop Saini
* Client side of the Data Communications Programming Assignment 2
*
*/
public class client {
    public static int window = 8;                                                                        //Window size N for Go-Back-N
    public static int defaultPacketSize = 30;                                                            //30 Character default
    public static int currentFileIndex = 0;                                                              //Global for current index of file String

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Hello client");
        InetAddress IPAddress = InetAddress.getByName(args[0]);                                           //Resolve server address.
        int sendPort = Integer.parseInt(args[1]);                                                         //Get Port to which to send data
        int ackPort = Integer.parseInt(args[2]);                                                          //Get Acknowledgement port
        String fileContents = getFileData(args[3]);                                                       //Get the file name and verify it's validity.
        DatagramSocket clientSocket = new DatagramSocket();                                               //Create sending data datagram socket.
        DatagramSocket ackSocket = new DatagramSocket(ackPort);                                           //Create ack receiving port.
        ackSocket.setSoTimeout(800);                                                                      //Set timer for receiving ack timeout
        File ackFile = new File("ack.log");                                                               //Open File ack.log
        File seqNumFile = new File("seqNum.log");                                                         //Open File seqNum.log
        FileOutputStream ackStream = new FileOutputStream(ackFile);                                       //Ack stream to write to file
        FileOutputStream seqStream = new FileOutputStream(seqNumFile);                                    //SeqNum stream to write to file

        while (true)
        {
            ArrayList<packet> windowContents = makePackets(fileContents);                                 //Function call to create the window of packets
            if (dispatch(windowContents, clientSocket, ackSocket, IPAddress, ackStream,seqStream))        //If done sending and receiving acks then break
            {
                break;
            }
        }
        ackStream.close();                                                                                  //Close ackStream
        seqStream.close();                                                                                  //Close seqStream
        clientSocket.close();                                                                              //close datagram socket.
    }

    /*
     * Function to Call send and receive functions and determine the current String index within the file.
     * Also determines if the EOT ack has been received and writes out the seqNums and acks to log files.
     */
    private static boolean dispatch
    (ArrayList<packet> windowContents, DatagramSocket clientSocket, DatagramSocket ackSocket, InetAddress ipAddress, FileOutputStream ackStream, FileOutputStream seqStream)
            throws IOException, ClassNotFoundException
    {
        boolean endTransaction = false;                                                                 //Instantiation of EOT bool to false
        int sent = 0;                                                                                   //Sent and ack received packet count
        for (int i = 0; i < windowContents.size(); i++)                                                 //Send all packets in the ArrayList of Packets
        {
            packet pkt = windowContents.get(i);
            sendPacket(clientSocket, pkt, ipAddress);                                                   //Function call to serialize & send individual packets
            seqStream.write(pkt.getSeqNum());                                                           //Write seqNum to seqNum log file
        }

        packet ack = null;                                                                              //Initialize empty packet to store ack packets
        for (int i = 0; i < windowContents.size(); i++)
        {
            packet latestAck = receiveAck(ackSocket);                                                   //Receive all acks
            if (latestAck !=null )                                                                      //If acks received
            {
                ack = latestAck;                                                                        //Latest ack placed into placeholder
                System.out.println(String.format("RECEIEVED ACK: %d.", ack.getSeqNum()));               //Print ack seqNum
                ackStream.write(ack.getSeqNum());                                                       //Write acks to ack log file
                sent = ack.getSeqNum() + 1;                                                             //Sent equals cumulative ack seqNum + 1
                if (ack.getSeqNum() == windowContents.size() - 1)                                       //If ack is the last ack of the window:
                {
                    if (ack.getType() == 2) endTransaction = true;                                      //If ack type is EOT type set endTransaction bool to true
                }
            }
            else                                                                                        //Else no acks received; break
            {
                break;
            }
        }

        int indexRewindForLostPackets = 0;                                                              //Initialize index rewind counter to 0
        for (int i = sent; i < windowContents.size(); i++)                                              //For every packet not acked
        {
            indexRewindForLostPackets += windowContents.get(i).getLength();                             //Sum total character indexes to rewind
        }

        if (sent < windowContents.size()) currentFileIndex -= indexRewindForLostPackets;                //If sent less than list size then rewind the current index
        System.out.println(String.format("current index: %d.", currentFileIndex));

        return endTransaction;                                                                          //Return EOT bool
    }

    /*
     * Function to Serialize and Send packet
     */
    private static void sendPacket(DatagramSocket clientSocket, packet pkt, InetAddress ipAddress) throws IOException {
        serial srl = new serial();                                                                      //New instance of serial object
        byte[] sendBuf = srl.serialize(pkt);                                                            //Serialize the packet object
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, ipAddress, 7000);       //Construct DatagramPacket to send
        clientSocket.send(sendPacket);                                                                  //Send packet

    }

    /*
     * Function to receive ack packets. Times out if no ack received after listening for 800ms.
     */
    private static packet receiveAck(DatagramSocket ackSocket) throws IOException, ClassNotFoundException {
        packet ack = null;                                                                              //Initialize empty return packet
        try
        {
            byte[] receiveData = new byte[1024];                                                        //Buffer for receive data
            serial srl = new serial();                                                                  //New instance of serial object
            DatagramPacket ackPacket = new DatagramPacket(receiveData, receiveData.length);             //Create receive packet pointed at receiveData buffer
            ackSocket.receive(ackPacket);                                                               //Receive Ack
            ack = srl.deserialize(receiveData);                                                         //Deserialize the receivedData into packet object
            ack.printContents();                                                                        //Print contents of the received ack packet
        }
        catch (SocketTimeoutException e)                                                                //Timeout exception.
        {
            System.out.println("Timeout reached! Resending packets.");
        }
        return ack;                                                                                     //Return null if no packets received, else return last received ack
    }

    /*
     * Function to create and populate an ArrayList of window N packets
     */
    private static ArrayList<packet> makePackets(String fileContents)
    {
        ArrayList<packet> packets = new ArrayList<packet>();                                            //Initialize new ArrayList
        boolean appendEOT = false;                                                                      //Bool Flag for EOT

        for (int i = 0;i < window;i++)                                                                  //For 0 -> window size create packet
        {
            int packetSize = defaultPacketSize;                                                         //Packet size counter
            appendEOT = (currentFileIndex >= fileContents.length());                                    //If currentIndex >= length of fileContents set EOT flag

            if (appendEOT)                                                                              //If flag set
            {
                packets.add(new packet(3, i, 0, null));                                                 //Add EOT packet to list and break
                break;
            }

            if (currentFileIndex + packetSize > fileContents.length())                                  //If current Index + packet String Length > length of fileContents then
            {                                                                                           // Last packet will be the remainder text.
                packetSize = fileContents.length() - currentFileIndex;                                  //Set packetSize = length of fileContents - current Index
            }

            //Add packet to list. Set: type to data packet, packetSize, data = substring from current Index to calculated end of file
            packets.add(new packet(1, i, packetSize, fileContents.substring(currentFileIndex, currentFileIndex + packetSize)));
            System.out.println(String.format("current index: %d. Packet content: %s", currentFileIndex, packets.get(i).getData()));

            currentFileIndex += packetSize;                                                             //Set current Index to start of next index
        }
        return packets;                                                                                 //Return ArrayList of packets
    }
    /*
     * Function to return the data from the input file as a String
     */
    private static String getFileData(String filename) throws IOException
    {
        File file = new File(filename);                                                                 //New file to read input file into
        FileInputStream fis = new FileInputStream(file);                                                //Open file in FileInputStream
        byte[] data = new byte[(int) file.length()];                                                    //Create buffer to read data into
        fis.read(data);                                                                                 //Read data into buffer
        fis.close();                                                                                    //Close FileInputStream
        return new String(data, "UTF-8");                                                               //Create and Return Data as String from Buffer
    }
}
