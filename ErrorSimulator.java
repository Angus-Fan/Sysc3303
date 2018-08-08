import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class ErrorSimulator{
    public int modified=0;
    private DatagramPacket receivePacket;
    private DatagramSocket receiveSocket;
    private int modifiedPackIndex=0;
    private int delayAmount=0;
    private ArrayList clientsTID;
    private String addToSend="";

    public ErrorSimulator() {
        clientsTID=new ArrayList<Integer>();
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
            System.out.println("we are now waiting1");
            receiveSocket.receive(receivePacket);

        }
        catch(IOException e) {
            System.out.println("IOException on waiting");
            System.exit(1);
        }

        if(!clientsTID.contains(receivePacket.getPort())) {
            clientsTID.add(receivePacket.getPort());
            ErrorSimConnection errorSimulator = new ErrorSimConnection(receivePacket, modified, modifiedPackIndex, delayAmount,addToSend);
            errorSimulator.start();
        }else {

            ErrorSimConnection errorSimulator = new ErrorSimConnection(receivePacket, 0, modifiedPackIndex, delayAmount,addToSend);
            errorSimulator.start();
        }



    }

    private void userInput(Scanner scan)
    {
        System.out.println("Which Error would you like to simulate (4-6) or 0 for no Error : ");
        System.out.println("7 is iteration 4 errors (delays/duplicates/etc)");
        //useINETAddress();
        //Scanner scan = new Scanner(System.in);


        int choice = scan.nextInt();


        while(true) {
            if(choice==4) {
                System.out.println("ILLEGAL TFTP");
                System.out.println("Which Error would you like to simulate( [1:RRQ/WWR] [2:DATA] [3:ACK] ) : ");
                //Scanner errorScan = new Scanner(System.in);
                int errorToSimulate = scan.nextInt();


                if(errorToSimulate==1) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE]  [2:MODE] ) : ");
                    //Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();

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
                    //fieldScanner.close();
                }
                else if(errorToSimulate==2) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE] [2:DATABLOCK] ) : ");

                    //Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();
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



                    //fieldScanner.close();

                }
                else if(errorToSimulate==3) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:OPCODE] [2:DATABLOCK] ) : ");

                   //Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();
                    if(partToSimulate==1) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        System.out.println("Creating packet with OP CODE 11");
                        modified=431;
                    }
                    if(partToSimulate==2) {
                        System.out.println("You have chosen to simulate field : " + partToSimulate);
                        modified=432;

                    }

                    //fieldScanner.close();
                }
                //errorScan.close();

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
            if(choice==7) {

                System.out.println("Which Error would you like to simulate( [1:Lose a packet] [2:Delay a packet] [3:Duplicate a packet] ) : ");
               // Scanner errorScan = new Scanner(System.in);
                int errorToSimulate = scan.nextInt();
                if(errorToSimulate==1) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:RRQ/WRQ] [2:ACK] [3:DATA] ) : ");
                    //Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();
                    if(partToSimulate==1) {


                        modifiedPackIndex =0;
                        modified=611;



                    }

                    if(partToSimulate==2) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=613;
                       // blockNumScanner.close();


                    }
                    if(partToSimulate==3) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=612;

                        //blockNumScanner.close();

                    }

                   // fieldScanner.close();


                }
                if(errorToSimulate==2) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:RRQ/WRQ] [2:ACK] [3:DATA] ) : ");
                    //Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();
                    if(partToSimulate==1) {
                        //System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);
                        modified=621;
                        modifiedPackIndex = 0;

                        System.out.println("How long do you want to delay for ");
                        //Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                        //blockNumScanner.close();
                        //delayScanner.close();

                    }

                    if(partToSimulate==2) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=623;
                        System.out.println("How long do you want to delay for ");
                        //Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                        //blockNumScanner.close();
                        //delayScanner.close();

                    }
                    if(partToSimulate==3) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=622;
                        System.out.println("How long do you want to delay for ");
                        //Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                       // blockNumScanner.close();
                        //delayScanner.close();
                    }

                    //fieldScanner.close();


                }
                if(errorToSimulate==3) {
                    System.out.println("You have chosen to simulate error : " + errorToSimulate);
                    System.out.println("Which field would you like to change( [1:RRQ/WRQ] [2:ACK] [3:DATA] ) : ");
                   // Scanner fieldScanner = new Scanner(System.in);
                    int partToSimulate = scan.nextInt();
                    if(partToSimulate==1) {

                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex =0;
                        modified=631;
                        System.out.println("How long do you want to delay for ");
                       // Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                        //blockNumScanner.close();
                        //delayScanner.close();

                    }

                    if(partToSimulate==2) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=633;
                        System.out.println("How long do you want to delay for ");
                        //Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                        //blockNumScanner.close();
                        //delayScanner.close();

                    }
                    if(partToSimulate==3) {
                        System.out.println("Which block do you want the error to occur in ");
                        //Scanner blockNumScanner = new Scanner(System.in);

                        modifiedPackIndex = scan.nextInt();
                        modified=632;
                        System.out.println("How long do you want to delay for ");
                        //Scanner delayScanner = new Scanner(System.in);
                        delayAmount = scan.nextInt();
                        //blockNumScanner.close();
                        //delayScanner.close();
                    }

                    //fieldScanner.close();


                }
                //errorScan.close();

                break;

            }
        }
        //scan.close();
    }
    private boolean useINETAddress(Scanner scan) {
        boolean useOrNot;
        System.out.println("Would you like to use an inetAddress?");
        System.out.println("[Yes = 0] [No = 1]");
        //Scanner useInetScan = new Scanner(System.in);
        InetAddress netAdd = null;
        int choice1 = scan.nextInt();

        while(true) {
            if(choice1==0) {

                try {
                    netAdd = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Please enter the inetAddress");
                System.out.println("The current inetAddress is :" + netAdd );
                Scanner inetScanner = new Scanner(System.in);
                addToSend = inetScanner.nextLine();
                System.out.println(("You chose to send to " + addToSend));

                useOrNot = true;
                inetScanner.close();
                break;
            }
            else {
                System.out.println("We are using local");

                useOrNot = false;
                break;
            }

        }
        //useInetScan.close();
        return useOrNot;
    }

    public static void main( String args[] )
    {
        ErrorSimulator IH = new ErrorSimulator();
        Scanner scan = new Scanner(System.in);

        IH.userInput(scan);
        IH.useINETAddress(scan);
        scan.close();


        while(true) {
            IH.receiveAndSend();
            System.out.println();
        }

    }

}
