/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;
import java.io.*;
import java.net.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author pedro
 */
public class CommandClient extends Thread{
    static String address;
    static int port;
    Socket s;
    boolean running = true;
    Preferences prefs = Preferences.userNodeForPackage(getClass());

    public CommandClient(String a , int p)

    {
        address = a;
        port = p;
        try
        {
            s = new Socket(address,port);
        }
        catch(Exception e)
        {
            running =false;
            
        
        }//end catch

    }//end constructor

    public void run()
        {
            
          if( s!=null)
          {   
             
             try
             {
                 
                 String msg = "@#@#@#@";
                 PrintWriter out = new PrintWriter(s.getOutputStream(),true);
                 BufferedReader in = new BufferedReader( new InputStreamReader(s.getInputStream()));
                 Long currentTime,previousTime;
                 currentTime=System.currentTimeMillis();
                 previousTime=System.currentTimeMillis();
                 String thePattern = "(?i)(<CMD.*?>)(.+?)(</CMD>)";
                 Pattern pattern;
                 Matcher matcher;

                 while (running && msg != null) {
                     Thread.sleep(20);
                     

                     
                     if (in.ready()) {
                         msg = in.readLine();
                     }//end if
                     
                     pattern = Pattern.compile(thePattern);
                     matcher = pattern.matcher(msg);                     

                     if (matcher.find()) {
                         
                         processCommand(msg, thePattern,out);
                         matcher.reset();
                         msg = "@#@#@#@";
                     }

                     if(msg.equals("ENQ"))
                     {
                         out.println("ACK");
                         currentTime = System.currentTimeMillis();
                         previousTime=currentTime;
                         msg="@#@#@#@";


                     }

                     if(currentTime - previousTime >= 5000)
                     {
                         System.out.println("connection timed out");
                         msg=null;

                     }//end if
  
                     if (msg != null && !msg.equals("@#@#@#@") && !msg.equals("ENQ")) {
                         //System.out.print(msg + "\n");
                         msg = "@#@#@#@";
                     }
                                          


       
                     
                     currentTime=System.currentTimeMillis();

                 }//end while
            
             }//end try
             catch(Exception e)
             {
                 e.printStackTrace();
             }//end catch
          }//end if
    }//end run   
    
    boolean isConnected()
    {
        boolean connected = false;
        if( s != null)
        {
            connected = s.isConnected();
        
        }//end if
        
        return connected;
    }//end isconnected
    
    void stopThread()
    {
        running=false;
    
    }//end stop Thread
    
    public void processCommand(String command,String pattern,PrintWriter out) {

        String CMD = command.replaceAll(pattern, "$2");
        
       
        
        if(CMD.equals("getQueuePath"))
        {
            out.println(prefs.get("queuePath", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getQueueRefresh"))
        {
            out.println(prefs.getInt("queueRefresh", 9898));
            CMD="";
        
        }
        if(CMD.equals("getServerName"))
        {
            out.println(prefs.get("serverName", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getUploadPath"))
        {
            out.println(prefs.get("uploadPath", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getHost"))
        {
            out.println(prefs.get("host", "@@@"));
            CMD="";
        
        }         
        if(CMD.equals("getLogFilePath"))
        {
            out.println(prefs.get("logFilePath", "@@@"));
            CMD="";
        
        } 
        if(CMD.equals("getPassword"))
        {
            out.println(prefs.get("password", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getPhoneBookEntryStatus"))
        {
            out.println(prefs.get("phoneBookEntryCheckBox", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getPhoneBookEntry"))
        {
            out.println(prefs.get("phoneBookentryTextField", "@@@"));
            CMD="";
        
        }  
        
        if(CMD.equals("getPort"))
        {
            out.println(prefs.get("port", "@@@"));
            CMD="";
        
        } 
        if(CMD.equals("getTransmitStatus"))
        {
            out.println(prefs.get("transmitCheckbox", "@@@"));
            CMD="";
        
        } 
        if(CMD.equals("getUploadPath"))
        {
            out.println(prefs.get("uploadPath", "@@@"));
            CMD="";
        
        }
        if(CMD.equals("getUserName"))
        {
            out.println(prefs.get("userName", "@@@"));
            CMD="";
        
        }   
        //*********************SET METHODSS******************************
        
        if(CMD.equals("setShutDown="))
        {
            out.println(prefs.get("close", "@@@"));
            CMD="";
        
        }         
         if(CMD.contains("setQueuePath="))
        {
           out.println(prefs.get("queuePath", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setQueueRefresh="))
        {
            out.println("<CMDREPLY>"+prefs.getInt("queueRefresh", 9898)+"</CMDREPLY>");
            //System.out.println("<CMDREPLY>"+prefs.getInt("queueRefresh", 9898)+"</CMDREPLY>");
            
            CMD="";
        
        }
        if(CMD.contains("setServerName="))
        {
            out.println(prefs.get("serverName", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setUploadPath="))
        {
            out.println(prefs.get("uploadPath", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setHost="))
        {
            out.println(prefs.get("host", "@@@"));
            CMD="";
        
        }         
        if(CMD.contains("setLogFilePath="))
        {
            out.println(prefs.get("logFilePath", "@@@"));
            CMD="";
        
        } 
        if(CMD.contains("setPassword="))
        {
            out.println(prefs.get("password", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setPhoneBookEntryStatus="))
        {
            out.println(prefs.get("phoneBookEntryCheckBox", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setPhoneBookEntry="))
        {
            out.println(prefs.get("phoneBookentryTextField", "@@@"));
            CMD="";
        
        }  
        
        if(CMD.contains("setPort="))
        {
            out.println(prefs.get("port", "@@@"));
            CMD="";
        
        } 
        if(CMD.contains("setTransmitStatus="))
        {
            out.println(prefs.get("transmitCheckbox", "@@@"));
            CMD="";
        
        } 
        if(CMD.contains("setUploadPath="))
        {
            out.println(prefs.get("uploadPath", "@@@"));
            CMD="";
        
        }
        if(CMD.contains("setUserName="))
        {
            out.println(prefs.get("userName", "@@@"));
            CMD="";
        
        }       
    }// end prcoess CommandsansCMD
    
}
