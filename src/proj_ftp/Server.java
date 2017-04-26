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
    FileOutputStream fos;
    int recvack=0;
    boolean download=true;
    
    Server(int portNumber,String fileName){
    	port=portNumber;
    	file=fileName;    	
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portNumber= Integer.parseInt(args[0]);
		String file=args[1];
		float prob=Float.parseFloat(args[2]);
		
        Server newServer=new Server(portNumber,file);
        System.out.println("Connection open. Waiting for file from client.");
        int len=0,currenttot=0;
        
        try{
        	newServer.sockServer = new DatagramSocket(portNumber); 
            byte[] receiveData;
            byte[] sendData  = new byte[10000000];
            float r;
            Random rs=new Random();
            FileOutputStream stream = new FileOutputStream(file);
            BufferedOutputStream bos=new BufferedOutputStream(stream);
            
            while(newServer.download) 
            { 
              receiveData = new byte[2048]; 
              DatagramPacket receivePacket =new DatagramPacket(receiveData, receiveData.length);
              newServer.sockServer.receive(receivePacket);
              byte [] db=new byte[receivePacket.getLength()-64];
              System.arraycopy(receiveData,64, db,0,db.length);
    		  //System.out.println(receivePacket.getLength());
              byte [] received=receivePacket.getData();
    		  //System.out.println("Data length of received packet=="+received.length);
              int seqno=CustomUtil.binaryToDecimalByte(received,32);
              String chck=new String(Arrays.copyOfRange(received, 32, 48));
              String type=new String(Arrays.copyOfRange(received,48,64));
              byte [] data=Arrays.copyOfRange(received, 64,received.length);
    		  //System.out.println("Bytes of actual data transferred"+data.length);
              r=rs.nextFloat();
              //System.out.println(r);
              if(r<=prob)
              {
                  System.out.println("Packet loss, Sequence number="+seqno);
                  continue;
              }
              newServer.ipAddr=receivePacket.getAddress();
              newServer.port=receivePacket.getPort();
              //System.out.println("Packet received from"+sfs.ip);
              String temp=CustomUtil.getChecksum(data);
              if(temp.equals(chck))
              {
                  //System.out.println("checksum validated");
                    if(newServer.recvack==seqno)
                    {
                    	newServer.recvack++;
                        //System.out.println("Packet is valid");
                        if(type.equals("0101010101010101"))
                        {
                            stream.write(db);
                            DatagramPacket ack=newServer.generate(newServer.recvack);
                            newServer.sockServer.send(ack);
                            //System.out.println("Acknowledgement sent for sequence no::"+sfs.recvack); 
                        }
                        else
                        	newServer.download=false;
                    }
               }
            }
          bos.flush();
          bos.close();
          System.out.println(newServer.file+" has been updated");
          
        }
        catch(SocketException e){
        	System.out.println("Error communicating with the port");
        }
        catch(Exception e){
        	System.err.println(e);
        }
	}
	
    DatagramPacket generate(int seq)
    {
        DatagramPacket p=null;
        String sequ=CustomUtil.getSequenceNo(seq);
        sequ+="00000000000000001010101010101010";
        byte []send=sequ.getBytes();
        try
        {
            p=new DatagramPacket(send,send.length,ipAddr,port);
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
        return p;
    }

}
