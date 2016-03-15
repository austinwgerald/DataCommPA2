import java.io.*;
import java.net.*;

/**
 * Created by ubuntu on 3/1/16.
 * Authors: Austin Gerald, Roop Saini
 * Server side of the Data Communications Programming Assignment 2
 *
 */
public class server {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Hello server");

        InetAddress IP = InetAddress.getByName(args[0]);                                                    //Resolve server address.
        int listenPort = Integer.parseInt(args[1]);                                                         //Get port on which to listen for incoming data
        int sendAckPort = Integer.parseInt(args[2]);                                                        //Get port on which to send acks
        File received = new File(args[3]);                                                                  //Get output filename
        DatagramSocket serverSocket = new DatagramSocket(listenPort);                                       //create datagram socket on transmission port.
        DatagramSocket ackSocket = new DatagramSocket();                                                    //Create datagram socket on acknowledgement port.
        FileOutputStream textDataStream = new FileOutputStream(received);                                   //open stream to write data to received file
        FileOutputStream arrivalStream = new FileOutputStream("arrival.log");                               //Open Stream to write arrival packet seqNums to log file
        byte[] receiveData = new byte[1024];                                                                //Receive Data Buffer
        packet previousPkt = null;                                                                          //Initialize empty packet for previous Packet
        String lastDataWritten = null;                                                                      //Initialize String for last data written

        while(true)
        {
            serial srl = new serial();
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);             //Create Receive packet initialized and data set to receiveData buffer
            serverSocket.receive(receivePacket);                                                            //receive packet via UDP
            packet pkt = srl.deserialize(receiveData);                                                      //Deserialize the received byte buffer data
            arrivalStream.write(pkt.getSeqNum());                                                           //Write seqNum to arrival log file
            boolean endTransaction = (pkt.getType() == 3);                                                  //If packet is EOT type then set endTransaction to True
            if (!isPckInOrder(previousPkt, pkt))                                                            //If packets not in order:
            {
                previousPkt = null;                                                                         //Set previous packet equal null
            }

            if (isFirstPacketInWindow(pkt) || isPckInOrder(previousPkt, pkt))                               //If the packet received is the first in the window or if the ack is in order
            {
                sendAck(IP, sendAckPort, ackSocket, srl, pkt, endTransaction);                              //Function call to Send ack packet
                previousPkt = pkt;                                                                          //Set previous packet = to received packet or nul
                if (endTransaction) break;                                                                  // if EOF notification received, break
                if (!pkt.getData().equals(lastDataWritten))                                                 //if packet data != last written data
                {
                    textDataStream.write(pkt.getData().getBytes());                                         //else, write data into file.
                    lastDataWritten = pkt.getData();                                                        //Save last written data for reference

                    System.out.println(String.format("DATA APPENDING: %s, FOR SEQ NO: %d", pkt.getData(), pkt.getSeqNum()));
                }
            }

        }
        serverSocket.close();                                                                               //close datagram sockets
        ackSocket.close();                                                                                  //Close Ack Socket
        textDataStream.close();                                                                             //close file.
        arrivalStream.close();                                                                              //Close arrival log file
    }

    /*
     * Function to determine if the packet received is the first packet in the new window
     */
    private static boolean isFirstPacketInWindow(packet pkt) {
        return (pkt.getSeqNum() == 0);// && (previousPkt.getSeqNum() > 0);
    }

    /*
     * Function to determine if the packet received is the next expected packet
     */
    private static boolean isPckInOrder(packet previousPkt, packet pkt)
    {
        if (previousPkt != null)
        {
            return pkt.getSeqNum() == (previousPkt.getSeqNum() + 1);                                        //Return true if the ack seqNum equals the previous ack seqNum+1
        }
        return false;
    }
    /*
     * Function to serialize and send the ack packet back to the client
     */
    private static void sendAck(InetAddress IP, int sendAckPort, DatagramSocket ackSocket, serial srl, packet pkt, boolean end) throws IOException {
        byte[] sendData;                                                                                            //Empty buffer for the serialized packet
        packet ack = end ? new packet (2, pkt.getSeqNum(), 0, null) : new packet(0, pkt.getSeqNum(), 0, null);      //If EOT true create EOT packet; else create standard ack packet
        sendData = srl.serialize(ack);                                                                              //Serialize the packet
        DatagramPacket ackPacket = new DatagramPacket(sendData, sendData.length, IP, sendAckPort);                  //Create Datagram
        ackSocket.send(ackPacket);                                                                                  //Send ack to client
    }
}
