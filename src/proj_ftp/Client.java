/*
 * References taken from StackOverflow community for socket connections, 
 * FTP concepts, checksum computation
 */
package proj_ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Client {
	String hostname;
	int port,n,mss,ack=0;
	File filename;
	InetAddress ipAddr;
	byte[][] fileStream;
	int count=0,buffer=0; long rtt=0;
	DatagramSocket sockClient;
	boolean isFinished=false;
	
	//constructor to set up client
	Client(String hostName, int serverPort,String fileName, int N,int MSS){
		hostname=hostName;
		port=serverPort;
		n=N;
		mss=MSS;		
		try{
			sockClient=new DatagramSocket();
			filename=new File(fileName);
			ipAddr=InetAddress.getByName(hostname);
		}
		catch(Exception e){
			System.out.println("An error occured.");
			System.err.println(e);
		}
	}
	
	DatagramPacket rdt_send(int counter){
		String seq = CustomUtil.getSequenceNo(counter),chksum,isdata="0101010101010101";
		chksum=CustomUtil.getChecksum(fileStream[counter]);
		String header=seq+chksum+isdata;
		byte[] data;
		data=header.getBytes();
	       byte[] packet= new byte[mss+data.length];
	       for(int i=0;i<packet.length;i++)
	       {
	           if(i<data.length)
	        	   packet[i]=data[i];
	           else
	        	   packet[i]=fileStream[counter][i-data.length];
	       }
	       return new DatagramPacket(packet,packet.length,ipAddr,port);
	}
	
	public static void main(String[] args){
		
		//take user input for host,port,N and MSS values
		String hostName= args[0];
		int port=Integer.parseInt(args[1]);
		String fileName=args[2];
		int N=Integer.parseInt(args[3]);
		int MSS=Integer.parseInt(args[4]);
		
		//setup the client with input values
		Client newClient= new Client(hostName,port,fileName,N,MSS);
		newClient.prepareInputFile();
		System.out.println("Sending to server");
		
		//start the timer for computation of transfer
		long startTimer=System.currentTimeMillis();   
		//initiate transfer to server
		newClient.sendFileToServer();
		//end the timer for computation of transfer
		long endTimer=System.currentTimeMillis();
		
		//Display the delay to console
		System.out.println("Delay time in milliseconds: "+(endTimer-startTimer));
		
		System.out.println("File has been sent to the server.");
	}
	
	public void prepareInputFile()
    {
		System.out.println("Getting file size");
        int i=(int)filename.length()/mss;

        int j;
        int x,y;
        System.out.println("Size of the file is: "+filename.length()+" bytes");
        fileStream=new byte[i+1][mss];
        try
        {
            byte [] bytearray  = new byte [(int)filename.length()];
            FileInputStream fin = new FileInputStream(filename);
            BufferedInputStream input = new BufferedInputStream(fin);
            input.read(bytearray,0,bytearray.length);
            //fill the fileStream array values
            for(j=0;j<bytearray.length;j++)
            {
            	x=j/mss;
            	y=j%mss;
            	fileStream[x][y]=bytearray[j];
            }
            i=j/mss;
            while(j%mss!=0)
            {
                j++;
                fileStream[i][j%mss]=0;
            }
            input.close();
        }
        catch(FileNotFoundException e)
        {
        	System.out.println("File not found");
        }
        catch(IOException ie)
        {
        	System.out.println("IO error occured.");
            System.err.println(ie);
        }
    }
	
	void sendFileToServer()
    {    
		int fileLength=(int) filename.length();
        while( (count*mss) < fileLength )
        {
        	//check if buffer is less than window size and stream length > count
            while(buffer<n && fileStream.length>count)
            {
                DatagramPacket send=rdt_send(count);
                try
                {
                    sockClient.send(send);
                    buffer++;
                    count++;
                }
                catch(IOException e)
                {
                    System.err.println(e);
                }
            }
            byte [] rec=new byte[1024];
            DatagramPacket recPacket=new DatagramPacket(rec,rec.length);
            isFinished=false;
            try
            {
            	sockClient.setSoTimeout(100);
                while(isFinished==false)
                {
                	sockClient.receive(recPacket);
                    String getPacketBack=new String(recPacket.getData());
                    if(getPacketBack.substring(48,64).equals("1010101010101010"))
                    {
                        String seqtemp=getPacketBack.substring(0,32);
                        int seqNum=CustomUtil.binaryToDecimal(seqtemp);
                        ack=seqNum;
                        //check if acknowledgement and count are same to see if all packets sent
                        if(ack==count)
                        {
                        	isFinished=true;
                            count=ack;
                            buffer=0;
                            break;
                        }}}}
            catch(SocketTimeoutException sto)
            {
                System.out.println("Timeout, Sequence number: "+ack);
                buffer=count-ack; count=ack;
            }
            catch(IOException ioe)
            {
                System.err.println(ioe);
            }
        }
        
      //End the file
        try
        {        	
           String temp=Integer.toBinaryString(count);
           int i=temp.length();
           while(i<32){
        	   temp="0"+temp;
        	   i++;
           }

            byte b[]=new byte[mss];
            String check=CustomUtil.getChecksum(b);
            String endFile=temp+check+"0000000000000000"+(new String(b));
            byte b1[]=endFile.getBytes();
            DatagramPacket p=new DatagramPacket(b1,b1.length,ipAddr,7735);
            sockClient.send(p);
        }
        catch(IOException e){
            System.err.print(e);
        }                           
    }    
}