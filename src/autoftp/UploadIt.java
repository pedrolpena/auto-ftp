/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;

import java.io.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTP;
import java.net.SocketException;

public class UploadIt 
{
    FTPClient ftp;
    File localFile2;
    String remoteFile2;
    javax.swing.JTextArea j2;
    boolean dirExists;
    AutoFTP autoFTPClass;
    SocketFactoryForFTPClient ssf;    

    public UploadIt()
    {
        ftp = new FTPClient();
        ssf = new SocketFactoryForFTPClient();
        ftp.setSocketFactory(ssf);
        ftp.setConnectTimeout(10000);
    }

public boolean connectToSite(String server) throws SocketException,IOException
{

    int reply;
    boolean success=false;  
    ftp.connect(server);  
    reply=ftp.getReplyCode();  
    if(reply >= 200 && reply <=300) 
    {      
        success=true;     
    }//end if

    return success;
}// end connectosite


    public boolean connectToSite(String server, int attempts) {

        int reply;
        boolean success = false;
        for (int i = 0; i < attempts; i++) {
            try {
                Thread.sleep(1000);
                ftp.connect(server);
                Thread.sleep(1000);
                reply = ftp.getReplyCode();
                if (reply >= 200 && reply <= 300) {
                    success = true;
                    return success;
                }//end if
            } catch (Exception e) {
                autoFTPClass.statusMessage("Unable to connect to " + server + " , attempting to connect  "+ (attempts-i-1) + " more times");

                //e.printStackTrace();
            }//end catch
            
        }//end for
        return success;
    }// end connectosite



public boolean binary() throws IOException
{
    return ftp.setFileType(FTP.BINARY_FILE_TYPE);   
}// end binary


public boolean login(String userName, String password) throws IOException
{    
    return ftp.login(userName, password);
}// end login


public boolean closeConnection() throws IOException
{
    ftp.disconnect();    
    return ftp.isConnected();
}//end close connection


 public boolean isConnected()
 {
     return ftp.isConnected();
 }// end isconnected


 void setIridiumFTP(AutoFTP iFTP){
    autoFTPClass=iFTP;

 }
 
 void enterLocalPassiveMode()
 {
     ftp.enterLocalPassiveMode();
 }//end method
 

	
 public int size(String x) throws IOException
 {
     int numOfBytes =-1,
             replyCode;     
     String reply;     
     ftp.sendCommand("SIZE " + x);
     reply = ftp.getReplyString();
     replyCode = ftp.getReplyCode();
     if(replyCode > 200 && replyCode < 300)
     {
         numOfBytes = (new Integer((reply.split(" "))[1].trim()).intValue());
     }//end if
     return numOfBytes;            
 }//ebd size()

    public boolean uploadFile (File file, String serverFile) throws IOException 
    {		

        boolean success;
        String response;
        FileInputStream in = new FileInputStream(file); 
             
        if(autoFTPClass.getDirExists())       
        {            
            success = ftp.storeFile(serverFile, in);
            response=ftp.getReplyString();
            
            if(response.toLowerCase().contains("no such file or directory"))
            {
                autoFTPClass.setDirExists(false);
                success= ftp.storeFile("/default/"+file.getName(),in);                
            }//end if
            
        }//end if

        else

        {           
            success= ftp.storeFile("/default/"+file.getName(),in);                                        
        }//end else             

        
        in.close();
        return success;	
    }// end upload file

    
    public boolean appendFile (File file, String serverSideFile) throws IOException 
    {
        String serverFile = serverSideFile;
        int startByte, 
                fileSize,
                existingFileSize; 
        if(!autoFTPClass.getDirExists())
        {
            startByte = size("/default/"+file.getName()); 
        }//end if
        else
        {
            startByte = size(serverFile);  
        }//end else
             
        fileSize = (int)file.length();
        existingFileSize = startByte;
        boolean success =  false; 

        if (startByte < fileSize && startByte >= 0) 
        {    
            autoFTPClass.statusMessage(file.getName()+" found with " + startByte +" bytes\n");
            autoFTPClass.statusMessage("Resuming interrupted upload\n");
            RandomAccessFile raf = new RandomAccessFile(file,"r");
            byte[] bytesIn = new byte[fileSize - startByte];
            ByteArrayInputStream in;                             
            raf.seek(startByte);
            raf.read(bytesIn);
            in = new ByteArrayInputStream(bytesIn);
            autoFTPClass.statusMessage("Attempting to send remaining " + (fileSize - startByte) + " bytes\n");
            
            if(autoFTPClass.getDirExists())       
            {            
                success = ftp.appendFile(serverFile, in);
            }//end if
            else
            {           
                success= ftp.appendFile("/default/"+file.getName(),in);                                       
            }//end else 
            raf.close();	  
        }// end if
        
        if(fileSize==existingFileSize)
        {    
            autoFTPClass.statusMessage(file.getName()+ " is already on the server\n");
   
        }
        if(startByte == fileSize)
        {
            success = true;
        }
        if(startByte > fileSize)
        {   
            autoFTPClass.statusMessage("oddly this file is smaller\nthan the one on the server\nreplacing "+file.getName()+" with smaller one \n");
            success = uploadFile(file,serverFile);
        }
        if(startByte == -1 )
        {    
            autoFTPClass.statusMessage("First attempt\n");
            success = uploadFile(file,serverFile);   
        }// end if
        return success; 
    }// end append file    

    public void closeAllSockets()
    {
        ssf.closeAllConnections();
    
    }
    


}// end class
