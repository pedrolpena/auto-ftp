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
                         //System.out.println("connection timed out");
                         previousTime=currentTime;
                         msg="@#@#@#@";

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
            out.println("<CMDREPLY>"+prefs.get("queuePath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getQueueRefresh"))
        {
            out.println("<CMDREPLY>"+prefs.getInt("queueRefresh", 9898)+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getServerName"))
        {
            out.println("<CMDREPLY>"+prefs.get("serverName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getUploadPath"))
        {
            out.println("<CMDREPLY>"+prefs.get("uploadPath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getHost"))
        {
            out.println("<CMDREPLY>"+prefs.get("host", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }         
        if(CMD.equals("getLogFilePath"))
        {
            out.println("<CMDREPLY>"+prefs.get("logFilePath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.equals("getPassword"))
        {
            out.println("<CMDREPLY>"+prefs.get("password", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getPhoneBookEntryStatus"))
        {
            out.println("<CMDREPLY>"+prefs.get("phoneBookEntryCheckBox", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getPhoneBookEntry"))
        {
            out.println("<CMDREPLY>"+prefs.get("phoneBookentryTextField", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }  
        
        if(CMD.equals("getPort"))
        {
            out.println("<CMDREPLY>"+prefs.get("port", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.equals("getTransmitStatus"))
        {
            out.println("<CMDREPLY>"+prefs.get("transmitCheckbox", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.equals("getUploadPath"))
        {
            out.println("<CMDREPLY>"+prefs.get("uploadPath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.equals("getUserName"))
        {
            out.println("<CMDREPLY>"+prefs.get("userName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }   
        //*********************SET METHODSS******************************
        
        if(CMD.contains("setShutDown="))
        {
            prefs.put("close",CMD.replaceAll("setShutDown=",""));
            flushPrefs();  
            out.println("<CMDREPLY>"+prefs.get("close", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }         
         if(CMD.contains("setQueuePath="))
        {
            
            prefs.put("queuePath",CMD.replaceAll("setQueuePath=",""));
            flushPrefs();
            out.println("<CMDREPLY>"+prefs.get("queuePath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setQueueRefresh="))
        {
            prefs.put("queueRefresh",CMD.replaceAll("setQueueRefresh=",""));
            flushPrefs();             
            out.println("<CMDREPLY>"+prefs.getInt("queueRefresh", 9898)+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setServerName="))
        {
            prefs.put("serverName",CMD.replaceAll("setServerName=",""));
            flushPrefs();            
            out.println("<CMDREPLY>"+prefs.get("serverName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setUploadPath="))
        {
            prefs.put("uploadPath",CMD.replaceAll("setUploadPath=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("uploadPath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setHost="))
        {
            prefs.put("host",CMD.replaceAll("setHost=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("host", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }         
        if(CMD.contains("setLogFilePath="))
        {
            prefs.put("logFilePath",CMD.replaceAll("setLogFilePath=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("logFilePath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.contains("setPassword="))
        {
            prefs.put("password",CMD.replaceAll("setPassword=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("password", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setPhoneBookEntryStatus="))
        {
            prefs.put("transmitCheckbox",CMD.replaceAll("setPhoneBookEntryStatus=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("phoneBookEntryCheckBox", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setPhoneBookEntry="))
        {
            prefs.put("phoneBookentryTextField",CMD.replaceAll("setPhoneBookEntry=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("phoneBookentryTextField", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }  
        
        if(CMD.contains("setPort="))
        {
            prefs.put("port",CMD.replaceAll("setPort=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("port", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.contains("setTransmitStatus="))
        {
            prefs.put("transmitCheckbox",CMD.replaceAll("setTransmitStatus=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("transmitCheckbox", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        if(CMD.contains("setUploadPath="))
        {
            prefs.put("uploadPath",CMD.replaceAll("setUploadPath=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("uploadPath", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if(CMD.contains("setUserName="))
        {
            prefs.put("userName",CMD.replaceAll("setUserName=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("userName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }   
        
       
    }// end prcoess CommandsansCMD
    
    void flushPrefs()
    {
        try
        {
            prefs.flush();
        }
        catch(Exception e)
        {
        e.printStackTrace();
        }//end catch
    
    }//end flushPrefs
    
}
