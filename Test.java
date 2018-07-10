import java.io.*;
public class Test {
	public static void main( String args[] )
	{
		InputStream is = null;
		OutputStream os = null;
	      char c;
	      
	      try {
	      
	         // new input stream created
	         //is = new FileInputStream("C://Users//michaelwang3//eclipse-workspace/Project//src//file.txt");
	    	  os = new FileOutputStream("C:\\Users\\michaelwang3\\Desktop\\server\\file.txt");
	         is = new FileInputStream("C:\\Users\\michaelwang3\\Desktop\\Test.txt");
	         System.out.println("Characters printed(IS):");
	         	      
	         byte[] data      = new byte[1024];
	         int    bytesRead = is.read(data);
	         
	         while(bytesRead != -1) {
	        	 System.out.println("lenght= "+bytesRead);
	        	 for(int i=0;i<data.length;i++) {System.out.print((char)data[i]);}
	        	 
	        	 
	        	 
	        	 os.write(data);
	        	 
	        	 
	        	 
	        	 data      = new byte[1024];
	        	  bytesRead = is.read(data);
	        	  System.out.println();
	        	}
	         is.close();
	         os.close();
	         
	      } catch(Exception e) {
	         
	         // if any I/O error occurs
	         e.printStackTrace();
	      } finally {
	         
	         // releases system resources associated with this stream
	       //  if(is!=null)
	       //     is.close();
	      }
	}
	
}
