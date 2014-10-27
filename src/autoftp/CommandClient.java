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
                         
                         processCommand(msg, thePattern);
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
                         System.out.print(msg + "\n");
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
    
    public void processCommand(String command,String pattern) {

        String sansCMD = command.replaceAll(pattern, "$2");
        if(sansCMD.equals("getQueue"))
        {
            System.out.println(prefs.get("queuePath", ""));
        
        }
        if(sansCMD.equals("getRefresh"))
        {
            System.out.println(prefs.getInt("queueRefresh", 9898));
        
        }
        if(sansCMD.equals("serverName"))
        {
            System.out.println(prefs.get("serverName", "@@@"));
        
        }
        if(sansCMD.equals("uploadPath"))
        {
            System.out.println(prefs.get("uploadPath", "@@@"));
        
        }        


    }// end prcoess CommandsansCMD
    
}
