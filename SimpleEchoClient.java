// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class SimpleEchoClient {
  byte[] resize = new byte[0];
  final String path="C:\\Users\\michaelwang3\\Desktop\\";
  final String fileName="Test.txt";
  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendReceiveSocket;
  InputStream is = null;
  OutputStream os = null;
  byte[] msg;
  
  public SimpleEchoClient()
  {
    try {
      // Construct a datagram socket and bind it to any available 
      // port on the local host machine. This socket will be used to
      // send and receive UDP Datagram packets.
      sendReceiveSocket = new DatagramSocket();
      
    } catch (SocketException se) {   // Can't create the socket.
      se.printStackTrace();
      System.exit(1);
    }     
    
  }
  
  public void sendReadAndReceive()
  {
    
    
    System.out.println("Client: sending a packet containing:\n" );
    constructArray(1,"Test.txt","netascii");
    sending(msg);   
    System.out.println("Client: Packet sent.\n");
    
    byte data[] = new byte[512];
    
    
    
    receiving(data);
    while(data.length>510)
    {
    	writting(data);
    	System.out.println("reading..");
    	 msg=new byte[4];
         msg[0]=0;
         msg[1]=4;
         msg[2]=0;
         msg[3]=0;
        sending(msg);  
        receiving(data);
    }
    printInfoReceived(receivePacket,data);
    
  } 
  public void sendWriteAndReceive()
  {    
    int numPack=0;    //finding out how many time need to send the whole file

    
    byte[] fileData  = new byte[512];
    System.out.println("Reading file from: "+(path+fileName));
    try {
      is = new FileInputStream((path+fileName));
      int    bytesRead = is.read(fileData);         
      while(bytesRead != -1) {    
        numPack++;
        fileData = new byte[512];
        bytesRead = is.read(fileData);
      }
      is.close();
    }
    catch(Exception e) {
      
      // if any I/O error occurs
      e.printStackTrace();
    } 
    System.out.println("Packages need to send "+numPack);
    byte[][] fullMsg=new byte[numPack][512];
    
    
    
    try {
      is = new FileInputStream((path+fileName));
      
      for(int i=0; i<numPack;i++)
      {
        is.read(fileData);
        fullMsg[i]=fileData;        
      }
      is.close();
    }
    catch(Exception e) {
      
      // if any I/O error occurs
      e.printStackTrace();
    }
    
    
    
    System.out.println("Client: sending a packet containing:\n");
    constructArray(2,"Test.txt","netascii");
    sending(msg);
    
    System.out.println("Client: Packet sent.\n");
    
    
    byte data1[] = new byte[100];
    receiving(data1);
    
    int blockNum=0;
    if(data1[1]==(byte)4)
    {
      if(data1[2]!=0)
      {
        blockNum=(int)data1[2]*10+(int)data1[3];
      }
      else
      {
        blockNum=(int)data1[3];
      }
    }
    
    
    
    while(blockNum!=numPack)
    {
      
      sending(fullMsg[blockNum]);
      blockNum++;
    }
  } 
  
  
  public void sending(byte[] data)
  {
    
    try {
      sendPacket = new DatagramPacket(data, data.length,
                                      InetAddress.getLocalHost(), 23);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      System.exit(1);
    }
    try {
      sendReceiveSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    printInfoToSend(sendPacket); 
  }
  
  public void writting(byte[] data)
  {
    System.out.println("data len="+data.length);
    byte[] newData = resize;
    resize = new byte[resize.length + data.length];
    System.arraycopy(newData, 0, resize, 0, newData.length);
    System.arraycopy(data, 0, resize, newData.length, data.length); 
    try
    {
      FileOutputStream out = new FileOutputStream(path+fileName);
      out.write(resize); 
      
      out.close();
    }
    catch(Exception e) {
      
      // if any I/O error occurs
      e.printStackTrace();
    }
    
    
  }
  public void receiving (byte[] data1)
  {
    
    receivePacket = new DatagramPacket(data1, data1.length);
    
    try {
      
      sendReceiveSocket.receive(receivePacket);
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    printInfoReceived(receivePacket,data1);
    
  }
  public static void main(String args[])
  {
    SimpleEchoClient c = new SimpleEchoClient();
    
    //c.sendAndReceive(1);
    c.sendReadAndReceive();
    
    //c.sendReceiveSocket.close();
  }
  
 
  public void printInfoToSend(DatagramPacket pack) {
    /*
     * 
     System.out.println("To host: " + pack.getAddress());
     System.out.println("Destination host port: " + pack.getPort());
     
     System.out.println("Length: " + len);
     
     */
    int len = getLen(pack);
    System.out.println("Client: Sending packet:");
    System.out.print("Containing: ");
    System.out.println(new String(pack.getData(),0,len)); // or could print "s"
    System.out.println("Sending done!");
  }
  public void printInfoReceived(DatagramPacket pack,byte[] dataByte) {
    System.out.println("Client: Packet received:");
    //System.out.println("From host: " + pack.getAddress());
    //System.out.println("Host port: " + pack.getPort());
    int len = getLen(pack);
    //System.out.println("Length: " + len);
    System.out.print("Containing: ");
    // Form a String from the byte array.
    String received = new String(dataByte,0,len);   
    System.out.println(received);
    
    
  }
  
  public int getLen(DatagramPacket pack) {
    return pack.getLength();
  }
  public void constructArray(int request,String file,String mode) {
    byte zero[] = new byte[1];
    byte fileByte [] = file.getBytes();
    
    byte modeByte[] = mode.getBytes();
    msg = new byte[zero.length + zero.length + 2 + modeByte.length + fileByte.length];
    msg[0] = (byte)0;
    msg[1] = (byte)request;
    /*
     System.out.println("First byte" + msg[0]);
     System.out.println("Second byte" + msg[1]);
     System.out.println("Filename" + fileByte);
     System.out.println("Zero Byte" + msg[0]);
     System.out.println("NetAscii " + modeByte);
     System.out.println("Zero byte" + msg[0]);
     */
    byte[] dataGramPackage = new byte[2 + fileByte.length + 1 + modeByte.length + 1];
    dataGramPackage[0] = msg[0];
    dataGramPackage[1] = msg[1];
    for(int i = 0 ;i < fileByte.length;i++) {
      dataGramPackage[2+i] = fileByte[i];
    }
    dataGramPackage[fileByte.length+2] = msg[0];
    for(int i = 0 ;i < modeByte.length;i++) {
      dataGramPackage[3+fileByte.length+i] = modeByte[i];
    }
    dataGramPackage[fileByte.length+2] = msg[0];
    /*System.out.print("Package in bytes: ");
     for(int x = 0;x<dataGramPackage.length;x++) {
     System.out.print(dataGramPackage[x]);
     }
     System.out.println("");
     String outgoing = new String(msg,0,msg.length);  
     System.out.print("Package in string: "+ outgoing);*/
    /*
     // System.arraycopy(fileByte, 0, data, fileByte.length);
     System.arraycopy(fileByte,0,msg,2+fileByte.length,fileByte.length);
     System.arraycopy(zero, 0, msg, 2 + fileByte.length, zero.length);
     System.arraycopy(modeByte, 0, msg, 2 + fileByte.length+ zero.length, modeByte.length);
     System.arraycopy(zero, 0, msg,2 + fileByte.length+ zero.length + modeByte.length, zero.length); 
     for(int x = 0;x<msg.length;x++) {
     System.out.print(msg[x]);
     }*/
    msg = dataGramPackage;
    
  }
}
