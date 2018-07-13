

//SimpleEchoServer.java
//This class is the server side of a simple echo server based on
//UDP/IP. The server receives from a client a packet containing a character
//string, then echoes the string back to the client.
//Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SimpleEchoServer {
  private DatagramPacket receivePacket;
  private DatagramSocket  receiveSocket;
  private int connectionCount=0;

  private final String path="C:\\Users\\michael\\Desktop\\3303\\server\\";

  public SimpleEchoServer()
  {
    try {
      // Construct a datagram socket and bind it to any available
      // port on the local host machine. This socket will be used to
      // send UDP Datagram packets.


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

  private void receiveAndEcho()
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

    Connection connection=new Connection(receivePacket,connectionCount++);
    connection.start();

  }

  public static void main(String args[])
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
class Connection extends Thread
{
  private DatagramPacket sendPacket, receivePacket;
  private DatagramSocket sendReceiveSocket;
  private final String path="C:\\Users\\michael\\Desktop\\3303\\server\\";
  private InputStream is = null;
  private OutputStream os = null;
  private String fileName="";
  private byte[] msg=new byte[4];
  private int blockCount=0;
  private byte[] data;
  private boolean overWritten=true;
  private int connectionID;
  private byte [][] fullFileData;
  public Connection(DatagramPacket packet, int connectionID)
  {
    System.out.println("Connection"+connectionID+" been created!");
    this.connectionID=connectionID;
    receivePacket=packet;
    try {

      sendReceiveSocket = new DatagramSocket();



    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }

  }
  public void run()
  {
    System.out.println("Connection"+connectionID+" port= "+sendReceiveSocket.getLocalPort());
    parseData();
    while(true) {
      if (getOpcode() == 0)
      {
        overWritten=true;
        break;
      }
      if (getOpcode() == 3) {
        System.out.println("Connection"+connectionID+" received data package");
        byte[] toFile = new byte[data.length - 4];
        for (int i = 0; i < data.length - 4; i++)
          toFile[i] = data[i + 4];
        blockCount++;
        toFile = trimByteArr(toFile);
        fileIO(2,toFile);
        constructArray();
        sending(msg);
      } else if (getOpcode() == 2)    //Writing request
      {
        System.out.println("Connection"+connectionID+" received writing request");
        blockCount=1;
        constructArray();
        sending(msg);
      } else if (getOpcode() == 1)   //Reading request
      {
        int numPack;    //finding out how many time need to send the whole file


        byte[] fileData = new byte[512];
        System.out.println("Reading file from: " + (path + fileName));
        fileIO(1,null);




          int blockNum = 0;
          while (true) {
            if (blockNum == fullFileData.length) {
              System.out.println("sending block num " + blockNum);
              sending(createDataPacket(3, blockNum, fullFileData[fullFileData.length-1]));
              data = new byte[4];
              receiving(data);
              System.out.println("Connection"+connectionID+" received ACK");
              break;
            } else {
              System.out.println("sending block num " + blockNum);
              sending(createDataPacket(3, blockNum, fullFileData[blockNum]));
              blockNum++;
              data = new byte[4];
              receiving(data);
              System.out.println("Connection"+connectionID+" received ACK");
            }
          }


          byte[] endOpCode = new byte[2];// the ending code
          endOpCode[0] = (byte) 0;
          endOpCode[1] = (byte) 0;
          sending(endOpCode);
          break;


      }
      data=new byte[512];
      receiving(data);
    }
  }
  private synchronized void fileIO(int readWrite,byte[] data)
  {
    if(readWrite==1) //reading
    {

      int numPack;    //finding out how many time need to send the whole file
      byte[] fileData;
      System.out.println("Reading file from: " + (path + fileName));
      File file = new File(path + "Test.txt");
      int size = (int) file.length();
      numPack = (int) Math.ceil(size / 512.0);
      int finalPacket = 0;
      if (numPack != 0)
        finalPacket = size % numPack;
      System.out.println("Packages need to send " + numPack);
      fullFileData=new byte[numPack][512];
      fullFileData[numPack-1]=new byte[finalPacket];
      try {
        is = new FileInputStream( (path + fileName));
        for(int i =0;i<numPack-1;i++){
          fileData = new byte[512];
          is.read(fileData);
          fullFileData[i]=fileData;
        }
        fileData = new byte[finalPacket];
        is.read(fileData);
        fullFileData[numPack-1]=fileData;
        is.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("fullfiledata---------------------------------");
      for(int i =0;i<fullFileData.length;i++)
      {
        String str=new String(fullFileData[i],0,fullFileData[i].length);
        System.out.println(str);
      }
      System.out.println("fullfiledata---------------------------------");
    }
    else
    {
      writing(data);
    }
  }

  private byte[] createDataPacket(int opCode,int dataBlock,byte[] data) {
    byte[] dataPack =new byte[4];
    dataPack[0]=(byte)0;
    dataPack[1]=(byte)3;
    dataPack[2]=(byte)Math.floor(dataBlock/10);
    dataPack[3]=(byte)(dataBlock%10);
    byte[] temp=new byte[4+data.length];
    temp[0]=dataPack[0];
    temp[1]=dataPack[1];
    temp[2]=dataPack[2];
    temp[3]=dataPack[3];
    for(int i=0;i<data.length;i++)
    {
      temp[i+4]=data[i];
    }
    /*System.out.println("Creating packet = ");
    for(int x = 0; x<temp.length;x++) {
      System.out.print((char)temp[x]);
    }
    System.out.println("");*/
    return temp;

  }
  private byte[] trimByteArr(byte[] data)
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
  private void sending(byte[] data)
  {
    String str = new String(data,0,data.length);
    System.out.println("Connection"+connectionID+" sending package= "+str);
    try {
      sendPacket = new DatagramPacket(data, data.length,
              receivePacket.getAddress(), 23);
      sendReceiveSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }


  }
  private void writing(byte[] data)
  {

    //System.out.println("data len="+data.length);

    /*byte[] newData = resize;
    resize = new byte[resize.length + data.length];
    System.arraycopy(newData, 0, resize, 0, newData.length);
    System.arraycopy(data, 0, resize, newData.length, data.length);
*/
    try
    {
      FileOutputStream out = new FileOutputStream(path+"Test.txt",!overWritten);
      overWritten=false;
      out.write(data);

      out.close();
    }
    catch(Exception e) {

      // if any I/O error occurs
      e.printStackTrace();
    }


  }
  private void receiving (byte[] data)
  {

    receivePacket = new DatagramPacket(data, data.length);

    try {

      sendReceiveSocket.receive(receivePacket);
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    //System.out.println("data receivied");

  }
  private void constructArray() {

    msg = new byte[4];
    msg[0] = (byte)0;
    msg[1] = (byte)4;
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
}
