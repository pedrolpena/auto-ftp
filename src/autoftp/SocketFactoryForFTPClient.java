/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.net.SocketFactory;

/**This class was implemented so that the application can have more control over the actual sockets that 
 * connect to the ftp server. the FTPClient.setSoTimeout() had no effect on the actual timeout of the socket.
 * It seemed to be completely dependent on the OS. 
 * With this class, the application can directly close or set to null the sockets after detecting a dropped callqwertyui.
 * This way the application doesnt hang while the iostreams read/write methods are blocking.
 * 
 *
 * @author pedro
 */
public class SocketFactoryForFTPClient extends SocketFactory {
    ArrayList socketList;
    
    public SocketFactoryForFTPClient(){
    
        socketList = new ArrayList();
    
    }

    public Socket createSocket() throws SocketException{        
        Socket s = new Socket();
        socketList.add(s);
        return s;
    }
    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException,SocketException {
        Socket s; 
        s = new Socket(string,i);
        socketList.add(s);       
        return s;
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException,SocketException {
        Socket s;
        s = new Socket(string,i,ia,i1);
        socketList.add(s);      
        return s;
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException ,SocketException{
        Socket s;
        s = new Socket(ia,i);
        socketList.add(s);      
        return s;
        
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException,SocketException {
        Socket s;
        s = new Socket(ia,i,ia1,i1);
        socketList.add(s);
     
        return s;
    }
    
    
  
    
    
    public void closeAllConnections() {

        ListIterator li;

        if (socketList != null) {
            li = socketList.listIterator();
            Socket s;
            while (li.hasNext()) {

                s = (Socket) li.next();
                if (s != null && s.isConnected()) {
                    try {
                        if (!s.isInputShutdown()) {
                            s.shutdownInput();
                        }

                        if (!s.isOutputShutdown()) {
                            s.shutdownOutput();
                        }
                        if (!s.isClosed()) {
                            s.close();
                        }
                        s = null;
                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                }//end if
                else {
                    li.remove();
                }//end else

            }//end while

        }//end if  
    }//end method   
    

    
}
