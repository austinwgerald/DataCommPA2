//Roop Karman Saini
//rss233

import java.net.*;
import java.io.*;

public class client 
{
    public static String reqNegotiation = "117";                                //Constant for negotiation request.
    public static String reqEOF = "^%&*";                                       //Constant for EOF notification.

    public static void main(String[] args) throws IOException 
    {
        String serverAddress = args[0];                                         //Server address argument.
        int nPort = Integer.parseInt(args[1]);                                  //Negotiation port arguement.
        int rPort = 0;                                                          //Transmission port (random)
        String filename = args[2];                                              //Filename argument.

        Socket socket = new Socket(serverAddress, nPort);                       //Create new socket for server and port provided.
        try
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);                              
            out.println(reqNegotiation);                                        //Send negotiation request via TCP.

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));      
            String inputLine;

            while (true)
            {
                if ((inputLine = input.readLine()) != null)                 //Infinitely wait for response.
                {
                    rPort = Integer.parseInt(inputLine);                    //On response, parse and store tranmission port number.
                    break;
                }
            }
        }
        finally
        {
            socket.close();                                                 //Close negotiation socket.
        }

        DatagramSocket clientSocket = new DatagramSocket();                                            //Create datagram socket.
        FileInputStream fileInputStream = new FileInputStream(filename);                               //Open file to read.
        InetAddress IPAddress = InetAddress.getByName(serverAddress);                                  //Resolve server address.
        byte[] sendData = new byte[4];                                                                 //initialize send and receieve buffers (4 bytes)
        byte[] receiveData = new byte[4];
    
        System.out.println(String.format("\nRandom Port: %d\n", rPort));                                //Print Transmission number.
        int dataRead;
        
        while (true)
        {
            dataRead = fileInputStream.read(sendData);                                                  //Read data from file/
            if (dataRead <= 0)
            {
                sendData = reqEOF.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, rPort);  //If no data read (EOF), send EOF notification as a UDP packet. 
                clientSocket.send(sendPacket);                                                          
                break;
            }
            else
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, rPort);           
                clientSocket.send(sendPacket);
                sendData = new byte[4];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);             //else, send UDP packet (file chunk) to target server/port.
                clientSocket.receive(receivePacket);
                String receivedString = new String(receivePacket.getData());
                System.out.println(receivedString);
            }    
        }
        fileInputStream.close();                                    //close file.
        clientSocket.close();                                       //close datagram socket.
    }
}