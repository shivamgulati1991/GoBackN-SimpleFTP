package proj_ftp;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;

public class Server {
    DatagramSocket sockServer;
    int port;
    InetAddress ipAddr;
    String file;
    int ackRcvd=0;
    boolean checkTransfer=true;
    
    //constructor to setup server
    Server(int portNumber,String fileName){
    	port=portNumber;
    	file=fileName;    	
    }
    
    DatagramPacket genPacket(int seq)
    {
        DatagramPacket dataPacket=null;
        String seqString=CustomUtil.getSequenceNo(seq);
        seqString+="00000000000000001010101010101010";
        byte []send=seqString.getBytes();
        try
        {
        	dataPacket=new DatagramPacket(send,send.length,ipAddr,port);
        }
        catch(Exception e)
        {
            System.out.println("An error occured.");
            System.err.println(e);
        }
        return dataPacket;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portNumber= Integer.parseInt(args[0]);
		String file=args[1];
		float prob=Float.parseFloat(args[2]);
		
		//start the server with user input port number
        Server newServer=new Server(portNumber,file);
        System.out.println("Connection open. Waiting for file from client.");
        
        try{
        	//setup the connection
        	newServer.sockServer = new DatagramSocket(portNumber); 
            byte[] recData; float rand;
            Random rs=new Random();
            
            //open output stream and buffer
            FileOutputStream stream = new FileOutputStream(file);
            BufferedOutputStream bos=new BufferedOutputStream(stream);
            
            //check if the transfer is complete or not
            while(newServer.checkTransfer) 
            { 
              recData = new byte[2048]; 
              DatagramPacket receivePacket =new DatagramPacket(recData, recData.length);
              newServer.sockServer.receive(receivePacket);
              
              byte [] newTemp=new byte[receivePacket.getLength()-64];
              System.arraycopy(recData,64, newTemp,0,newTemp.length);
              byte [] received=receivePacket.getData();
              
              int seqno=CustomUtil.binaryToDecimalByte(received);
              String chck=new String(Arrays.copyOfRange(received, 32, 48));
              String isData=new String(Arrays.copyOfRange(received,48,64));
              
              byte [] data=Arrays.copyOfRange(received, 64,received.length);
              rand=rs.nextFloat();
              if(rand<=prob)
              {
                  System.out.println("Packet loss, Sequence number: " + seqno);
                  continue;
              }
              //get IP and port
              newServer.ipAddr=receivePacket.getAddress();
              newServer.port=receivePacket.getPort();
              //verify checksum
              String temp=CustomUtil.getChecksum(data);
              if(temp.equals(chck))
              {
                    if(newServer.ackRcvd==seqno)
                    {
                    	newServer.ackRcvd=newServer.ackRcvd+1;
                        if(isData.equals("0101010101010101"))
                        {
                            stream.write(newTemp);
                            DatagramPacket ack=newServer.genPacket(newServer.ackRcvd);
                            newServer.sockServer.send(ack);
                        }
                        else
                        	newServer.checkTransfer=false;
                    }}}
            //close the buffers
          bos.flush(); bos.close();
          System.out.println("File has been recieved");        
        }
        catch(SocketException e){
        	System.out.println("Error communicating with the port");
        }
        catch(Exception e){
        	System.err.println(e);
        }
	}
}
