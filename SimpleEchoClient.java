// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class SimpleEchoClient {

   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendReceiveSocket;
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
 
      String s = "Anyone there?";
      System.out.println("Client: sending a packet containing:\n" + s);

      // Java stores characters as 16-bit Unicode values, but 
      // DatagramPackets store their messages as byte arrays.
      // Convert the String into bytes according to the platform's 
      // default character encoding, storing the result into a new 
      // byte array.

      //byte msg[] = s.getBytes();
      
      if(readOrWrite!=10) {
      constructArray(readOrWrite%2+1,"Test.txt","netascii");
      }
      else {
    	  constructArray(9,"Test.txt","netascii");
      }
      
     
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

   public static void main(String args[])
   {
      SimpleEchoClient c = new SimpleEchoClient();
      for(int i=0;i<11;i++) {      
    	  System.out.println("Rotation : " + i);
    	  c.sendAndReceive(i);
      
      }
      c.sendReceiveSocket.close();
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
       System.out.print("Package in bytes: ");
       for(int x = 0;x<dataGramPackage.length;x++) {
    	   System.out.print(dataGramPackage[x]);
       }
       System.out.println("");
       String outgoing = new String(msg,0,msg.length);  
       System.out.print("Package in string: "+ outgoing);
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
