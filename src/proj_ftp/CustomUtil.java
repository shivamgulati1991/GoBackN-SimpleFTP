package proj_ftp;

import java.util.Arrays;

public class CustomUtil {
	   public static String getSequenceNo(int n)
	    {
	        String temp=Integer.toBinaryString(n);
	        for(int i=temp.length();i<32;i++)
	            temp="0"+temp;
	        return temp;
	    }
	   
	    public static String getChecksum(byte [] b)
	    {
	       byte sum1=0,sum2=0;
	       for(int i=0;i<b.length;i=i+2)
	       {
	           sum1+=b[i];
	          if((i+1)<b.length)
	            sum2+=b[i+1];
	       }
	       String res=Byte.toString(sum1),res1=Byte.toString(sum2);
	       for(int i=res.length();i<8;i++)
	           res="0"+res;
	       for(int i=res1.length();i<8;i++)
	           res1="0"+res1;
	       return res+res1;
	    }
	    
	    public static int binaryToDecimal(String str)
	    {
	    	return Integer.parseInt(str, 2);
	    }
	    
	    public static int binaryToDecimalByte(byte [] st){
	    	String str=new String(Arrays.copyOfRange(st, 0, 32)); 
	        double j=0;
	        for(int i=0;i<str.length();i++){
	            if(str.charAt(i)== '1'){
	             j=j+ Math.pow(2,str.length()-1-i);
	         }
	        }
	        return (int) j;
	    }    
}
