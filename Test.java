import java.io.*;
public class Test { 
	public static void main( String args[] )
	{
		OutputStream os = null;

	      
	      try {
	    	  os = new FileOutputStream("C:\\Users\\michaelwang3\\Desktop\\server\\Test.txt");
	         byte[] data      = new byte[2049];
	         for(int i =0;i <2049;i++)
	         {
	        	 data[i]=(i+"").getBytes()[0];
	         }
	         os.write(data);
	         os.close();
	       }

	         
	         
	       catch(Exception e) {
	         

	         e.printStackTrace();
	      } 
	}
	
}
