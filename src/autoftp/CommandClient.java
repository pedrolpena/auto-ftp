/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;
import java.io.*;
import java.net.*;
/**
 *
 * @author pedro
 */
public class CommandClient extends Thread{
    static String address;
    static int port;
    Socket s;
    boolean running = true;


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

                 while( running && msg != null )
                 {
                     Thread.sleep(20);

                     if(in.ready())
                     {  
                        msg=in.readLine(); 
                     }//end if

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
                         
                     if(msg!=null && !msg.equals("@#@#@#@") && !msg.equals("ENQ"))
                     {
                         System.out.print(msg + "\n");
                         msg="@#@#@#@";
                     }

                     
                     if(msg.contains("<CMD>") &&msg.contains("</CMD>") )
                     {
                         out.println("ACK");
                         currentTime = System.currentTimeMillis();
                         previousTime=currentTime;
                         msg="@#@#@#@";

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
    
}
