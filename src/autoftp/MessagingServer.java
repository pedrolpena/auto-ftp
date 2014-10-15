package autoftp;
import java.net.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Date;

public class MessagingServer implements Runnable
{
    
    int port=25000;
    String host="127.0.0.1";
    ServerSocket serverSocket;
    Socket s;
    PrintStream out;
    ArrayList socketList;
    ListIterator li;
    connectionHandler t;
    Thread csT;
  
    public MessagingServer(String adr , int prt)
    {
        host = adr;
        port = prt;

       
    
    }//end constructor
    
        public MessagingServer()
    {
        host = "127.0.0.1";
        port = 25000;      
    
    }//end constructor
    public void run() 
    {
        try
        {
            socketList = new ArrayList();
            serverSocket = new ServerSocket(port);
            while(true)
            {
                Thread.sleep(20);
                //System.out.println("Waiting for connection");
                s=serverSocket.accept();               
                t = new connectionHandler(s);
                t.start();
                socketList.add(t);
                //sendToAllClients("A new connection was made, there are now "+socketList.size()+" live connections");
                //System.out.println("A new connection was made, there are now "+socketList.size()+" live connections");
            }//end while
        }//end try
        catch(Exception e)
    
        {
        }//end catch
    }
    
    public void sendToAllClients(String msg) {
 
        
        if (socketList != null) {
            li = socketList.listIterator();
            connectionHandler ch;
            while (li.hasNext()) {

                ch = ((connectionHandler) li.next());
                if (ch != null && ch.isAlive()) {
                    ch.sendMessage(msg);

                }//end if
                else {
                    li.remove();
                    //sendToAllClients("A connection was lost, there are now "+socketList.size()+" live connections");
                    //System.out.println("A connection was lost, there are now "+socketList.size()+" live connections");
                }//end else

            }//end while

        }//end if  
    }//end method

public class connectionHandler extends Thread
{

    Long currentTime,previousTime,previousTime2;
    Socket socket;
    BufferedOutputStream bos;
    PrintWriter pw ;


    public connectionHandler( Socket s)
        {
            socket =s;
            try
            {
                bos = new BufferedOutputStream(socket.getOutputStream());
            }
            catch(Exception e)
            {
            }//end catch
            pw = new PrintWriter(bos,true);
            
        }//end constructer

    
    public void run() 
    
    {
    
        currentTime=System.currentTimeMillis();        
        previousTime=System.currentTimeMillis();
        previousTime2=System.currentTimeMillis();
        try
        {
            String msg="";
            BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));


            while(socket !=null && !socket.isClosed())
                
            { 
                Thread.sleep(20);
                if(in.ready() && !msg.trim().equals("ACK"))
                {
                    //msg=in.readLine();
                    msg+=(char)in.read();
                    
                     
                    
                }//end if
                
                if(currentTime - previousTime >= 5000)       
                {               
                    
                    //System.out.println(socket.toString() + " timed out");
                    this.sendMessage("closing this connection.");
                    in=null;
                    
                    socket.shutdownOutput();
                    socket.shutdownInput();                      
                    socket.close();
                    socket=null;
                }//end if 
                
                if(socket != null && !socket.isClosed() && (currentTime - previousTime2 >= 1000))       
                { 
                    this.sendMessage("ENQ");
                    previousTime2=currentTime;

                                          
                }//end if                 

                
                
                if(socket != null && msg.trim().equals("ACK"))
                {
                    previousTime = currentTime;
                    //System.out.println("ACK received from "+ socket.toString());
                    msg="";
                }//end if
            currentTime=System.currentTimeMillis();

            }//end while
        }//end try
        catch(Exception e)
        {
            e.printStackTrace();
        
        }//end catch
                              {
        host = "127.0.0.1";
        port = 25000;
    
    }//end constructor
            
            
        
    }
    public void sendMessage(String msg)
    {
        try{

            if(socket!=null && !socket.isClosed())
            {     
                pw.println(msg);
                //pw.flush(); 
            
            }//end if
       
        }//end try
        catch(Exception e)
        {
            e.printStackTrace();
        }//end cacth
    }//end SendMessage
    
}//end class




void setPort(int p)
{
    port = p;
}//end set port

void setHost(String h)
{
    host = h;

}//end setHost

}//end class


