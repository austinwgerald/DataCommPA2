import java.io.*;

/**
 * Created by ubuntu on 3/1/16.
 * Authors: Austin Gerald, Roop Saini
 * Data Communications Programming Assignment 2
 *
 */

public class serial implements java.io.Serializable{
    public serial() {}                                                                                  //Constructor

    /*
     * Function to serialize a packet and return the resulting buffer array
     */
    public byte[] serialize(packet pkt) throws IOException {
        ByteArrayOutputStream oSt = new ByteArrayOutputStream();                                        //New ByteArrayOutputStream
        ObjectOutputStream ooSt = new ObjectOutputStream(oSt);                                          //New ObjectOutputStream of ByteArrayOutputStream
        ooSt.writeObject(pkt);                                                                          //Write the object into the ObjectOutputStream
        ooSt.flush();                                                                                   //Flush the ObjectOutputStream
        byte[] sendBuf = oSt.toByteArray();                                                             //Buffer equals ByteArrayOutputStream data to byte array
        return sendBuf;                                                                                 //Return Buffer
    }

    public packet deserialize(byte[] receiveData) throws IOException, ClassNotFoundException {
        packet pkt;                                                                                     //Initialize empty packet
        ByteArrayInputStream inSt = new ByteArrayInputStream(receiveData);                              //New ByteArrayInputStream of receiveData buffer
        ObjectInputStream oinSt = new ObjectInputStream(inSt);                                          //New ObjectInputStream of ByteArrayInputStream
        pkt = (packet)oinSt.readObject();                                                               //Packet equals ObjectInputStream cast to packet object
        return pkt;                                                                                     //Return packet
    }
}