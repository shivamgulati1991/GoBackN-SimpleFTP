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
	       byte x1=0,x2=0;
	       for(int i=0;i<b.length;i=i+2)
	       {
	           x1+=b[i];
	           if((i+1)<b.length)
	           {
	        	   x2+=b[i+1];
	           }
	       }
	       String result=Byte.toString(x1),result1=Byte.toString(x2);
	       for(int i=result.length();i<8;i++){
	    	   result="0"+result;
	       }
	           
	       for(int i=result1.length();i<8;i++){
	    	   result1="0"+result1;
	       }
	       return result+result1;
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
