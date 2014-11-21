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
    PrintWriter nullOut;

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
                 nullOut = new PrintWriter(new NullOutputStream(),true);
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
        if(CMD.equals("getUseDialer"))
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
        if(CMD.equals("getTransmit"))
        {
            out.println("<CMDREPLY>"+prefs.get("transmitCheckbox", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 

        if(CMD.equals("getUserName"))
        {
            out.println("<CMDREPLY>"+prefs.get("userName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        
        if (CMD.equals("getConfig")) {
            out.println("<CMDREPLY>"
                    + prefs.getInt("queueRefresh", 9898)
                    + "::" + prefs.get("queuePath", "@@@")
                    + "::" + prefs.get("logFilePath", "@@@")
                    + "::" + prefs.get("serverName", "@@@")
                    + "::" + prefs.get("userName", "@@@")
                    + "::" + prefs.get("password", "@@@")
                    + "::" + prefs.get("uploadPath", "@@@")
                    + "::" + prefs.get("transmitCheckbox", "@@@")
                    + "::" + prefs.get("phoneBookentryTextField", "@@@")
                    + "::" + prefs.get("phoneBookEntryCheckBox", "@@@")
                    + "::" + prefs.get("host", "@@@")
                    + "::" + prefs.get("port", "@@@")
                    + "</CMDREPLY>");
            CMD = "";

        }        
        //*********************SET METHODS******************************
        
        if (CMD.contains("setShutDown=")) {
            boolean state = false;
            String prefName = "close";
            CMD = CMD.replaceAll("setShutDown=", "");

            if (CMD.equals("true") || CMD.equals("false")) {
                state = (new Boolean(CMD).booleanValue());
                prefs.putBoolean(prefName, state);
                flushPrefs();
            }//end if
            else{
                            out.println("<CMDERROR>" + CMD + " is not a boolean. value not changed." + "</CMDERROR>");
            
            }//end else

            out.println("<CMDREPLY>" + prefs.get(prefName, "@@@") + "</CMDREPLY>");
            CMD = "";

        }        
        if (CMD.contains("setQueuePath=")) {
            String path = CMD.replaceAll("setQueuePath=", "");
            String prefName="queuePath";

            if (!setPath(prefName, path)) {
                out.println("<CMDERROR>" + path + " is not a valid path. the path was not changed." + "</CMDERROR>");
            }//end if

            out.println("<CMDREPLY>" + prefs.get(prefName, "@@@") + "</CMDREPLY>");
            CMD = "";

        }
        if(CMD.contains("setQueueRefresh="))
        {
            CMD = CMD.replaceAll("setQueueRefresh=", "");
            String prefName = "queueRefresh";
            boolean isInt = true;
            int value = 1;
            
            try {
                value = Integer.parseInt(CMD);
                
            }//end try           
            catch (Exception e) {
                isInt = false;
            }//end catch
            if (isInt && value > 0) {
                prefs.putInt(prefName, value);
                flushPrefs();
            }//end if
            else {
              out.println("<CMDERROR>" + CMD + " is not an integer or is not an integer greater than 0" + "</CMDERROR>");  
            }//end else

            out.println("<CMDREPLY>" + prefs.getInt(prefName, 9898) + "</CMDREPLY>");
            CMD = "";
        
        }
        if(CMD.contains("setServerName="))
        {
            prefs.put("serverName",CMD.replaceAll("setServerName=",""));
            flushPrefs();            
            out.println("<CMDREPLY>"+prefs.get("serverName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if (CMD.contains("setUploadPath=")) {
            CMD = CMD.replaceAll("setUploadPath=", "").trim();
            if (!CMD.equals("") && CMD.startsWith("/") && CMD.endsWith("/")) {
                prefs.put("uploadPath", CMD);
                flushPrefs();
            }//end if
            else {
                out.println("<CMDERROR>" + CMD + " is not a properly formatted upload path." + "</CMDERROR>");
            }//end else

            out.println("<CMDREPLY>" + prefs.get("uploadPath", "@@@") + "</CMDREPLY>");
            CMD = "";

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
            String path = CMD.replaceAll("setLogFilePath=", "");
            String prefName="logFilePath";

            if (!setPath(prefName, path)) {
                out.println("<CMDERROR>" + path + " is not a valid path. the path was not changed." + "</CMDERROR>");
            }//end if

            out.println("<CMDREPLY>" + prefs.get(prefName, "@@@") + "</CMDREPLY>");
            CMD = "";
        
        } 
        if(CMD.contains("setPassword="))
        {
            prefs.put("password",CMD.replaceAll("setPassword=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("password", "@@@")+"</CMDREPLY>");
            CMD="";
        
        }
        if (CMD.contains("setUseDialer=")) {

            boolean state = false;
            String prefName = "phoneBookEntryCheckBox";
            CMD = CMD.replaceAll("setUseDialer=", "");

            if (CMD.equals("true") || CMD.equals("false")) {
                state = (new Boolean(CMD).booleanValue());
                prefs.putBoolean(prefName, state);
                flushPrefs();
            }//end if
            else {
                out.println("<CMDERROR>" + CMD + " is not a boolean. value not changed." + "</CMDERROR>");

            }//end else

            out.println("<CMDREPLY>" + prefs.get(prefName, "@@@") + "</CMDREPLY>");
            CMD = "";

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
         
            
            CMD = CMD.replaceAll("setPort=", "");
            String prefName = "port";
            boolean isInt = true;
            int value = 25000;
            
            try {
                value = Integer.parseInt(CMD);
                
            }//end try           
            catch (Exception e) {
                isInt = false;
            }//end catch
            if (isInt && value > -1) {
                prefs.putInt(prefName, value);
                flushPrefs();
            }//end if
            else {
              out.println("<CMDERROR>" + CMD + " is not an integer or is not an integer greater than -1" + "</CMDERROR>");  
            }//end else

            out.println("<CMDREPLY>" + prefs.getInt(prefName, 9898) + "</CMDREPLY>");
            CMD = "";            

        
        } 
        if (CMD.contains("setTransmit=")) {
            boolean state = false;
            String prefName = "transmitCheckbox";
            CMD = CMD.replaceAll("setTransmit=", "");

            if (CMD.equals("true") || CMD.equals("false")) {
                state = (new Boolean(CMD).booleanValue());
                prefs.putBoolean(prefName, state);
                flushPrefs();
            }//end if
            else {
                out.println("<CMDERROR>" + CMD + " is not a boolean. value not changed." + "</CMDERROR>");

            }//end else

            out.println("<CMDREPLY>" + prefs.get(prefName, "@@@") + "</CMDREPLY>");
            CMD = "";

        }


        if(CMD.contains("setUserName="))
        {
            prefs.put("userName",CMD.replaceAll("setUserName=",""));
            flushPrefs();              
            out.println("<CMDREPLY>"+prefs.get("userName", "@@@")+"</CMDREPLY>");
            CMD="";
        
        } 
        
        
        
        if (CMD.contains("setConfig=")) {

            CMD = CMD.replaceAll("setConfig=", "");
            String[] values = CMD.split("::");
            if (values.length == 12) {
                processCommand("setQueueRefresh=" + values[0], pattern, nullOut);
                processCommand("setQueuePath=" + values[1], pattern, nullOut);
                processCommand("setLogFilePath=" + values[2], pattern, nullOut);
                processCommand("setServerName=" + values[3], pattern, nullOut);
                processCommand("setUserName=" + values[4], pattern, nullOut);
                processCommand("setPassword=" + values[5], pattern, nullOut);
                processCommand("setUploadPath=" + values[6], pattern, nullOut);
                processCommand("setTransmit=" + values[7], pattern, nullOut);
                processCommand("setPhoneBookEntry=" + values[8], pattern, nullOut);
                processCommand("setUseDialer=" + values[9], pattern, nullOut);
                processCommand("setHost=" + values[10], pattern, nullOut);
                processCommand("setPort=" + values[11], pattern, nullOut);

            }//end if
            else {
                out.println("<CMDERROR>setConfig was not formatted properly</CMDERROR>");

            }//end else
            processCommand("getConfig", pattern, out);
            CMD = "";

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

    boolean setPath(String prefName, String path) {
        boolean exists = false;

        try {

            File dirPath = new File(path);

            if (dirPath.exists()) {
                prefs.put(prefName, path);
                flushPrefs();
                exists = true;

            }//end if
        }//end try
        catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }//end setPath


	

/**Writes to nowhere*/
public class NullOutputStream extends OutputStream {
  @Override
  public void write(int b) throws IOException {
  }
}

    
}
