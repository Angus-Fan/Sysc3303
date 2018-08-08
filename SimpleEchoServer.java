

//SimpleEchoServer.java
//This class is the server side of a simple echo server based on
//UDP/IP. The server receives from a client a packet containing a character
//string, then echoes the string back to the client.
//Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SimpleEchoServer {
    private DatagramPacket receivePacket;
    private DatagramSocket  receiveSocket;
    private int connectionCount=0;
    private ArrayList hostTID;
    private String path="C:\\Users\\michaelwang3\\Desktop\\server\\";
    private ArrayList<Connection> thread;
    private ShutDownThread shutDownThread;
    private static boolean mode=true;


    class ShutDownThread extends Thread {

        public ShutDownThread() {

        }

        public void run() {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("type 0 to shutdown the server!");
            if (keyboard.nextInt() == 0) {
                keyboard.close();

                receiveSocket.close();
                shutDown();
            }
        }



    }




    public SimpleEchoServer()
    {
        thread=new ArrayList<Connection>();
        hostTID=new ArrayList<Integer>();
        shutDownThread=new ShutDownThread();
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

    private boolean receiveAndEcho()
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
        }
        catch (SocketException e)
        {
            System.out.println("server receive socket shuts down!");
            shutDown();
            return false;
        }
        catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Server: Packet received:");
        int len = receivePacket.getLength();
        if(!mode) {
            System.out.println("From host: " + receivePacket.getAddress());
            System.out.println("Host port: " + receivePacket.getPort());


            System.out.println("Length: " + len);
            System.out.print("Containing: ");
        }

        // Form a String from the byte array.
        String received = new String(data,0,len);
        System.out.println(received + "\n");

        if(!hostTID.contains(receivePacket.getPort())) {
            hostTID.add(receivePacket.getPort());
            Connection connection = new Connection(receivePacket, connectionCount++, path,mode);
            connection.start();

            thread.add(connection);
        }
        else
        {
            System.out.println("Server: got duplicate request");
        }
        return true;
    }
    private void readFilePath(Scanner scan) {

        System.out.println("Please enter the path to the file: ");
        path = scan.nextLine();
        System.out.println("The file path is : " + path);



    }
    private void shutDown() {
        for (int i = thread.size() - 1; i >= 0; i--) {
            if (thread.get(i).isAlive()) {

                try {
                    System.out.println("Waiting for connection"+i+" to shutdown");
                    thread.get(i).join();

                } catch (Exception e) {
                }
            }
        }
        System.out.println("Server shuts down");
       // System.exit(0);
    }




    public void startListen()
    {
        shutDownThread.start();
    }
    private void close()
    {
        receiveSocket.close();
    }
    public static void main(String args[])
    {
        SimpleEchoServer c = new SimpleEchoServer();
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Type 1 to be in normal mode or 2 to be in Verbose mode");
        if(keyboard.nextInt() == 1)
            mode = true;
        else
            mode = false;
        //ShutDownThread shutDownThread= new ShutDownThread();
        c.readFilePath(keyboard);
        //shutDownThread.start();
        c.startListen();
        while(true) {
        	/*System.out.println("Type 1 to close the server or 0 to continue");
            if(keyboard.nextInt() == 1)
                break;*/
            if(!c.receiveAndEcho())
                break;
        }
        //keyboard.close();
        //c.close(); // This is shutdown
    }

}
