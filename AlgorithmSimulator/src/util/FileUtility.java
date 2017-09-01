package util;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility {
	public static File[] getListOfFile(String path){
		if(path==null)
			return null;
		File folder = new File(path);
		return folder.listFiles();
	}
	
	public static String getFileExtension(File file) {
	    String name = file.getName();
	    String ext = "";
	    try {
	    	ext= name.substring(name.lastIndexOf(".") + 1);
	    	ext = ext.toLowerCase();
	    } catch (Exception e) {
	    	ext= "";
	    }
	    return ext.toLowerCase();
	}
	
	public static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	public static void openFile(String filename){
		 try {

	      		File file = new File(filename);
	      		if (file.exists()) {

	      			if (Desktop.isDesktopSupported()) {
	      				Desktop.getDesktop().open(file);
	      			} else {
	      				System.out.println("Awt Desktop is not supported!");
	      			}

	      		} else {
	      			System.out.println("File is not exists!");
	      		}

	      		System.out.println("Done");

	      	  } catch (Exception ex) {
	      		ex.printStackTrace();
	      	  }
	}

	public static boolean makeFolder(String filename){
		  File dir = new File(filename);
		  return dir.mkdir();
	}
	
	public static void writeWordsToText(String text,String filename){
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter( new FileWriter(filename));
			String updatedWords = text.replaceAll("\\r?\\n", System.lineSeparator());
		    writer.write( updatedWords);
		}
		catch ( IOException e)
		{
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e)
		    {
		    }
		}
	}
	
	public static boolean moveFiles(File file,String newDir){
	  File dir = new File(newDir);
	  if(!dir.exists()){
		  boolean successful = dir.mkdir();
		  if (!successful){
			   return false;
		  }
	  }
	  if(!file.renameTo(new File(newDir+"\\"+file.getName()))){
    		 return false;
	  }
	  return true;
	}
}
