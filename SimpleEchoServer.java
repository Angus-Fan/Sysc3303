

//SimpleEchoServer.java
//This class is the server side of a simple echo server based on
//UDP/IP. The server receives from a client a packet containing a character
//string, then echoes the string back to the client.
//Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Scanner;

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
    
    byte data[] = new byte[516];
    
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
    
    /*byte newdata[]=new byte[receivePacket.getLength()];
     * 
     for(int i=0;i<newdata.length;i++)
     {
     newdata[i]=data[i];
     
     }
     data=newdata;*/
    
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
    
    if(blockCount==0||isReading)
      parseData();
    
    if(getOpcode()==4)
    {
    	System.out.println("Reading.......");
    }
    else if(getOpcode()==3)
    {
      System.out.println("writting.......");
      byte[] toFile= new byte[data.length-4];
      
      for(int i =0;i<data.length-4;i++)
      {
        toFile[i]=data[i+4];
        
      }
      blockCount++;
      toFile=trimByteArr(toFile);
      writting(toFile);
      isReading=false;
      constructArray();
      sending(msg);
    }
    else if(getOpcode()==2)
    {
      System.out.println("writting...");
      isReading=false;
      constructArray();
      sending(msg);
    }    
    else if(getOpcode()==1)
    {
    	int numPack=0;    //finding out how many time need to send the whole file
        
        
        byte[] fileData  = new byte[512];
        System.out.println("Reading file from: "+(path+fileName));
        try {
          is = new FileInputStream((path+"Test.txt"));
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
        File file = new File(path+"Test.txt");
        int size = (int)file.length();
        int finalPacket = size%numPack;
        
        
        
        try {
          is = new FileInputStream((path+"Test2.txt"));
          
          
          
          
          
          
          
          
          int blockNum=0;
          while(blockNum!=numPack)
          {
        	if(blockNum == numPack)
        	{
        		 fileData=new byte[finalPacket];
                 is.read(fileData);
                // fileData=trimByteArr(fileData);
                 
                 System.out.println("sending block num "+blockNum);
                 sending(createDataPacket(3,blockNum,fileData));
                 
                 blockNum++;
                 receiving(data);
        	}
        	else {
	            fileData=new byte[512];
	            is.read(fileData);
	            fileData=trimByteArr(fileData);
	            
	            System.out.println("sending block num "+blockNum);
	            sending(createDataPacket(3,blockNum,fileData));
	            
	            blockNum++;
	            receiving(data);
        	}
          }
          is.close();
          
          byte[] endOpCode=new byte[2];
          endOpCode[0]=(byte)0;
          endOpCode[1]=(byte)0;
          sending(endOpCode);
          
        }
        catch(Exception e) {
          
          // if any I/O error occurs
          e.printStackTrace();
        }
    }
    /*
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
      //byte[][] fullMsg=new byte[numPack][512];
      
      blockCount=0;
      
      
      
      
        is.read(fileData);
      sending(fileData);
      blockCount++;
      while(blockCount!=numPack)
      {
        receiving(data);
        fileData=new byte[512];
        is.read(fileData);
        sending(createDataPacket(3,blockCount,trimByteArr(fileData)));
        blockCount++;
      }
      receiving(data);
      msg=new byte[1];
      sending(msg);
      
      
    }
    
    catch(Exception e) {
      
      // if any I/O error occurs
      e.printStackTrace();
    }*/
    
    /*
     try {
     Thread.sleep(0);
     } catch (InterruptedException e ) {
     e.printStackTrace();
     System.exit(1);
     }*/

 
  }
  
  
  public byte[] createDataPacket(int opCode,int dataBlock,byte[] data) {
	    byte[] dataPack =new byte[4];
	    dataPack[0]=(byte)0;
	    dataPack[1]=(byte)3;
	    dataPack[2]=(byte)((int)dataBlock/10);
	    dataPack[3]=(byte)((int)dataBlock%10);
	    byte[] temp=new byte[4+data.length];
	    temp[0]=dataPack[0];
	    temp[1]=dataPack[1];
	    temp[2]=dataPack[2];
	    temp[3]=dataPack[3];
	    for(int i=0;i<data.length;i++)
	    {
	      temp[i+4]=data[i];
	    }     
	    System.out.println("Create packet's packet = ");
	    for(int x = 0; x<temp.length;x++) {
	      System.out.print((char)temp[x]);
	    }
	    System.out.println("");
	    return temp;
	    
	  }
  public byte[] trimByteArr(byte[] data)
  {
	  int i=0;
	  for(i =0;i<data.length;i++)
	  {
		  if(data[i]==(byte)0)
			  break;
	  }
	  byte [] temp=new byte[i];
	  for(i=0;i<temp.length;i++)
	  {
		  temp[i]=data[i];
	  }
	  return temp;
  }
  public void sending(byte[] data)
  {
    System.out.println("Server: packet sent:    "+data.length);
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
      FileOutputStream out = new FileOutputStream(path+"Test.txt");
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
  
  private int getOpcode()
  {
    return receivePacket.getData()[1];
    
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
    Scanner keyboard = new Scanner(System.in);
    while(true) {
      c.receiveAndEcho();
      System.out.println("Type 1 to close the server or 0 to continue");
      if(keyboard.nextInt() == 1)
    	  break;
    }
    keyboard.close();
  }
  
}

