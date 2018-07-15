import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class ErrorSimulator{
    public int modified=0;
    private DatagramPacket receivePacket;
    private DatagramSocket receiveSocket;
    public ErrorSimulator() {
        try {
            receiveSocket = new DatagramSocket(23);
        }
        catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }

    }
    public void receiveAndSend() {
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

        ErrorSimConnection errorSimulator=new ErrorSimConnection(receivePacket,modified);
        errorSimulator.start();


    }
    private void userInput()
    {
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
                    System.out.println("Which field would you like to change( [1:OPCODE]  [2:MODE] ) : ");
                    Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = fieldScanner.nextInt();

                    if(partToSimulate==1) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with OP CODE 11");
                        modified=411;
                    }/*
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with filename byte>127");
                    }*/
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with mode failure 'nepascii' " );
                        modified=412;
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
                        modified=432;

                    }

                    fieldScanner.close();
                }
                errorScan.close();

                break;
            }
            if(choice==5) {

                System.out.println("UNKOWN TID");
                modified=5;
                break;
            }
            if(choice==0) {
                modified=0;
                break;
            }
        }
        scan.close();
    }

    public static void main( String args[] )
    {
        ErrorSimulator IH = new ErrorSimulator();
        IH.userInput();



        while(true) {
            IH.receiveAndSend();
            System.out.println();
        }

    }

}
