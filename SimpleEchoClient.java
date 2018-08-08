// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SimpleEchoClient{
  private byte[] resize = new byte[0];
  private  String path="C:\\Users\\michaelwang3\\Desktop\\";
  //private final String path="E:\\";
  private  String fileName="test.txt";
  private DatagramPacket sendPacket, receivePacket;
  private DatagramSocket sendReceiveSocket;
  private InputStream is = null;
  private OutputStream os = null;
  private byte[] msg;
  private int blockCount=0;
  private int hostPort=0;
  private boolean isError=false;
  private int timeout=3000;
  private int maxAttempt=5;
  private int curtAttempt=0;
  private byte[] previousPacket = new byte[4];
  private boolean dupPacket = false;
  private static boolean mode=true; //true = normal
  public SimpleEchoClient()
  {

    try {
      
      sendReceiveSocket = new DatagramSocket();


    } catch (SocketException se) {   
      se.printStackTrace();
      System.exit(1);
    }

  }

  private void sendReadAndReceive()
  {
    if(!checkAccess())
      return;
    hostPort = 0;
    if(!mode) {
      System.out.println("Client: sending a packet containing:\n");
    }
    constructArray(1,fileName,"netascii");
    sending(msg);
    System.out.println("Client: Packet sent.\n");

    byte data[] = new byte[516];

    int dataSize;
    int temp;

    curtAttempt=0;
    dataSize=receivingTimeout(data);
    while(dataSize==-1)
    {
      if(curtAttempt>=maxAttempt)
      {
        System.out.println("Connection lost, client shuts down");
        close();
        return;
      }
      curtAttempt++;
      System.out.println("Resending the request");
      sending(msg);
      dataSize= receivingTimeout(data);
    }

    while(dataSize==516)
    {

      if(receivePacket.getData()[1]==(byte)5) {
        System.out.println("Data Received: " + new String(receivePacket.getData(), 4, dataSize-5));
        break;
      }

      byte[] newdata = new byte[dataSize];
      for(int i=0;i<newdata.length;i++)
      {
        newdata[i]=data[i];
      }
      data=newdata;

      byte[] toFile= new byte[data.length-4];

      for(int i =0;i<data.length-4;i++)
      {
        toFile[i]=data[i+4];

      }
      blockCount++;

      if(!writting(toFile))
        return;
      ack(data);
      sending(msg);
      data=new byte[516];

      dataSize=receiving(data);

    }



    if(receivePacket.getData()[1]==(byte)5) {
      System.out.println(new String(receivePacket.getData(), 4, dataSize-5));

    }

    byte[] newdata = new byte[dataSize];
    for(int i=0;i<newdata.length;i++)
    {
      newdata[i]=data[i];
    }
    data=newdata;

    byte[] toFile= new byte[data.length-4];

    for(int i =0;i<data.length-4;i++)
    {
      toFile[i]=data[i+4];

    }
    blockCount++;

    if(!writting(toFile))
      return;
    ack(data);
    sending(msg);
    System.out.println("End with the RRQ ");



  }
  private void sendWriteAndReceive()
  {
    int numPack=0;    //finding out how many time need to send the whole file

    hostPort = 0;
    byte[] fileData  = new byte[512];
    System.out.println("Reading file from: "+(path+fileName));
    try {
      


      File file = new File(path + fileName);
      if(!file.canRead())
      {
        System.out.println("Cannot read the file from: "+(path+fileName));
        System.exit(0);
      }
      int size = (int) file.length();
      if(!mode) {
        System.out.println("File size: " + size);
      }
      numPack = (int) Math.ceil(size / 512.0);
      int finalPacket = 0;
      if (numPack != 0)
        finalPacket = size % 512;

      if(!mode) {
        System.out.println("Packages need to send " + numPack);
      }
      is = new FileInputStream((path+fileName));
      if(!mode) {
        System.out.println("Client: sending a packet containing:\n");
      }
      constructArray(2,fileName,"netascii");
      sending(msg);

      System.out.println("Client: Packet sent.\n");


      byte data1[] = new byte[516];

      // resend RQ when timeout
      int temp;

      curtAttempt=0;
      temp=receivingTimeout(data1);
      while(temp==-1)
      {
        if(curtAttempt>=maxAttempt)
        {
          System.out.println("Connection lost, client shuts down");
          close();
          return;
        }
        curtAttempt++;
        System.out.println("Resending the request");
        sending(msg);
        temp= receivingTimeout(data1);
      }






      if(checkingError(data1))
      {
        return;
      }
      int blockNum=0;
      while(blockNum!=numPack)
      {
        fileData=new byte[512];
        is.read(fileData);
        fileData=trimByteArr(fileData);

        System.out.println("sending block num "+blockNum);
        sending(createDataPacket(3,blockNum+1,fileData));


        curtAttempt=0;
        temp=receivingTimeout(data1);
        while(temp==-1)
        {
          if(curtAttempt>=maxAttempt)
          {
            System.out.println("Connection lost, client shuts down");
            close();
            return;
          }
          curtAttempt++;
          System.out.println("Resending the data block "+(blockNum+1));
          sending(createDataPacket(3,blockNum+1,fileData));
          temp= receivingTimeout(data1);
        }
        blockNum++;
        if(checkingError(data1))
        {
          return;
        }
        /*if(blockNum==numPack-1)
          break;*/
      }
      if(finalPacket==0)
      {
        numPack++;
        msg =new byte[4];
        msg[0]=(byte)0;
        msg[1]=(byte)3;
        msg[2]=(byte)(numPack/10);
        msg[3]=(byte)(numPack%10);
        sending(msg);
      }
      is.close();

    }
    catch (FileNotFoundException e)
    {
      if(e.toString().substring(e.toString().length()-42).compareTo("The system cannot find the file specified)")==0)
      {
        System.out.println("ERROR:File not found!");
      }
      else
      {
        e.printStackTrace();

      }
    }
    catch(Exception e) {

      // if any I/O error occurs
      e.printStackTrace();
    }

    System.out.println("End with the WRQ ");
  }
  private boolean checkingError(byte [] data)
  {
    if(data[1]==(byte)5) {
      System.out.println(new String(data, 4, parseData()));
      if(data[3]==(byte)4)
      {
        System.out.println("End with the WRQ ");

      }
      return true;
    }
    return false;
  }
  private byte[] createDataPacket(int opCode,int dataBlock,byte[] data) {
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
    
    return temp;

  }

  private void sending(byte[] data)
  {
    if(hostPort==0) {
      try {
        sendPacket = new DatagramPacket(data, data.length,
                InetAddress.getLocalHost(), 23);
      } catch (UnknownHostException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    else
    {
      try {
        sendPacket = new DatagramPacket(data, data.length,
                InetAddress.getLocalHost(), hostPort);
      } catch (UnknownHostException e) {
        e.printStackTrace();
        System.exit(1);
      }

    }
    try {
      sendReceiveSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    printInfoToSend(sendPacket);
  }
  private boolean checkAccess()
  {
    try
    {
      FileOutputStream out = new FileOutputStream(path+fileName);
      out.write((byte)1);

      out.close();
    }
    catch (FileNotFoundException e)
    {
      if(e.toString().substring(e.toString().length()-17).compareTo("Access is denied)")==0)
      {
        System.out.println("ERROR:Access is denied!!");
        return false;
      }
      else
      {
        e.printStackTrace();

      }
    }
    catch(IOException e) {
      if(e.toString().substring(e.toString().length()-37).compareTo("There is not enough space on the disk")==0)
      {
        System.out.println("ERROR: There is not enough space on the disk");
        return false;
      }
      else
      {
        e.printStackTrace();

      }
    }
    catch(Exception e) {

      // if any I/O error occurs
      e.printStackTrace();
    }
    return true;


  }

  private boolean writting(byte[] data)
  {
    if(!mode) {
      System.out.println("Length of the file is: " + data.length);
    }
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
    catch (FileNotFoundException e)
    {
      if(e.toString().substring(e.toString().length()-17).compareTo("Access is denied)")==0)
      {
        System.out.println("ERROR:Access is denied!!");
        isError=true;
        return false;
      }
      else
      {
        e.printStackTrace();

      }
    }
    catch(IOException e) {
      if(e.toString().substring(e.toString().length()-37).compareTo("There is not enough space on the disk")==0)
      {
        System.out.println("ERROR: There is not enough space on the disk");
        isError=true;
        String errorMsg="ERROR: There is not enough space on the disk";
        byte [] data1=new byte[5+errorMsg.getBytes().length];
        data1[0]=(byte)0;
        data1[1]=(byte)5;
        data1[2]=(byte)0;
        data1[3]=(byte)3;
        data1[data1.length-1]=(byte)0;
        System.arraycopy(errorMsg.getBytes(),0,data1,4,errorMsg.getBytes().length);
        sending(data1);



        return false;

      }
      else
      {
        e.printStackTrace();

      }
    }
    catch(Exception e) {

      // if any I/O error occurs
      e.printStackTrace();
    }
    return true;


  }
  private int parseData() {
    int i  = 4;
    int len = 0;

    while(receivePacket.getData()[i] != (byte)0)
    {
      len++;
      i++;
    }
    return len;

  }

  private int receiving (byte[] data1)
  {

    receivePacket = new DatagramPacket(data1, data1.length);

    try {
      sendReceiveSocket.setSoTimeout(0);
      sendReceiveSocket.receive(receivePacket);
    }

    catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    checkDup();
    if(!dupPacket) {
      storeOpcode();

      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
     


      if (hostPort == 0)
        hostPort = receivePacket.getPort();
      data1 = trimByteArr(data1);
      return newdata.length;
    }
    else {
      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
      System.out.println("Duplicate Packet Received");



      if(data1[1]==3)
      {
        System.out.println("resend ACK!");
        ack(data1);
        sending(msg);
      }

      return receiving(data1);
    }
  }
  private int receivingTimeout (byte[] data1)
  {

    receivePacket = new DatagramPacket(data1, data1.length);

    try {
      sendReceiveSocket.setSoTimeout(timeout);
      sendReceiveSocket.receive(receivePacket);
    }
    catch(SocketTimeoutException e) {
      System.out.println("TIME OUT!");
      return -1;
    }
    catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    checkDup();
    if(!dupPacket) {
      storeOpcode();

      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
     


      if (hostPort == 0)
        hostPort = receivePacket.getPort();
      data1 = trimByteArr(data1);
      return newdata.length;
    }
    else {
      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
      System.out.println("Duplicate Packet Received");



      return 0;
    }
  }

  private int firstReceive(byte[] data1)
  {

    receivePacket = new DatagramPacket(data1, data1.length);

    try {
      sendReceiveSocket.setSoTimeout(20000);
      sendReceiveSocket.receive(receivePacket);
    }
    catch(SocketTimeoutException e) {
      System.out.println("TIME OUT!");
      System.out.println("No connection, Client shuts down");
      System.exit(0);
    }
    catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    checkDup();
    if(!dupPacket) {
      storeOpcode();

      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
      


      if (hostPort == 0)
        hostPort = receivePacket.getPort();
      data1 = trimByteArr(data1);
      return newdata.length;
    }
    else {
      printInfoReceived(receivePacket, data1);

      byte[] newdata = new byte[getLen(receivePacket)];
      for (int i = 0; i < newdata.length; i++) {
        newdata[i] = data1[i];
      }
      data1 = newdata;
      System.out.println("Duplicate Packet Received");



      return 0;
    }
  }


  private void printInfoToSend(DatagramPacket pack) {

    int len = getLen(pack);
    if(!mode) {
      System.out.println("Client: Sending packet:");
      System.out.print("Containing: ");
      System.out.println(new String(pack.getData(), 0, len));
    }// or could print "s"
    System.out.println("Sending done!");
  }
  private void printInfoReceived(DatagramPacket pack,byte[] dataByte) {
    System.out.println("Client: Packet received:");
    
    int len = getLen(pack);

    if(!mode) {
      System.out.print("Containing: ");
      // Form a String from the byte array.
      String received = new String(dataByte, 0, len);
      System.out.println(received);
      System.out.println();
      for (int x = 0; x < len; x++) {
        System.out.print(pack.getData()[x]);
      }
    }
    System.out.println();

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
  private int getLen(DatagramPacket pack) {
    return pack.getLength();
  }
  private void constructArray(int request,String file,String mode) {

    byte zero[] = new byte[1];
    byte fileByte [] = file.getBytes();

    byte modeByte[] = mode.getBytes();
    msg = new byte[zero.length + zero.length + 2 + modeByte.length + fileByte.length];
    msg[0] = (byte)0;
    msg[1] = (byte)request;

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

    msg = dataGramPackage;

  }
  private void ack(byte []data) {

    msg = new byte[4];
    msg[0] = (byte)0;

    msg[1] = (byte)4;
/*
    if(blockCount<10)
    {
      msg[2] = (byte)0;
      msg[3] =(byte)blockCount;
    }
    else
    {
      msg[2] = (byte)(int)(blockCount%10);
      msg[3] =(byte)blockCount;
    }*/
    msg[2]=receivePacket.getData()[2];
    msg[3]=receivePacket.getData()[3];



    byte[] dataGramPackage = new byte[4];
    dataGramPackage[0] = msg[0];
    dataGramPackage[1] = msg[1];
    dataGramPackage[2] = msg[2];
    dataGramPackage[3] = msg[3];

    msg = dataGramPackage;


  }
  private void readFilePath(Scanner scan) {

    System.out.println("Please enter the path to the file: ");
    path = scan.nextLine();
    System.out.println("The file path is : " + path);



  }
  private void readFileName(Scanner scan) {

    System.out.println("Please enter the name of the file: ");
    fileName = "\\"+scan.nextLine();
    System.out.println("The file name is : " + fileName);




  }
  private void storeOpcode()
  {
    for (int i = 0; i<4; i++)
      previousPacket[i] = receivePacket.getData()[i];
  }

  private void checkDup()
  {

    int four = 0;
    for (int i = 0; i<4; i++)
    {

      if(previousPacket[i] == receivePacket.getData()[i])
        four++;
    }
    if(four == 4) {

      dupPacket = true;
    }
    else
      dupPacket = false;
  }



  private void close()
  {
    sendReceiveSocket.close();
  }

  public static void main(String args[])
  {
    SimpleEchoClient c = new SimpleEchoClient();
    Scanner scannerPath = new Scanner(System.in);
    c.readFilePath(scannerPath);
    Scanner scannerFileName = new Scanner(System.in);
    c.readFileName(scannerFileName);
    //    c.readFileName();




    //c.sendReadAndReceive();
    //c.sendReadAndReceive();

    Scanner keyboard = new Scanner(System.in);
    System.out.println("Type 1 to be in normal mode or 2 to be in Verbose mode");
    if(keyboard.nextInt() == 1)
      mode = true;
    else
      mode = false;
    System.out.println("Type 1 to close the client, 2 to read or 3 to write");

    int temp=keyboard.nextInt();
    if(temp== 2)
    {

      c.sendReadAndReceive();
    }
    else if(temp==3)
    {
      c.sendWriteAndReceive();
    }


    scannerPath.close();
    scannerFileName.close();
    keyboard.close();

    c.close(); //This is shutdown
  }

}
