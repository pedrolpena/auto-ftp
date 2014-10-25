/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package autoftp;
import java.io.*;

/**
 *
 * @author Pedro.Pena
 */
public class RasDialer {





    private String[] sendCommand(String command) {
        String line = "";
        BufferedReader input;
         Long currentTime,timeOut;
         currentTime=(long)0;
         
         
        try {
            Process p = Runtime.getRuntime().exec(command);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            timeOut = System.currentTimeMillis()+ 120*1000 ;
            
            while (!line.contains("Command completed successfully") && 
                    !line.contains("Remote Access error") && 
                    !line.contains("No connections") && 
                    !line.contains("Connected to") && 
                    !line.contains("error") &&
                    (currentTime <= timeOut)) {
                currentTime=System.currentTimeMillis();
                Thread.sleep(20);
                if (input.ready()) {
                    line += input.readLine() + "::";
                }

            }// end while
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] lines = line.split("::");
        return lines;
    }

// checks to see if the connection is stil active
public  boolean isAlive(){
    String connectionStatus = sendCommand("cmd /c rasdial")[0];
    if(connectionStatus.contains("No connections"))
        return false;
    if(connectionStatus.contains("Connected to"))
        return true;

    return false;


}
// mehod to close the Iridiium connection
public  boolean closeConnection(String entry ){
    sendCommand("cmd /c rasdial "+entry+" /d");
    return !isAlive();

}// end close connection

// mehod to close the Iridiium connection
public  boolean closeAllConnections(){
    sendCommand("cmd /c rasdial /disconnect");
    return !isAlive();

}// end close connection

public  boolean openConnection(String entry) throws RasDialerException{
    String connectionResponse[] = sendCommand("cmd /c rasdial "+entry);
    String response = "";

    for(int i = 0; i < connectionResponse.length ; i++)
        response += connectionResponse[i];


    // this is a list of error codes and the accompanying messge for rasdial

if(response.contains("600")) throw new RasDialerException("An operation is pending.");
if(response.contains("601")) throw new RasDialerException("The port handle is invalid.");
if(response.contains("602")) throw new RasDialerException("The port is already open.");
if(response.contains("603")) throw new RasDialerException("Caller's buffer is too small.");
if(response.contains("604")) throw new RasDialerException("Wrong information specified.");
if(response.contains("605")) throw new RasDialerException("Cannot set port information.");
if(response.contains("606")) throw new RasDialerException("The port is not connected.");
if(response.contains("607")) throw new RasDialerException("The event is invalid.");
if(response.contains("608")) throw new RasDialerException("The device does not exist.");
if(response.contains("609")) throw new RasDialerException("The device type does not exist.");
if(response.contains("610")) throw new RasDialerException("The buffer is invalid.");
if(response.contains("611")) throw new RasDialerException("The route is not available.");
if(response.contains("612")) throw new RasDialerException("The route is not allocated.");
if(response.contains("613")) throw new RasDialerException("Invalid compression specified.");
if(response.contains("614")) throw new RasDialerException("Out of buffers.");
if(response.contains("615")) throw new RasDialerException("The port was not found.");
if(response.contains("616")) throw new RasDialerException("An asynchronous request is pending.");
if(response.contains("617")) throw new RasDialerException("The port or device is already disconnecting.");
if(response.contains("618")) throw new RasDialerException("The port is not open.");
if(response.contains("619")) throw new RasDialerException("The port is disconnected.");
if(response.contains("620")) throw new RasDialerException("There are no endpoints.");
if(response.contains("621")) throw new RasDialerException("Cannot open the phone book file.");
if(response.contains("622")) throw new RasDialerException("Cannot load the phhe port is already in use or is not configured for Remote Access dialoutone book file.");
if(response.contains("623")) throw new RasDialerException("Cannot find the phone book entry.");
if(response.contains("624")) throw new RasDialerException("Cannot write the phone book file.");
if(response.contains("625")) throw new RasDialerException("Invalid information found in the phone book.");
if(response.contains("626")) throw new RasDialerException("Cannot load a string.");
if(response.contains("627")) throw new RasDialerException("Cannot find key.");
if(response.contains("628")) throw new RasDialerException("The port was disconnected.");
if(response.contains("629")) throw new RasDialerException("The port was disconnected by the remote machine.");
if(response.contains("630")) throw new RasDialerException("The port was disconnected due to hardware failure.");
if(response.contains("631")) throw new RasDialerException("The port was disconnected by the user.");
if(response.contains("632")) throw new RasDialerException("The structure size is incorrect.");
if(response.contains("633")) throw new RasDialerException("The port is already in use or is not configured for Remote Access dialout.");
if(response.contains("634")) throw new RasDialerException("Cannot register your computer on the remote network.");
if(response.contains("635")) throw new RasDialerException("Unknown error.");
if(response.contains("636")) throw new RasDialerException("The wrong device is attached to the port.");
if(response.contains("637")) throw new RasDialerException("The string could not be converted.");
if(response.contains("638")) throw new RasDialerException("The request has timed out.");
if(response.contains("639")) throw new RasDialerException("No asynchronous net available.");
if(response.contains("640")) throw new RasDialerException("A NetBIOS error has occurred.");
if(response.contains("641")) throw new RasDialerException("The server cannot allocate NetBIOS resources needed to support the client.");
if(response.contains("642")) throw new RasDialerException("One of your NetBIOS names is already registered on the remote network.");
if(response.contains("643")) throw new RasDialerException("A network adapter at the server failed.");
if(response.contains("644")) throw new RasDialerException("You will not receive network message popups.");
if(response.contains("645")) throw new RasDialerException("Internal authentication error.");
if(response.contains("646")) throw new RasDialerException("The account is not permitted to log on at this time of day.");
if(response.contains("647")) throw new RasDialerException("The account is disabled.");
if(response.contains("648")) throw new RasDialerException("The password has expired.");
if(response.contains("649")) throw new RasDialerException("The account does nhe port is already in use or is not configured for Remote Access dialoutot have Remote Access permission.");
if(response.contains("650")) throw new RasDialerException("The Remote Access server is not responding.");
if(response.contains("651")) throw new RasDialerException("Your modem (or other connecting device) has reported an error.");
if(response.contains("652")) throw new RasDialerException("Unrecognized response from the device.");
if(response.contains("653")) throw new RasDialerException("A macro required by the device was not found in the device .INF file section.");
if(response.contains("654")) throw new RasDialerException("A command or response in the device .INF file section refers to an undefined macro.");
if(response.contains("655")) throw new RasDialerException("The <message> macro was not found in the device .INF file section.");
if(response.contains("656")) throw new RasDialerException("The <defaultoff> macro in the device .INF file section contains an undefined macro.");
if(response.contains("657")) throw new RasDialerException("The device .INF file could not be opened.");
if(response.contains("658")) throw new RasDialerException("The device name in the device .INF or media .INI file is too long.");
if(response.contains("659")) throw new RasDialerException("The media .INI file refers to an unknown device name.");
if(response.contains("660")) throw new RasDialerException("The device .INF file contains no responses for the command.");
if(response.contains("661")) throw new RasDialerException("The device .INF file is missing a command.");
if(response.contains("662")) throw new RasDialerException("Attempted to set a macro not listed in device .INF file section.");
if(response.contains("663")) throw new RasDialerException("The media .INI file refers to an unknown device type.");
if(response.contains("664")) throw new RasDialerException("Cannot allocate memory.");
if(response.contains("665")) throw new RasDialerException("The port is not configured for Remote Access.");
if(response.contains("666")) throw new RasDialerException("Your modem (or other connecting device) is not functioning.");
if(response.contains("667")) throw new RasDialerException("Cannot read the media .INI file.");
if(response.contains("668")) throw new RasDialerException("The connection dropped.");
if(response.contains("669")) throw new RasDialerException("The usage parameter in the media .INI file is invalid.");
if(response.contains("670")) throw new RasDialerException("Cannot read the section name from the media .INI file.");
if(response.contains("671")) throw new RasDialerException("Cannot read the device type from the media .INI file.");
if(response.contains("672")) throw new RasDialerException("Cannot read the device name from the media .INI file.");
if(response.contains("673")) throw new RasDialerException("Cannot read the usage from the media .INI file.");
if(response.contains("674")) throw new RasDialerException("Cannot read the maximum connection BPS rate from the media .INI file.");
if(response.contains("675")) throw new RasDialerException("Cannot read the maximum carrier BPS rate from the media .INI file.");
if(response.contains("676")) throw new RasDialerException("The line is busy.");
if(response.contains("677")) throw new RasDialerException("A person answered instead of a modem.");
if(response.contains("678")) throw new RasDialerException("There is no answer.");
if(response.contains("679")) throw new RasDialerException("Cannot detect carrhe port is already in use or is not configured for Remote Access dialoutier.");
if(response.contains("680")) throw new RasDialerException("There is no dial tone.");
if(response.contains("681")) throw new RasDialerException("General error reported by device.");
if(response.contains("682")) throw new RasDialerException("ERROR WRITING SECTIONNAME");
if(response.contains("683")) throw new RasDialerException("ERROR WRITING DEVICETYPE");
if(response.contains("684")) throw new RasDialerException("ERROR WRITING DEVICENAME");
if(response.contains("685")) throw new RasDialerException("ERROR WRITING MAXCONNECTBPS");
if(response.contains("686")) throw new RasDialerException("ERROR WRITING MAXCARRIERBPS");
if(response.contains("687")) throw new RasDialerException("ERROR WRITING USAGE");
if(response.contains("688")) throw new RasDialerException("ERROR WRITING DEFAULTOFF");
if(response.contains("689")) throw new RasDialerException("ERROR READING DEFAULTOFF");
if(response.contains("690")) throw new RasDialerException("ERROR EMPTY INI FILE");
if(response.contains("691")) throw new RasDialerException("Access denied because username and/or password is invalid on the domain.");
if(response.contains("692")) throw new RasDialerException("Hardware failure in port or attached device.");
if(response.contains("693")) throw new RasDialerException("ERROR NOT BINARY MACRO");
if(response.contains("694")) throw new RasDialerException("ERROR DCB NOT FOUND");
if(response.contains("695")) throw new RasDialerException("ERROR STATE MACHINES NOT STARTED");
if(response.contains("696")) throw new RasDialerException("ERROR STATE MACHINES ALREADY STARTED");
if(response.contains("697")) throw new RasDialerException("ERROR PARTIAL RESPONSE LOOPING");
if(response.contains("698")) throw new RasDialerException("A response keyname in the device .INF file is not in the expected format.");
if(response.contains("699")) throw new RasDialerException("The device response caused buffer overflow.");
if(response.contains("700")) throw new RasDialerException("The expanded command in the device .INF file is too long.");
if(response.contains("701")) throw new RasDialerException("The device moved to a BPS rate not supported by the COM driver.");
if(response.contains("702")) throw new RasDialerException("Device response received when none expected.");
if(response.contains("703")) throw new RasDialerException("ERROR INTERACTIVE MODE");
if(response.contains("704")) throw new RasDialerException("ERROR BAD CALLBACK NUMBER");
if(response.contains("705")) throw new RasDialerException("ERROR INVALID AUTH STATE");
if(response.contains("706")) throw new RasDialerException("ERROR WRITING INITBPS");
if(response.contains("707")) throw new RasDialerException("X.25 diagnostic indication.");
if(response.contains("708")) throw new RasDialerException("The account has expired.");
if(response.contains("709")) throw new RasDialerException("Error changing password on domain.");
if(response.contains("710")) throw new RasDialerException("Serial overrun errors were detected while communicating with your modem.");
if(response.contains("711")) throw new RasDialerException("RasMan initialization failure. Check the event log.");
if(response.contains("712")) throw new RasDialerException("Biplex port is initializing. Wait a few seconds and redial.");
if(response.contains("713")) throw new RasDialerException("No active ISDN lines are available.");
if(response.contains("714")) throw new RasDialerException("Not enough ISDN channels are available to make the call.");
if(response.contains("715")) throw new RasDialerException("Too many errors occurred because of poor phone line quality.");
if(response.contains("716")) throw new RasDialerException("The Remote Access IP configuration is unusable.");
if(response.contains("717")) throw new RasDialerException("No IP addresses are available in the static pool of Remote Access IP addresses.");
if(response.contains("718")) throw new RasDialerException("PPP timeout.");
if(response.contains("719")) throw new RasDialerException("PPP terminated by remote machine.");
if(response.contains("720")) throw new RasDialerException("No PPP control protocols configured.");
if(response.contains("721")) throw new RasDialerException("Remote PPP peer is not responding.");
if(response.contains("722")) throw new RasDialerException("The PPP packet is invalid.");
if(response.contains("723")) throw new RasDialerException("The phone number, including prefix and suffix, is too long.");
if(response.contains("724")) throw new RasDialerException("The IPX protocol cannot dial-out on the port because the computer is an IPX router.");
if(response.contains("725")) throw new RasDialerException("The IPX protocol cannot dial-in on the port because the IPX router is not installed.");
if(response.contains("726")) throw new RasDialerException("The IPX protocol cannot be used for dial-out on more than one port at a time.");
if(response.contains("727")) throw new RasDialerException("Cannot access TCPCFG.DLL.");
if(response.contains("728")) throw new RasDialerException("Cannot find an IP adapter bound to Remote Access.");
if(response.contains("729")) throw new RasDialerException("SLIP cannot be used unless the IP protocol is installed.");
if(response.contains("730")) throw new RasDialerException("Computer registration is not complete.");
if(response.contains("731")) throw new RasDialerException("The protocol is not configured.");
if(response.contains("732")) throw new RasDialerException("The PPP negotiation is not converging.");
if(response.contains("733")) throw new RasDialerException("The PPP control protocol for this network protocol is not available on the server.");
if(response.contains("734")) throw new RasDialerException("The PPP link control protocol terminated.");
if(response.contains("735")) throw new RasDialerException("The requested address was rejected by the server.");
if(response.contains("736")) throw new RasDialerException("The remote computer terminated the control protocol.");
if(response.contains("737")) throw new RasDialerException("Loopback detected.");
if(response.contains("738")) throw new RasDialerException("The server did not assign an address.");
if(response.contains("739")) throw new RasDialerException("The remote server cannot use the Windows NT encrypted password.");
if(response.contains("740")) throw new RasDialerException("The TAPI devices configured for Remote Access failed to initialize or were not installed correctly.");
if(response.contains("741")) throw new RasDialerException("The local computer does not support encryption.");
if(response.contains("742")) throw new RasDialerException("The remote server does not support encryption.");
if(response.contains("743")) throw new RasDialerException("The remote server requires encryption.");
if(response.contains("744")) throw new RasDialerException("Cannot use the IPX net number assigned by the remote server. Check the event log.");
if(response.contains("752")) throw new RasDialerException("A syntax error was encountered while processing a script.");



    return isAlive();


}// end open Connection


}





