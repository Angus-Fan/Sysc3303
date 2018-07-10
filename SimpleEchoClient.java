// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class SimpleEchoClient {
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

   public void sendAndReceive(int readOrWrite)
   {
      // Prepare a DatagramPacket and send it via sendReceiveSocket
      // to port 5000 on the destination host.
 
      String s = "";
      System.out.println("Client: sending a packet containing:\n" + s);

      // Java stores characters as 16-bit Unicode values, but 
      // DatagramPackets store their messages as byte arrays.
      // Convert the String into bytes according to the platform's 
      // default character encoding, storing the result into a new 
      // byte array.

      //byte msg[] = s.getBytes();
      
     /* if(readOrWrite!=10) {
    	  
 	     constructArray(readOrWrite%2+1,readFromFile(),"netascii");
      }
      else {
    	  constructArray(9,readFromFile(),"netascii");
      }*/
      
     
      // Construct a datagram packet that is to be sent to a specified port 
      // on a specified host.
      // The arguments are:
      //  msg - the message contained in the packet (the byte array)
      //  msg.length - the length of the byte array
      //  InetAddress.getLocalHost() - the Internet address of the 
      //     destination host.
      //     In this example, we want the destination to be the same as
      //     the source (i.e., we want to run the client and server on the
      //     same computer). InetAddress.getLocalHost() returns the Internet
      //     address of the local host.
      //  5000 - the destination port number on the destination host.
      try {
         sendPacket = new DatagramPacket(msg, msg.length,
                                         InetAddress.getLocalHost(), 23);
      } catch (UnknownHostException e) {
         e.printStackTrace();
         System.exit(1);
      }
      printInfoToSend(sendPacket);    

      // Send the datagram packet to the server via the send/receive socket. 

      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Client: Packet sent.\n");

      // Construct a DatagramPacket for receiving packets up 
      // to 100 bytes long (the length of the byte array).

      byte data[] = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);

      try {
         // Block until a datagram is received via sendReceiveSocket.  
         sendReceiveSocket.receive(receivePacket);
      } catch(IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      // Process the received datagram.
      printInfoReceived(receivePacket,data);
      
      // We're finished, so close the socket.
      //sendReceiveSocket.close();
   } 
   public void sendWriteAndReceive()
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
	  
	  
	  
	  try {
	 	  is = new FileInputStream((path+fileName));
	 	  
	      for(int i=0; i<numPack;i++)
	      {
	    	  is.read(fileData);
		      fullMsg[i]=fileData;		      
	      }
	  }
	  catch(Exception e) {
	         
	         // if any I/O error occurs
	         e.printStackTrace();
	  }
	  
	  
	  
	  System.out.println("Client: sending a packet containing:\n");
	  constructArray(2,"Test.txt","netascii");
      try {
         sendPacket = new DatagramPacket(msg, msg.length,
                                         InetAddress.getLocalHost(), 23);
      } catch (UnknownHostException e) {
         e.printStackTrace();
         System.exit(1);
      }
      printInfoToSend(sendPacket);    
      
      
      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

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
      c.sendWriteAndReceive();
      
      //c.sendReceiveSocket.close();
   }
   
   private byte[] readFromFile()
   {
	  System.out.println("Reading file from: "+path);
	  byte[] data  = new byte[256];
	  try {
 		 is = new FileInputStream(path);
 		 int    bytesRead = is.read(data);         
         while(bytesRead != -1) {
        	 System.out.println("lenght= "+bytesRead);
        	 for(int i=0;i<data.length;i++) {System.out.print((char)data[i]);}        	 
        	 data      = new byte[256];
        	  bytesRead = is.read(data);
        	  System.out.println();
        	}
         is.close();
         System.out.println("Reading done!");
 	  }
 	  catch(Exception e) {
	         
	         // if any I/O error occurs
	         e.printStackTrace();
 	  } 
	  return data;
   }
   public void printInfoToSend(DatagramPacket pack) {
	   /*
	      
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
