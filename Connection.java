import java.io.*;
import java.net.*;


class Connection extends Thread
{
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private  String path="C:\\Users\\michaelwang3\\Desktop\\server\\";
    //private final String path="E:\\";
    private InputStream is = null;
    private OutputStream os = null;
    private String fileName="";
    private byte[] msg=new byte[4];
    private int blockCount=1;
    private byte[] data;
    private boolean overWritten=true;
    private int connectionID;
    private byte [][] fullFileData;
    private int errorCode=8;
    private int fileNameLen;
    private byte[] netascii = "netascii".getBytes();
    private int errorSimulatorPort=0;
    private String errorMsg;
    private byte[] previousPacket = new byte[4];
    private boolean dupPacket = false;
    private int hostPort=0;
    private boolean duplicateRQ=false;
    private int timeout=5000;
    private int maxAttempt=5;
    private int curtAttempt=0;
    public Connection(DatagramPacket packet, int connectionID,String path)
    {
        System.out.println("Connection"+connectionID+" been created!");
        this.connectionID=connectionID;
        this.hostPort=packet.getPort();
        this.path=path;

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
        if(errorSimulatorPort==0)
            errorSimulatorPort=receivePacket.getPort();
        errorCheck();
        if(!dupPacket)
            storeOpcode();
        if(errorCode==8) {
            while (true) {
                if (getOpcode() == 0 ) {

                    break;
                }
                else if (getOpcode() == 3) {

                    System.out.println("Connection" + connectionID + " received data package "+data[2]+data[3]+"(Length ="+receivePacket.getLength()+"): "+new String(data));
                    if(!dupPacket) {
                        byte[] toFile = new byte[data.length - 4];
                        for (int i = 0; i < data.length - 4; i++)
                            toFile[i] = data[i + 4];
                        toFile = trimByteArr(toFile);


                        fileIO(2, toFile);
                        if (errorCode != 8) {
                            System.out.println("Connection" + connectionID + " shuts down");
                            return;
                        }
                    }
                    else
                    {
                        System.out.println("Connection" + connectionID + " gets duplicate data package!");
                    }
                    constructArray();
                    if(!dupPacket)
                        blockCount++;
                    sending(msg);

                    if(receivePacket.getLength()<516)
                    {
                        
                        break;
                    }
                }
                else if(dupPacket)
                {
                    System.out.println("duplicate package!");

                    if(receiving(data)==-1)
                        break;

                    continue;
                }
                else if (getOpcode() == 5) {
                    System.out.println("Connection" + connectionID + " gets an error from client");
                    System.out.println(new String(data).substring(4,data.length-1));
                    System.out.println("Connection"+connectionID+" shuts down");
                    sendReceiveSocket.close();
                    return;
                }
                else if (getOpcode() == 2)    //Writing request
                {
                    System.out.println("Connection" + connectionID + " received write request");
                    duplicateRQ=true;
                    blockCount = 0;
                    constructArray();
                    blockCount = 1;
                    sending(msg);
                } else if (getOpcode() == 1)   //Reading request
                {
                    //int numPack;    //finding out how many time need to send the whole file
                    System.out.println("Connection" + connectionID + " received read request");
                    duplicateRQ=true;

                    //byte[] fileData = new byte[512];

                    fileIO(1, null);

                    if(errorCode!=8) {
                        System.out.println("Connection" + connectionID + " shuts down");
                        return;
                    }

                    int blockNum = 1;
                    boolean toBreak=true;
                    while (true) {
                        while ( dupPacket)
                        {
                            System.out.println("duplicate package!!");
                            int temp=receivingTimeout(data);
                            while(temp==0) {
                                temp=receivingTimeout(data);
                            }
                            if(temp==-1)
                                break;
                            continue;
                        }
                        if (blockNum  == fullFileData.length) {
                            System.out.println("sending block num " + blockNum);
                            sending(createDataPacket(3, blockNum, fullFileData[fullFileData.length - 1]));
                            data = new byte[4];

                            DatagramPacket tempPack=receivePacket;

                            curtAttempt=0;
                            int temp=receivingTimeout(data);
                            while(temp==0)
                            {
                                sending(createDataPacket(3, blockNum, fullFileData[fullFileData.length - 1]),tempPack);
                                curtAttempt++;
                                if(curtAttempt>=maxAttempt) {
                                    System.out.println("Lost connection, connection " + connectionID + " shuts down");
                                    sendReceiveSocket.close();
                                    return;
                                }
                                temp=receivingTimeout(data);
                            }
                            if(temp==-1)
                                toBreak=true;
                            else
                                toBreak=false;




                            if(toBreak)
                            {
                                System.out.println("Connection" + connectionID + " shuts down");
                                sendReceiveSocket.close();
                                break;
                            }
                            if(data[1]==(byte)5)
                            {
                                System.out.println("Connection" + connectionID + " gets an error from client");
                                System.out.println(new String(data).substring(4,data.length-1));
                                System.out.println("Connection"+connectionID+" shuts down");
                                sendReceiveSocket.close();
                                return;
                            }
                            else {
                                System.out.println("Connection" + connectionID + " received ACK"+data[2]+data[3]);
                                break;
                            }
                        } else {
                            System.out.println("sending block num " + blockNum);
                            sending(createDataPacket(3, blockNum, fullFileData[blockNum-1]));

                            data = new byte[4];
                            //System.out.println("blockCount= "+blockCount);


                            DatagramPacket tempPack=receivePacket;


                            curtAttempt=0;
                            int temp=receivingTimeout(data);
                            while(temp==0)
                            {
                                sending(createDataPacket(3, blockNum, fullFileData[blockNum-1]),tempPack);
                                curtAttempt++;
                                if(curtAttempt>=maxAttempt) {
                                    System.out.println("Lost connection, connection " + connectionID + " shuts down");
                                    sendReceiveSocket.close();
                                    return;
                                }
                                temp=receivingTimeout(data);
                                
                            }
                            if(temp==-1)
                                toBreak=true;
                            else
                                toBreak=false;







                            while(dupPacket)
                            {
                                System.out.println("duplicate Ack!!!");
                                temp=receivingTimeout(data);
                            }
                            




                            blockNum++;
                            blockCount=blockNum;
                            if(toBreak)
                            {
                                System.out.println("Connection" + connectionID + " shuts down");
                                sendReceiveSocket.close();
                                break;
                            }
                            if(data[1]==(byte)5)
                            {
                                System.out.println("Connection" + connectionID + " gets an error from client");
                                System.out.println("Connection"+connectionID+" shuts down");
                                sendReceiveSocket.close();
                                return;
                            }
                            else
                                System.out.println("Connection" + connectionID + " received ACK"+data[2]+data[3]);
                        }
                    }
                    if(toBreak)
                        break;


                    break;


                }
                data = new byte[516];
                if(!timeoutReceive())
                    break;

            }
        }
        else
        {
            System.out.println("Connection" + connectionID + " gets an "+errorMsg);
            data=new byte[5+errorMsg.getBytes().length];
            data[0]=(byte)0;
            data[1]=(byte)5;
            data[2]=(byte)0;
            data[3]=(byte)errorCode;
            data[data.length-1]=(byte)0;
            System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
            sending(data);

        }
        System.out.println("Connection"+connectionID+" shuts down");
        sendReceiveSocket.close();
    }
    private synchronized void fileIO(int readWrite,byte[] data)
    {
        if(readWrite==1) //reading
        {

            int numPack;    //finding out how many time need to send the whole file
            byte[] fileData;
            System.out.println("Reading file from: " + (path + fileName));
            File file = new File(path + fileName);
            int size = (int) file.length();
            System.out.println("File size: "+size);
            numPack = (int) Math.ceil(size / 512.0);
            int finalPacket = 0;
            if (numPack != 0)
                finalPacket = size % 512;



            if(finalPacket==0)
                numPack++;
               // fullFileData=new byte[numPack+1][512];

            //else
                fullFileData=new byte[numPack][512];

            try {
                is = new FileInputStream( (path + fileName));
                for(int i =0;i<numPack-1;i++){
                    fileData = new byte[512];
                    is.read(fileData);
                    //System.out.println("file1= "+new String(fileData));
                    fullFileData[i]=fileData;
                }
                //System.out.println("file2= "+new String(fullFileData[0]));
                System.out.println("numPack= "+numPack);
                fileData = new byte[finalPacket];
                is.read(fileData);
                /*if(finalPacket==0)
                    fullFileData[numPack]=fileData;
                else*/
                    fullFileData[numPack-1]=fileData;
                //System.out.println("file3= "+new String(fullFileData[0]));
                is.close();

            }
            catch (FileNotFoundException e)
            {
                if(e.toString().substring(e.toString().length()-42).compareTo("The system cannot find the file specified)")==0)
                {
                   
                    errorMsg="ERROR:File not found!";
                    System.out.println("Connection" + connectionID + " gets an "+errorMsg);
                    errorCode=1;
                    data=new byte[5+errorMsg.getBytes().length];
                    data[0]=(byte)0;
                    data[1]=(byte)5;
                    data[2]=(byte)0;
                    data[3]=(byte)errorCode;
                    data[data.length-1]=(byte)0;
                    System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
                    sending(data);


                }
                else
                {
                    e.printStackTrace();

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

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
                    receivePacket.getAddress(), errorSimulatorPort);
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
       

    }
    private void sending(byte[] data,DatagramPacket pack)
    {
        String str = new String(data,0,data.length);
        System.out.println("Connection"+connectionID+" sending package= "+str);

        try {
            sendPacket = new DatagramPacket(data, data.length,
                    pack.getAddress(), errorSimulatorPort);
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
       

    }
    private void writing(byte[] data)
    {



    /*byte[] newData = resize;
    resize = new byte[resize.length + data.length];
    System.arraycopy(newData, 0, resize, 0, newData.length);
    System.arraycopy(data, 0, resize, newData.length, data.length);
*/
        try
        {
            FileOutputStream out = new FileOutputStream(path+fileName,!overWritten);
            overWritten=false;
            out.write(data);

            out.close();
        }
        catch (FileNotFoundException e)
        {
            if(e.toString().substring(e.toString().length()-17).compareTo("Access is denied)")==0)
            {
                
                errorMsg="ERROR:Access is denied!!";
                System.out.println("Connection" + connectionID + " gets an "+errorMsg);
                data=new byte[5+errorMsg.getBytes().length];
                data[0]=(byte)0;
                data[1]=(byte)5;
                data[2]=(byte)0;
                data[3]=(byte)2;
                data[data.length-1]=(byte)0;
                System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
                sending(data);
                errorCode=10;
            }
            else
            {
                e.printStackTrace();

            }
        }
        catch(IOException e) {
            if(e.toString().substring(e.toString().length()-37).compareTo("There is not enough space on the disk")==0)
            {
                
                errorMsg="ERROR:There is not enough space on the disk";
                System.out.println("Connection" + connectionID + " gets an "+errorMsg);
                data=new byte[5+errorMsg.getBytes().length];
                data[0]=(byte)0;
                data[1]=(byte)5;
                data[2]=(byte)0;
                data[3]=(byte)3;
                data[data.length-1]=(byte)0;
                System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
                sending(data);
                errorCode=11;
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


    }
    private int receivingTimeout (byte[] data)
    {

        receivePacket = new DatagramPacket(data, data.length);
        
        try {
            sendReceiveSocket.setSoTimeout(timeout);
            sendReceiveSocket.receive(receivePacket);
           
        }
        catch(SocketTimeoutException e) {
            System.out.println("TIME OUT!");
            return 0;
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if(receivePacket.getPort()!=errorSimulatorPort)
        {
            System.out.println("Connection" + connectionID + " gets an UNKNOWN TID "+receivePacket.getPort());
        }
        if((data[1]==1||data[1]==2)&&duplicateRQ&&data[0]==0)
        {
            System.out.println("Connection" + connectionID + " gets a duplicate request");

            return receiving(data);
        }
        errorCheck();
        if(!dupPacket)
            storeOpcode();
        if(errorCode!=8)
        {

            System.out.println("Connection" + connectionID + " gets an "+errorMsg);
            data=new byte[5+errorMsg.getBytes().length];
            data[0]=(byte)0;
            data[1]=(byte)5;
            data[2]=(byte)0;
            data[3]=(byte)errorCode;
            data[data.length-1]=(byte)0;
            System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
            sending(data);
            //sendReceiveSocket.close();
            return -1;
        }
        return 1;
        

    }
    private int receiving (byte[] data)
    {

        receivePacket = new DatagramPacket(data, data.length);
        //sendReceiveSocket.receive(receivePacket);
        try {
            sendReceiveSocket.setSoTimeout(0);
            sendReceiveSocket.receive(receivePacket);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if(receivePacket.getPort()!=errorSimulatorPort)
        {
            System.out.println("Connection" + connectionID + " gets an UNKNOWN TID "+receivePacket.getPort());
        }
        if((data[1]==1||data[1]==2)&&duplicateRQ&&data[0]==0)
        {
            System.out.println("Connection" + connectionID + " gets a duplicate request");

            return receiving(data);
        }
        errorCheck();
        if(!dupPacket)
            storeOpcode();
        if(errorCode!=8)
        {

            System.out.println("Connection" + connectionID + " gets an "+errorMsg);
            data=new byte[5+errorMsg.getBytes().length];
            data[0]=(byte)0;
            data[1]=(byte)5;
            data[2]=(byte)0;
            data[3]=(byte)errorCode;
            data[data.length-1]=(byte)0;
            System.arraycopy(errorMsg.getBytes(),0,data,4,errorMsg.getBytes().length);
            sending(data);
            //sendReceiveSocket.close();
            return -1;
        }
        return 1;
       

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
        fileNameLen = fileName.length();
    }

    private void errorCheck()
    {
        if (receivePacket.getData()[0] != (byte)0)
        {
            errorCode = 4;
            errorMsg = "Error Code 04: The first value of the packet is invalid";
        }
        else if(getOpcode() > 5)
        {
            errorCode = 4;
            errorMsg = "Error Code 04: The Opcode is invalid";
        }
        else if(getOpcode() == 1 || getOpcode() == 2)
        {
            checkDup();
            if(!dupPacket) {
                parseData();
                byte[] fileNameByte = fileName.getBytes();
                for (int i = 0; i < fileNameLen; i++) {
                    if (fileNameByte[i] > 127 || fileNameByte[i] < 0) {
                        errorCode = 4;
                        errorMsg = "Error Code 04: There is an invalid character in the file name";
                    }
                }
                if (receivePacket.getData()[2 + fileNameLen] != (byte) 0) {
                    errorCode = 4;
                    errorMsg = "Error Code 04: The mode of the packet is invalid";
                }
                int k = 0;
                int modeLen = 0;
                while (receivePacket.getData()[3 + fileNameLen + k] != (byte) 0) {
                    k++;
                    modeLen++;
                }

                if (modeLen != 8) {
                    errorCode = 4;
                    errorMsg = "Error Code 04: The mode of the packet is invalid";
                } else {
                    for (int j = 0; j < 8; j++) {
                        if (netascii[j] != receivePacket.getData()[3 + fileNameLen + j]) {
                            errorCode = 4;
                            errorMsg = "Error Code 04: The mode of the packet is invalid";
                        }
                    }

                    if (receivePacket.getLength() != (4 + 8 + fileNameLen)) {

                        errorCode = 4;
                        errorMsg = "Error Code 04: There is nothing that ends the packet";
                    } else if (receivePacket.getData()[receivePacket.getData().length - 1] != (byte) 0) {
                        errorCode = 4;
                        errorMsg = "Error Code 04: There is an illegal ending to the packet";
                    }
                }
            }
        }
        else if(getOpcode() == 3 || getOpcode() == 4) {
            checkDup();
            if (!dupPacket) {
                int tensDigit, onesDigit;
                if (blockCount < 10) {
                    tensDigit = 0;
                    onesDigit = blockCount;
                } else {
                    tensDigit = blockCount / 10;
                    onesDigit = blockCount % 10;
                }

                if (receivePacket.getData()[2] != tensDigit) {
                    errorCode = 4;
                    errorMsg = "Error Code 04: The data block that was sent is not the subsequent one from the previous data block";
                }
                if (receivePacket.getData()[3] != onesDigit) {
                    errorCode = 4;
                    errorMsg = "Error Code 04: The data block that was sent is not the subsequent one from the previous data block";
                }
            }
        }
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
    public boolean timeoutReceive()
    {
        curtAttempt=0;
        int temp=receiving(data);
        while(temp==0)
        {
            curtAttempt++;
            if(curtAttempt>=maxAttempt) {
                System.out.println("Lost connection, connection " + connectionID + " shuts down");
                return false;
            }
            temp=receiving(data);
        }
        if(temp==-1)
            return false;
        return true;
    }



}