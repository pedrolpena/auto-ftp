
import java.io.*;
import java.net.*;

public class client extends Thread
{
    static String address;
    static int port;

    public static void main(String[] args)
    {
        if(args.length==2)
        {
            address = args[0];
            port = (new Integer(args[1])).intValue();
            client ct = new client();
            ct.start(); 

        }//end if
    }//end main




    public void run()
        {
            
             
             
             try
             {
                 
                 Socket s = new Socket(address,port);
                 String msg = "@#@#@#@";
                 PrintWriter out = new PrintWriter(s.getOutputStream(),true);
                 BufferedReader in = new BufferedReader( new InputStreamReader(s.getInputStream()));
                 Long currentTime,previousTime;
                 currentTime=System.currentTimeMillis();
                 previousTime=System.currentTimeMillis();

                 while( msg != null )
                 {
                     Thread.sleep(20);

                     if(in.ready())
                     {  
                        msg=in.readLine(); 
                     }//end if

                     if(msg.equals("ENQ"))
                     {
                         out.println("ACK"); 
                         //out.print("ACK");
                         //out.flush();                        
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

                     currentTime=System.currentTimeMillis();

                 }//end while
            
             }//end try
             catch(Exception e)
             {
                 e.printStackTrace();
             }//end catch

    }//end run

}//end class
