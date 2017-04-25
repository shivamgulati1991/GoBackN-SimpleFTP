package proj_ftp;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FtpClient {
	String hostname="";
	int port,n,mss;
	int ack=0;
	File filename;
	InetAddress ipAddr;
	byte[] data="helloworld1234".getBytes();
	DatagramSocket sockClient;
	FtpClient(String hostName, int serverPort,String fileName, int N,int MSS){
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
			System.err.println(e);
		}
	}
	
	DatagramPacket rdt_sent(){
		DatagramPacket dp = null;
		String header,seq = null,checksum = null,isdata;
		isdata="0101010101010101";
		
		header=seq+checksum+isdata;
		return dp;
	}
	
	public static void main(String[] args){
		FtpClient newClient= new FtpClient(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]),Integer.parseInt(args[4]));
		
	}
}
