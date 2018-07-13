import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class ErrorSimulator {

  private DatagramPacket sendPacket, receivePacket, trimmedReceivePacket;
  private DatagramSocket sendSocket, receiveSocket;
  private int serverPort=69;
  private int portToSend;
  private boolean getServerTip = false;
  private ArrayList<Integer> clientsTID=new ArrayList<Integer>();
  private ArrayList<Integer> serversTID=new ArrayList<Integer>();
  public ErrorSimulator() {
    try {
      sendSocket = new DatagramSocket();
      receiveSocket = new DatagramSocket(23);
    }
    catch (SocketException se){
      se.printStackTrace();
      System.exit(1);   
    }
  }

  private void receiveAndSend() {
    byte data[] = new byte[516];
    
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
    byte newdata[] = new byte[receivePacket.getLength()];
    for(int i =0;i<newdata.length;i++)
    {
      newdata[i]=data[i];
    }
    data=newdata;
    System.out.println("the new data len= "+newdata.length);
    if(getServerTip)
    {
      serversTID.add(receivePacket.getPort());
      getServerTip=false;
    }
    else if(data[1]==1||data[1]==2)
    {
      clientsTID.add(receivePacket.getPort());
      getServerTip=true;
    }
    /*
    if(!fromClient) {
      clientPort = receivePacket.getPort();
      fromClient = true;
    }
    portToSend = flipPort(receivePacket);*/
    
    
    
    printInfoReceived(receivePacket,data);
    if(data[1]==1||data[1]==2)
    {
      portToSend=serverPort;
    }
    else if(serversTID.contains(receivePacket.getPort()))
    {
      portToSend=clientsTID.get(serversTID.indexOf(receivePacket.getPort()));
    }
    else if (clientsTID.contains(receivePacket.getPort()))
    {
      portToSend=serversTID.get(clientsTID.indexOf(receivePacket.getPort()));
    }
    else
    {
      System.out.println("ERROR: UNKNOWN DESTINATION");
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
  
  
  
  

  
  
  
  
  //Additional Helper Functions
  //IO DISPLAY FUNCTIONS
  private void printInfoToSend(DatagramPacket pack) {
    System.out.println("InterHost: Sending packet:");
    //  System.out.println("To host: " + pack.getAddress());
    // System.out.println("Destination host port: " + pack.getPort());
    int len = getLen(pack);
    // System.out.println("Length: " + len);
    System.out.print("Containing: ");
    System.out.println(new String(pack.getData(),0,len)); // or could print "s"
  }
  private void printInfoReceived(DatagramPacket pack,byte[] dataByte) {
    System.out.println("InterHost: Packet received:");
    //   System.out.println("From host: " + pack.getAddress());
    // System.out.println("Host port: " + pack.getPort());
    int len = getLen(pack);
    //System.out.println("Length: " + len);
    System.out.print("Containing: ");
    // Form a String from the byte array.
    
    // System.out.println(received);
    //System.out.println("Byte array: " + pack.getData());
    System.out.print("Package in bytes: ");
    for(int x = 0;x<len;x++) {
      System.out.print(pack.getData()[x]);
    }
    System.out.println("");
    String receive = new String(pack.getData(),0,pack.getLength());  
    System.out.print("Package in string: "+ receive);
    
    
    
  }
/*
  private int flipPort(DatagramPacket pack) {
    if(pack.getPort()==clientPort) {
      return 69;
    }
    else {
      return clientPort;
    }
  }*/

  private int getLen(DatagramPacket pack) {
    return pack.getLength();
  }
  public static void main( String args[] )
  {
    ErrorSimulator IH = new ErrorSimulator();

    while(true) {
      IH.receiveAndSend();
    }
  }
  
}
