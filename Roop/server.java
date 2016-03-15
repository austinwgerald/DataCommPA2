//Roop Karman Saini
//rss233

import java.util.Random;
import java.net.*;
import java.io.*;

public class server
{
    public static String reqNegotiation = "117";                                    //Constant for negotiation request.
    public static String reqEOF = "^%&*";                                           //Constant for EOF notification.

    public static void main(String[] args) throws IOException
    {
        int nPort = Integer.parseInt(args[0]);                                      //Negotiation Port argument
        int rPort = 0;                                                              //Transmission () port (random)
        ServerSocket serverSocket = new ServerSocket(nPort);                        //Create socket for negotiation port.
        try
        {
            while (true)
            {
                Socket socket = serverSocket.accept();                              //Infinitely wait for incoming connection request.
                try
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));              
                    while (true)
                    {
                        if (input.readLine().equals(reqNegotiation))                                                ////If negotiation request detected, generate new rPort.
                        {
                            rPort = new Random().nextInt(65535) + 1024; 
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);                      //Send rPort over TCP back to client.
                            out.println(Integer.toString(rPort));                                                  
                            break;
                        }
                    }
                    if (rPort != 0) break;                                              //if rPort set, break out of negoatiation loop.
                }
                finally
                {
                    System.out.println(String.format("\nNegotiation detected. Selected random port %d\n", rPort));              //print negotiation detection.
                    socket.close();
                }
            } 
        }
        finally
        {
            serverSocket.close();                   //close original socket connection.
        }

        DatagramSocket datagramSocket = new DatagramSocket(rPort);                                      //create datagram socket on transmission port.
        FileOutputStream fileOutputStream = new FileOutputStream("received.txt");                       //open file to wrte in
        byte[] receiveData = new byte[4];                                                               
        byte[] sendData = new byte[4];
        
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);         
            datagramSocket.receive(receivePacket);                                                      //receieve packet via UDP
            String sentence = new String( receivePacket.getData());                                    
            
            if (sentence.equals(reqEOF)) break;                                                         //if EOF notification received, break

            fileOutputStream.write(sentence.getBytes());                                                //else, write data into file. 
            InetAddress IPAddress = receivePacket.getAddress();                 
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);         //Capitalize letters, send response to server.
            datagramSocket.send(sendPacket);
        }
        datagramSocket.close();                 //close datagram socket.
        fileOutputStream.close();               //close file.
    }
}
