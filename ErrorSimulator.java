import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class ErrorSimulator {

    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendSocket, receiveSocket;
    private int serverPort=69;
    private int portToSend;
    private boolean getServerTip = false;
    private ArrayList<Integer> clientsTID=new ArrayList<Integer>();
    private ArrayList<Integer> serversTID=new ArrayList<Integer>();
    private static int modified;
    private int fileNameLen;
    private String fileName;
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
        else if(readOrWrite(data))
        {
            clientsTID.add(receivePacket.getPort());
            getServerTip=true;
        }



        printInfoReceived(receivePacket,data);
        System.out.println();
        if(readOrWrite(data))
        {
            System.out.println("from client to server");
            portToSend=serverPort;
        }
        else if(serversTID.contains(receivePacket.getPort()))
        {
            System.out.println("from connection to client");
            portToSend=clientsTID.get(serversTID.indexOf(receivePacket.getPort()));
            if(data[1]==0)
            {
                clientsTID.remove(serversTID.indexOf(receivePacket.getPort()));
                serversTID.remove(serversTID.indexOf(receivePacket.getPort()));
            }
        }
        else if (clientsTID.contains(receivePacket.getPort()))
        {
            System.out.println("from client to connection");
            portToSend=serversTID.get(clientsTID.indexOf(receivePacket.getPort()));
            if(data[1]==0)
            {
                serversTID.remove(clientsTID.indexOf(receivePacket.getPort()));
                clientsTID.remove(clientsTID.indexOf(receivePacket.getPort()));
            }
        }
        else
        {
            System.out.println("ERROR: UNKNOWN DESTINATION");
        }


        if(modified!=0)
        {
            if(modified==411)
            {
                data=badOpCode();
            }
            else if(modified==413)
            {
                data=badMode();
            }
            else if(modified==421)
            {
                if(data[1]==(byte)3)
                {
                    data=new byte[2];
                    data=badOpCode();
                }
            }
            else if(modified==422)
            {
                if(data[1]==(byte)3)
                {
                    data[3]=(byte)9;
                }
            }
        }
        sendPacket = new DatagramPacket(data, data.length,
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
    private boolean readOrWrite(byte[] data)
    {
        return data[1]==(byte)1||data[1]==(byte)2;
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
        //System.out.println(new String(pack.getData(),0,len)); // or could print "s"
        for(int x = 0;x<len;x++) {
            System.out.print(pack.getData()[x]);
        }
        System.out.println("");
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


    private int getLen(DatagramPacket pack) {
        return pack.getLength();
    }
    private byte[] badOpCode() {
        byte[] badOpCodeBytes = new byte[2];
        badOpCodeBytes[0] = 1;
        badOpCodeBytes[1] = 1;
        return badOpCodeBytes;

    }
    private byte[] badMode() {
        parseData();
        byte[] fileNameBytes = fileName.getBytes();
        byte[] oldInfo = new byte[fileNameLen+2+1+1+8];
        oldInfo[0] = 0;
        oldInfo[1] = (byte)getOpcode();
        for(int i = 0;i<fileNameLen;i++) {
            oldInfo[i+2] = fileNameBytes[i];
        }
        oldInfo[2+fileNameLen] = (byte)0;
        byte[] nebasciiBytes = "nebascii".getBytes();
        for(int i = 0;i<nebasciiBytes.length;i++) {
            oldInfo[i+2+1+fileNameLen] = nebasciiBytes[i];
        }
        oldInfo[2+fileNameLen+1+8]=(byte)0;
        System.out.println(oldInfo);
        return oldInfo;


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
        fileNameLen = fileName.length();
    }


    public static void main( String args[] )
    {
        ErrorSimulator IH = new ErrorSimulator();
        System.out.println("Which Error would you like to simulate (4-5) or 0 for no Error : ");
        Scanner scan = new Scanner(System.in);
        int choice = scan.nextInt();
        while(true) {
            if(choice==4) {
                System.out.println("ILLEGAL TFTP");
                System.out.println("Which Error would you like to simulate( [1:RRQ/WWR] [2:DATA] [3:ACK] ) : ");
                Scanner errorScan = new Scanner(System.in);
                int errorToSimulate = errorScan.nextInt();


                if(errorToSimulate==1) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE] [2:FILENAME] [3:MODE] ) : ");
                    Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = fieldScanner.nextInt();

                    if(partToSimulate==1) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with OP CODE 11");
                        modified=411;
                    }
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with filename byte>127");
                    }
                    if(partToSimulate==3) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with mode failure 'nepascii' " );
                        modified=413;
                    }
                    fieldScanner.close();
                }
                else if(errorToSimulate==2) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE] [2:DATABLOCK] ) : ");

                    Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = fieldScanner.nextInt();
                    if(partToSimulate==1) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with OP CODE 11");
                        modified=421;
                    }
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with wrong block number");
                        modified=422;

                    }



                    fieldScanner.close();

                }
                else if(errorToSimulate==3) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE] [2:DATABLOCK] ) : ");

                    Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = fieldScanner.nextInt();
                    if(partToSimulate==1) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with OP CODE 11");
                        modified=431;
                    }
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Which block do you want to throw the error in");
                        Scanner blockScan = new Scanner(System.in);
                        int blockToFail = blockScan.nextInt();//maybe make this thing
                        blockScan.close();

                    }

                    fieldScanner.close();
                }
                errorScan.close();

                break;
            }
            if(choice==5) {

                System.out.println("UNKOWN TID");
                break;
            }
            if(choice==0) {
                modified=0;
                break;
            }


        }
        scan.close();
        while(true) {
            IH.receiveAndSend();
            System.out.println();
        }

    }

}
