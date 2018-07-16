import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class ErrorSimConnection extends Thread{

    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket  socket,fakeSocket;
    private int serverPort=69;
    private int portToSend;
    private boolean flip=false;
    private int clientsTID;
    private int serversTID;
    private static int modified;
    private int fileNameLen;
    private String fileName;
    private byte[] data;
    private int usingFakeSocket=0;
    public ErrorSimConnection(DatagramPacket pack,int modified) {
        this.modified=modified;
        try {
            socket = new DatagramSocket();

        }
        catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }
        receivePacket=pack;
    }

    public void run() {
        clientsTID=receivePacket.getPort();
        data=receivePacket.getData();
        byte newdata[] = new byte[receivePacket.getLength()];
        for(int i =0;i<newdata.length;i++)
        {
            newdata[i]=data[i];
        }
        data=newdata;
        System.out.println("data len= "+data.length);

        printInfoReceived(receivePacket,data);
        System.out.println();




        while(true) {

            if(flip)
            {
                if (modified == 411) {
                    data = badOpCode();
                }  else if (modified == 421) {
                    if (data[1] == (byte) 3) {

                        data = badOpCode();
                    }
                } else if (modified == 422) {
                    if (data[1] == (byte) 3) {
                        data[3] = (byte) 9;
                    }
                } else if (modified == 431) {
                    if (data[1] == (byte) 4) {
                        data = badOpCode();
                    }
                } else if (modified == 432) {
                    if (data[1] == (byte) 4) {
                        data[3] = (byte) 9;
                    }
                }
            }
            else
            {
                if (modified == 412) {
                       data = badMode();
                      System.out.println("WE WENT INTO 412");
                        modified=0;
                }
            }

            if(serversTID==0)
                portToSend=69;
            else if(flip) {
                System.out.println("sending to server");
                portToSend = serversTID;
                flip=false;
            }
            else {
                System.out.println("sending to client");
                flip=true;
                portToSend = clientsTID;
            }

            sendPacket = new DatagramPacket(data, data.length,
                    receivePacket.getAddress(), portToSend);

            printInfoToSend(sendPacket);
            // or (as we should be sending back the same thing)
            // System.out.println(received);

            // Send the datagram packet to the client via the send socket.
            System.out.println("modified= "+modified+" usingFakeSocket="+usingFakeSocket);
            if (modified == 5 && usingFakeSocket>=2) {
                System.out.println("Using the fake Socket");
                try {
                    fakeSocket = new DatagramSocket();
                    fakeSocket.send(sendPacket);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                try {
                    socket.send(sendPacket);
                    usingFakeSocket++;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            System.out.println("InterHost: packet sent");
            if(data[1]==(byte)0) {

                break;
            }

            receive();
            if(serversTID==0)
                serversTID=receivePacket.getPort();
        }
        System.out.println("Host thread shuts down");




    }
    private boolean readOrWrite(byte[] data)
    {
        return data[1]==(byte)1||data[1]==(byte)2;
    }
    private void receive()
    {
        data = new byte[516];

        receivePacket = new DatagramPacket(data,data.length);


        try {
            System.out.println("we are now waiting");
            socket.receive(receivePacket);

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
        byte[] badOpCodeBytes = new byte[receivePacket.getLength()];
        badOpCodeBytes = receivePacket.getData();
        badOpCodeBytes[0] = 1;
        badOpCodeBytes[1] = 1;
        System.out.print("MODIFIED OPCODE = ");
        for(int x = 0;x<badOpCodeBytes.length;x++) {
            System.out.print(badOpCodeBytes[x]);
        }
        return badOpCodeBytes;

    }
    private byte[] badMode() {
        parseData();
        byte[] fileNameBytes = fileName.getBytes();
        byte[] oldInfo = new byte[fileNameLen + 2 + 1 + 1 + 8];
        oldInfo[0] = 0;
        oldInfo[1] = (byte) getOpcode();
        for (int i = 0; i < fileNameLen; i++) {
            oldInfo[i + 2] = fileNameBytes[i];
        }
        oldInfo[2 + fileNameLen] = (byte) 0;
        byte[] nebasciiBytes = "nebascii".getBytes();
        for (int i = 0; i < nebasciiBytes.length; i++) {
            oldInfo[i + 2 + 1 + fileNameLen] = nebasciiBytes[i];
        }
        oldInfo[2 + fileNameLen + 1 + 8] = (byte) 0;
        System.out.print("MODIFIED MODE = ");
        for (int x = 0; x < oldInfo.length; x++) {
            System.out.print(oldInfo[x]);
        }
        System.out.print("");

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
}
