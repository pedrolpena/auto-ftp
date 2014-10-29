/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;

/**
 *
 * @author pedro
 */
public interface MessagingServerInterface extends Runnable{
 void sendToAllClients(String msg);
 void sendToClient(String name,String msg);
    
}
