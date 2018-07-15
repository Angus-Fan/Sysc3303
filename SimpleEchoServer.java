

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
    private void close()
    {
        receiveSocket.close();
    }
    public static void main(String args[])
    {
        SimpleEchoServer c = new SimpleEchoServer();
        Scanner keyboard = new Scanner(System.in);
        while(true) {
            c.receiveAndEcho();
           /* System.out.println("Type 1 to close the server or 0 to continue");
            if(keyboard.nextInt() == 1)
                break;*/
        }
        /*keyboard.close();
        c.close();*/
    }

}

