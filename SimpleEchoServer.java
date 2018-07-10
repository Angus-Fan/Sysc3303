

//SimpleEchoServer.java
//This class is the server side of a simple echo server based on
//UDP/IP. The server receives from a client a packet containing a character
//string, then echoes the string back to the client.
//Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class SimpleEchoServer {
  byte[] resize = new byte[0];
  byte[] msg;
  final String path="C:\\Users\\michaelwang3\\Desktop\\server\\";
  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendSocket, receiveSocket;
  String fileName;
  InputStream is = null;
  OutputStream os = null;
  int blockCount=0;
  boolean isReading=false;
  public SimpleEchoServer()
  {
    try {
      // Construct a datagram socket and bind it to any available 
      // port on the local host machine. This socket will be used to
      // send UDP Datagram packets.
      sendSocket = new DatagramSocket();
      
      // Construct a datagram socket and bind it to port 5000 
      // on the local host machine. This socket will be used to
      // receive UDP Datagram packets.
      receiveSocket = new DatagramSocket(69);
      
      // to test socket timeout (2 seconds)
      //receiveSocket.setSoTimeout(2000);
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    } 
  }
  
  public void receiveAndEcho()
  {
    // Construct a DatagramPacket for receiving packets up 
    // to 100 bytes long (the length of the byte array).
    
    byte data[] = new byte[512];
    receivePacket = new DatagramPacket(data, data.length);
    System.out.println("Server: Waiting for Packet.\n");
    
    // Block until a datagram packet is received from receiveSocket.
    try {        
      System.out.println("Waiting..."); // so we know we're waiting
      receiveSocket.receive(receivePacket);
    } catch (IOException e) {
      System.out.print("IO Exception: likely:");
      System.out.println("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
    }
    
    // Process the received datagram.
    System.out.println("Server: Packet received:");
    System.out.println("From host: " + receivePacket.getAddress());
    System.out.println("Host port: " + receivePacket.getPort());
    int len = receivePacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: " );
    
    // Form a String from the byte array.
    String received = new String(data,0,len);   
    System.out.println(received + "\n");
    getOpcode();
    if(blockCount==0||isReading)
      parseData();
    
    if(!isReading)
    {
      if(blockCount!=0)
      {
        writting(data);     
      }    
      constructArray();
      blockCount++;
      
      sendPacket = new DatagramPacket(msg, msg.length,
                                      receivePacket.getAddress(), 23); 
    }    
    else
    {
    	int numPack=0;    //finding out how many time need to send the whole file
        int blockCount=0;
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
        
        blockCount=0;
        
        try {
          is = new FileInputStream((path+fileName));
          
          for(int i=0; i<numPack;i++)
          {
            is.read(fileData);
            for(int j=0; j<fileData.length;j++)
            {
            	fullMsg[i][j]=fileData[j]; 
            }
          }
        }
        catch(Exception e) {
          
          // if any I/O error occurs
          e.printStackTrace();
        }
        
        System.out.println(numPack+" block:");
        for(int j=0;j<fullMsg[4].length;j++)
        {
        	System.out.print((char)fullMsg[4][j]);
        }
        System.out.println(); 
        
        
        sending(fullMsg[blockCount]);
        blockCount++;
        while(blockCount!=numPack)
        {
        	receiving(data);
         
          sending(fullMsg[blockCount]);
          blockCount++;
        }
        receiving(data);
        msg=new byte[1];
        sending(msg);
        
        
    }
    /*
    try {
      Thread.sleep(0);
    } catch (InterruptedException e ) {
      e.printStackTrace();
      System.exit(1);
    }*/
    
    
    // Create a new datagram packet containing the string received from the client.
    
    // Construct a datagram packet that is to be sent to a specified port 
    // on a specified host.
    // The arguments are:
    //  data - the packet data (a byte array). This is the packet data
    //         that was received from the client.
    //  receivePacket.getLength() - the length of the packet data.
    //    Since we are echoing the received packet, this is the length 
    //    of the received packet's data. 
    //    This value is <= data.length (the length of the byte array).
    //  receivePacket.getAddress() - the Internet address of the 
    //     destination host. Since we want to send a packet back to the 
    //     client, we extract the address of the machine where the
    //     client is running from the datagram that was sent to us by 
    //     the client.
    //  receivePacket.getPort() - the destination port number on the 
    //     destination host where the client is running. The client
    //     sends and receives datagrams through the same socket/port,
    //     so we extract the port that the client used to send us the
    //     datagram, and use that as the destination port for the echoed
    //     packet.
    
    
    
    
    
    
    
  }
  public void sending(byte[] data)
  {
    
    try {
    	sendPacket = new DatagramPacket(data, data.length,
                receivePacket.getAddress(), 23); 
        sendSocket.send(sendPacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
      
      System.out.println("Server: packet sent");
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
      
    	receiveSocket.receive(receivePacket);
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("data receivied");
    
  }
  public void constructArray() {
    
    msg = new byte[4];
    msg[0] = (byte)0;
    if(isReading)
      msg[1] = (byte)3;
    else
      msg[1] = (byte)4;
    if(!isReading)
    {
      if(blockCount<10)
      {
        msg[2] = (byte)0;
        msg[3] =(byte)blockCount;
      }
      else
      {
        msg[2] = (byte)(int)(blockCount%10);
        msg[3] =(byte)blockCount;
      }
      
    }
    
    byte[] dataGramPackage = new byte[4];
    dataGramPackage[0] = msg[0];
    dataGramPackage[1] = msg[1];
    dataGramPackage[2] = msg[2];
    dataGramPackage[3] = msg[3];
    
    msg = dataGramPackage;
    
    
  }
  
  private void getOpcode()
  {
    if(receivePacket.getData()[1] == 1)
      isReading = true;
    else
      isReading = false;
  }
  
  private void parseData() {
    int i  = 2;
    int len = 2;
    // Figures out how long the filename is
    while(receivePacket.getData()[i] != (byte)0)
    {
      len++;
      i++;
    }
    fileName = new String(receivePacket.getData(),2,len-2);
    
  }
  
  public static void main( String args[] )
  {
    SimpleEchoServer c = new SimpleEchoServer();
    //while(true) {
      c.receiveAndEcho();
    //}
    
  }
  
}

