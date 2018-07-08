import java.io.*;
import java.net.*;


public class InterHost {
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket, sendReceiveSocket;
	int clientPort;
	int portToSend;
	boolean fromClient = false;
	
	public InterHost() {
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(23);
		}
		catch (SocketException se){
			se.printStackTrace();
			System.exit(1);			
		}
	}
	
	public void receiveAndSend() {
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data,data.length);
		
		System.out.println("Created receive packet will attempt wait");
		
		try {
			System.out.println("we are now waiting");
			receiveSocket.receive(receivePacket);
			
		}
		catch(IOException e) {
			System.out.println("IOException on waiting");
			System.exit(1);
		}
		if(!fromClient) {
			clientPort = receivePacket.getPort();
			fromClient = true;
		}
		portToSend = flipPort(receivePacket);
		
		printInfoReceived(receivePacket,data);
	      
	      // Slow things down (wait 2 seconds)
	      try {
	          Thread.sleep(2000);
	      } catch (InterruptedException e ) {
	          e.printStackTrace();
	          System.exit(1);
	      }
	      
	      sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                  receivePacket.getAddress(), portToSend);

		printInfoToSend(sendPacket);
		// or (as we should be sending back the same thing)
		// System.out.println(received); 
		
		// Send the datagram packet to the client via the send socket. 
		try {
		sendSocket.send(sendPacket);
		} catch (IOException e) {
		e.printStackTrace();
		System.exit(1);
		}
		
		System.out.println("InterHost: packet sent");
		
		// We're finished, so close the sockets.
		//sendSocket.close();
		//receiveSocket.close();
	}
	
	
	
	
	public static void main( String args[] )
	   {
	      InterHost IH = new InterHost();
	      while(true)
	      IH.receiveAndSend();
	    
	   }
	
	
	//Additional Helper Functions
	//IO DISPLAY FUNCTIONS
	public void printInfoToSend(DatagramPacket pack) {
	      System.out.println("InterHost: Sending packet:");
	      System.out.println("To host: " + pack.getAddress());
	      System.out.println("Destination host port: " + pack.getPort());
	      int len = getLen(pack);
	      System.out.println("Length: " + len);
	      System.out.print("Containing: ");
	      System.out.println(new String(pack.getData(),0,len)); // or could print "s"
	}
	 public void printInfoReceived(DatagramPacket pack,byte[] dataByte) {
		   	  System.out.println("InterHost: Packet received:");
		      System.out.println("From host: " + pack.getAddress());
		      System.out.println("Host port: " + pack.getPort());
		      int len = getLen(pack);
		      System.out.println("Length: " + len);
		      System.out.print("Containing: ");
		      // Form a String from the byte array.
		      String received = new String(dataByte,0,len);   
		      System.out.println(received);
	
		     
	 }
	 
	 public int flipPort(DatagramPacket pack) {
		 if(pack.getPort()==clientPort) {
			 return 69;
		 }
		 else {
			 return clientPort;
		 }
	 }
 
	 public int getLen(DatagramPacket pack) {
		   return pack.getLength();
	 }

}
