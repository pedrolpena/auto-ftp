/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AutoFTP.java
 *@author Pedro Pena
 * Created on Jan 20, 2011, 10:39:30 AM
 * May 5th, 2011 changed UleadThread from a Runnable implementation to inherit from Thread. this allows a reference to use he isAlive() method
 * to make sure the thread is dead before creating it to access rasdial.
 *
 * May 5th, 2011 changed default caret update policy for the status JTextArea to always update
 * may 11th, 2011 added append method in Uploadit and SunFtpWrapper that accepts a reference to the File object to be uploaded
 * instead passing a string that is later used to create a File object.
 * The reason is that AutoFTP later creates a File object for the same file and this can cause conflicts.
 *
 * May 5ht, 2011 append methos now acceps a JTextArea Object that it updates with file upload status
 * Aug 2nd, 2011 the program zips any file that does not contain the .zip extension before transmitting.
 * Aug 2nd, 2011 if there is a no such directory error while uploading it will upload to the /default folder
 * Aug 5th added statements to close any open streams in the catch blocks of the append and upload methods of SunFtpWrapper
 * Aug 8th saves transmitted files names in an sqlite database and checks the database before dialing. if the filename is found
 * in the database then the file is deleted and a connection is never attempted. This was added because it was noticed that
 * the program attempted to upload a file that had already been uploaded continuously.
 * Dec 8th 2011, added login and server connect attempt tracking. If there are 5 consecutive failed login or server connect attempts
 * then the program will stop attempting to connect every time it checks the queue. instead it will try and connect once every 24 hours until it
 * successfully connects. Since every successfull connection is has a cost to it one can potentially receive a huge bill without ever transmitting any data.
 * Dec 9th 2011, restart
 * Dec 13 2011, added aa conection log to track connect times.
 * May 24 2012, added initilizatons to app preferences because new isntalls were crashing at startup.
 * Jul 9 2014, repalced sun ftp libray with apache commons ftp client library 3.3
 * Jul 13 2014 added messaging socket server to transmist messages
 * Jul 14 2014 add crc check o determine of zip file is good before sending
 * Jul 17 2014 added A CommandClient which is a socket thread that attempts to connect to the messaging server. The prgram will close if a connection is made.
 * This is to make sure there is  only ever one instance of the program up and running.
 * Aug 4 2014 modified transmitted file database to include transmission date. This is added using epoch time. milliseconds since jan 1st 1970 00:00:00
 * Aug 7 2014 modified wasTransmitted method to return false if an exception occurs and to check if the resultset is empty
 * Aug 7 2014 added method to log exceptions
 * Aug 8 2014 changed when it is considered a successful connection for the purposes of the 24 hour queue timer. the failed attempts timer is reset when successfully set to binary mode.
 * Aug 8 2014 replaced \n by system dependent newline character in the log file mehtods
 * 10.14.14 started versioning with gitorious
 */
package autoftp;

import java.io.*;
import java.util.zip.*;
import java.util.prefs.*;
import java.util.Date;
import javax.swing.Timer;
import java.awt.event.*;
import javax.swing.text.DefaultCaret;
import java.sql.*;

/**
 *
 * @author Pedro.Pena
 */
public class AutoFTP implements ActionListener {

    MessagingServer messageMan;
    String NL = System.getProperty("line.separator");
    UploadIt u;
    Timer timer;
    Timer prefsTimer;
    Timer queueTimer;
    Thread messageThread;
    ActionListener prefsActionListener, queueTimerActionListener;
    RasDialer rD;
    PPPConnectThread pppConnect;
    String host = "127.0.0.1";
    int port = 25000;
    CommandClient comClient;
    boolean dirExists = true, isVisible = true;
    Preferences prefs = Preferences.userNodeForPackage(getClass());
    boolean isTransmitting = false, tempbool;
    File pWD, logDIR;
    int centerX = 0, centerY = 0;
//long connectTime=-1,loginTime=-1,uploadAttempt=-1,uploaded=-1,connectionClosed=-1;
    int unsuccessfulLoginAttempts = 0;    // this holds the number of failed login attempts
    int unsuccessfulServerConnectAttempts = 0; //  this holds the number of failed server connects 
    int queueRefreshInterval = 5;

    long dialTime, //holds time when a dialout attempt is made
            internetConnect, //holds the time the actual internet connection is made
            serverConnect, //holds the time when connection to the FTP server is made
            loginTime, //holds the time when a successful login to the ftp server is made
            uploadStartTime,//holds the time when an upload/append is started
            uploadEndTime, //holds the time when an upload/append is completed
            disconnectTime, //holds the time when the modem disconnects from the internet
            fileSize, //holds the number of bytes
            averageTransferRate;//holds the average transfer rate

    String fileName = "None", temp;
    String connectionHeader = "Time_Stamp,Server_Connect(ms),Login_Time(ms),Upload_duration(ms),File_Size(Bytes),Transfer_Rate(bps),Total_Time_Connected(ms),File_Name\n";

    /**
     * Creates new form IridiumFTP
     */
    public AutoFTP() {

        init();

    }

    public static void main(String args[]) {

        new AutoFTP();

    }

    /**
     * this method is called from the constructor and this is where
     * initialization stuff should be placed.
     */
    private void init() {
        String remoteFile = "";
    //System.out.println("version 2.0 compiled 7.23.14");

        try {

            pWD = new File(System.getProperty("user.home") + File.separatorChar + "autoftp_queue");
            logDIR = new File(System.getProperty("user.home") + File.separatorChar + "auto_ftp_logs");

            if (!pWD.exists()) {
                pWD.mkdir();
            }//end if

            if (!logDIR.exists()) {
                logDIR.mkdir();
            }//end if

            if (prefs.get("queuePath", "").equals("")) {
                prefs.put("queuePath", pWD.getAbsolutePath());
           //queueLocationTextField.setText(pWD.getAbsolutePath());

            }//end if

            int tempInt = 0;

            //********initialize prefs****************//
            temp = prefs.get("password", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("password", "13%of99is99");
            }//end if

            tempInt = prefs.getInt("queueRefresh", 9898);
            if (tempInt == 9898) {
                prefs.putInt("queueRefresh", 5);
            }//end if
            temp = prefs.get("serverName", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("serverName", "192.111.123.134");
            }//end iftransmitCheckbox
            temp = prefs.get("uploadPath", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("uploadPath", "/default/");
            }//end if
            temp = prefs.get("userName", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("userName", "soopdata1");
            }//end if

            temp = prefs.get("phoneBookentryTextField", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("phoneBookentryTextField", "Iridium");
            }//end if

            temp = prefs.get("close", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("close", "false");
            }//end if
            temp = prefs.get("isVisible", "true");
            if (temp.equals("@@@")) {
                prefs.put("isVisible", "true");
            }//end if    
            temp = prefs.get("phoneBookEntryCheckBox", "@@@");
            if (temp.equals("@@@")) {
                prefs.putBoolean("phoneBookEntryCheckBox", false);
            }//end if
            temp = prefs.get("transmitCheckbox", "@@@");
            if (temp.equals("@@@")) {
                prefs.putBoolean("transmitCheckbox", false);
            }//end if   

            temp = prefs.get("logFilePath", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("logFilePath", logDIR.getAbsolutePath());
            }//end if 

            temp = prefs.get("host", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("host", "127.0.0.1");
            }//end if
            temp = prefs.get("port", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("port", "25000");
            }//end if  

    //**************************************//
            //database
            String dbPath = prefs.get("logFilePath", "") + File.separator;

//***************check for running instance**************
            comClient = new CommandClient(host, port);
            comClient.start();

            if (comClient.isConnected()) {
                System.out.println("shutting down because an instance of this program is already running");
                System.exit(0);

            }//end is connected
            else {

                comClient.stopThread();

            }//end else

            //********************************************************
            initSQLLiteDB("jdbc:sqlite:" + dbPath + "transmissions.db");

            prefs.putBoolean("close", false);
            host = prefs.get("host", "127.0.0.1");
            port = prefs.getInt("port", 25000);
            isVisible = prefs.getBoolean("isVisible", true);

    //DefaultCaret dc = (DefaultCaret)this.statusTextArea.getCaret();
            //dc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            unsuccessfulLoginAttempts = 0;    // this holds the number of failed login attempts
            unsuccessfulServerConnectAttempts = 0; //  this holds the number of failed server connects

            compressFiles();
            updateTransmitTextArea();
            remoteFile = prefs.get("uploadPath", "/default/");
            if (remoteFile.length() > 0 && !remoteFile.endsWith("/")) {
                remoteFile += "/";
                prefs.put("uploadPath", remoteFile);
            }// end if 
            resetCounters();

            //startTimer();
            startQueueTimer();
            startPreferenceLoaderTimer();

            comClient = new CommandClient(host, port);
            comClient.start();

            if (comClient.isConnected()) {
                System.out.println("shutting down because an instance of this program is already running");
                System.exit(0);

            }//end is connected
            else {

                comClient.stopThread();

            }//end else

  //*****************************************************************
            messageMan = new MessagingServer(host, port);
            messageThread = new Thread(messageMan);
            messageThread.start();
    //comClient = new CommandClient(host,port);
            //comClient.start();
            pppConnect = new PPPConnectThread();
            rD = new RasDialer();
            compressFiles();

            updateStatusTextArea("AOML Iridium FTPer version 2.0\n");
            updateStatusTextArea("compiled 08.08.14\n");
            updateStatusTextArea("java vendor " + System.getProperty("java.vendor") + "\n");
            updateStatusTextArea("java version " + System.getProperty("java.version") + "\n");
            updateTransmitTextArea();
    //sendFiles();

        }// end try
        catch (Exception e) {
            String error = e.toString();
            logExceptions(e);
            if (!error.contains("SocketTimeoutException")) {
                e.printStackTrace();
            }//end if
            else {
                System.out.println("listening for socket");

            }//end else

            pWD = new File(System.getProperty("user.home") + File.separatorChar + "iridium_ftp_queue");
            if (!pWD.exists()) {
                pWD.mkdir();
            }//end if

        }// end catch 
    }// end init

    void initSQLLiteDB(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn
                    = DriverManager.getConnection(dbName);
            Statement stat = conn.createStatement();
            stat.executeUpdate("create table IF NOT EXISTS transmitted (date,transmittedFile);");

            stat.close();
            conn.close();
        } catch (Exception e) {
            logExceptions(e);
            e.printStackTrace();

        }//
    }// end initSQLLiteDB

    /**
     * this method lists the files in the queue. before returning the list of
     * files, it calls a compression method that compresses and archives files
     * in a zip file. The method also checks to see if files currently in the
     * queue have already been transmitted by looking at a sqlite database file
     * that keeps a list of transmitted file names. If a file name matches a
     * filename in the database, it will be ignored.
     *
     * @param filePath
     * @return string containing all the file names
     */
    private String fileLister(String filePath) {
        compressFiles();
        if (filePath == null) {
            return "";
        }//end if
        File folder = new File(filePath);
        File[] listOfFiles = folder.listFiles();
        String fileList = "";
        String fileName = "";

        if (listOfFiles == null) {
            return "";

        }//end if

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (!wasTransmitted(fileName)) {
                    fileList += fileName + "\n";
                }// end if
                else {
                    if (listOfFiles.length > 0) {
                        this.updateStatusTextArea(fileName + " will not be transmitted because it was previously sent.\n");
                        listOfFiles[i].delete();
                    }//end if

                }// end else
            }// end if

        }// end for
        return fileList;
    }//end fileLister

    private void resetCounters() {

        dialTime = 0;
        internetConnect = 0;
        serverConnect = 0;
        loginTime = 0;
        uploadStartTime = 0;
        uploadEndTime = 0;
        disconnectTime = 0;
        fileName = "None";
        fileSize = 0;
        averageTransferRate = -1;

    }// end resetCounters()

    /**
     * this method returns an array of files currently in the queue before
     * returning the list of files, it calls a compression method that
     * compresses and archives files in a zip file. The method also checks to
     * see if files currently in the queue have already been transmitted by
     * looking at a sqlite database file that keeps a list of transmitted file
     * names. If a file name matches a filename in the database, it will be
     * ignored.
     *
     */
    private File[] filesInQueue(String filePath) {
        compressFiles();
        int j = 0;
        File folder = new File(filePath);
        File[] listOfFiles = folder.listFiles();
        File[] ff = new File[listOfFiles.length];

        String fileName = "";
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();

                if (!wasTransmitted(fileName)) {
                    ff[j++] = listOfFiles[i];

                }// end if
                else {
                    this.updateStatusTextArea("This file will not be transmitted because it was previously sent.\n");
                    listOfFiles[i].delete();

                }// end else
            }// end if

        }// end for
        File[] f2 = new File[j];
        for (int i = 0; i < j; i++) {
            f2[i] = ff[i];
        }// end for
        return f2;
    }//end fileLister

    /**
     * This method checks the sqlite database to see if the file has already
     * been transmitted
     *
     * @param fileName
     * @return
     */
    boolean wasTransmitted(String fileName) {

        String dbPath = prefs.get("logFilePath", "") + File.separator;
        boolean wasTransmitted = false;
        Connection conn;
        ResultSet rs;
        Statement stat;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath + "transmissions.db");
            stat = conn.createStatement();
            rs = stat.executeQuery("SELECT * FROM transmitted WHERE transmittedFile='" + fileName + "';");

            if (rs.isBeforeFirst()) {
                wasTransmitted = rs.next();
            }// end if
            rs.close();
            conn.close();

        } catch (Exception e) {
            logExceptions(e);
            if (e.getMessage().contains("no such table")) {
                initSQLLiteDB("jdbc:sqlite:" + dbPath + "transmissions.db");
            }//end if
            e.printStackTrace();
            return false;
        }

        return wasTransmitted;
    }// end wasTransmitted

    /**
     * this method adds a filename to an sqlite database
     *
     * @param fileName
     */
    public void addFile2DB(String fileName) {

        String epochTime = "";
        epochTime = System.currentTimeMillis() + "";
        String dbPath = prefs.get("logFilePath", "") + File.separator;
        Connection conn;
        PreparedStatement ps;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath + "transmissions.db");
            //ps = conn.prepareStatement("INSERT INTO transmitted VALUES ('"+fileName+"');");
            ps = conn.prepareStatement("INSERT INTO transmitted ('transmittedFile','date') VALUES ('" + fileName + "','" + epochTime + "');");
            ps.execute();
            ps.close();
            conn.close();

        } catch (Exception e) {
            logExceptions(e);
            if (e.getMessage().contains("no such table")) {
                initSQLLiteDB("jdbc:sqlite:" + dbPath + "transmissions.db");
            }//end if
            e.printStackTrace();

        }

    }// end addFile2DB

    /**
     * this method starts the timer thread that is used to check to see if there
     * are files ready to transmit
     */
    public void startTimer() {
        timer = new Timer((new Integer(prefs.get("queueRefresh", "5")).intValue()) * 60000, this);
        timer.setInitialDelay(2000);
        timer.start();

    }// end startTimer

    public void startQueueTimer() {

        queueTimerActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
           //System.out.println("QueueTimer");

                updateTransmitTextArea();
                String fileList = fileLister(prefs.get("queuePath", ""));

                if (!fileList.equals("") && (prefs.getBoolean("transmitCheckbox", false) && !isTransmitting()) && !pppConnect.isAlive()) {
                    pppConnect = new PPPConnectThread();
                    pppConnect.start();
                }//end if    

            }//end action performed
        };// end action listener 

        queueTimer = new Timer((new Integer(prefs.get("queueRefresh", "5")).intValue()) * 60000, queueTimerActionListener);
        //queueTimer.setInitialDelay(2000);
        queueTimer.start();

    }// end startTimer

    /**
     * this method starts the timer thread that refreshes the user preferences
     */
    public void startPreferenceLoaderTimer() {

        prefsActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //System.out.println(evt.getActionCommand());
                try {

                    prefs.flush();
                } catch (Exception e) {
                    logExceptions(e);
                    e.printStackTrace();

                }//end catch

                if (prefs.getBoolean("close", true)) {
                    prefs.putBoolean("close", false);
                    System.exit(0);

                }//end if

                if (prefs.getInt("queueRefresh", 5) != queueRefreshInterval) {
                    queueRefreshInterval = prefs.getInt("queueRefresh", 5);
                    queueTimer.stop();
                    queueTimer.setDelay((new Integer(prefs.get("queueRefresh", "5")).intValue()) * 60000);
                    queueTimer.setInitialDelay(1000);
                    queueTimer.restart();

                }//end if

            }
        };

        prefsTimer = new Timer(1000, prefsActionListener);
        //prefsTimer.setInitialDelay(2000);
        prefsTimer.start();

    }// end startTimer

    /**
     * The bulk of the logic for transmitting files is probably done in this
     * method this method will attempt to connect to an ftp server and login to
     * the ftp server. if all three are successful then it will attempt to
     * upload a file or restart a previously interrupted upload.
     */
    public void sendFiles() {
        //if(!u.isConnected())
        u = new UploadIt();
        u.setIridiumFTP(this);
        compressFiles();
        File folder = new File(prefs.get("queuePath", ""));
        File uFile;
        //File[] listOfFiles = folder.listFiles();
        File[] listOfFiles = this.filesInQueue(prefs.get("queuePath", ""));
        int good = 0;

        boolean success = false;
        String server, user, password, localFile, remoteFile, badFile = "";
        server = prefs.get("serverName", "");
        user = prefs.get("userName", "");
        password = prefs.get("password", "");
        localFile = prefs.get("queuePath", "");
        remoteFile = prefs.get("uploadPath", "");

        try {
            for (int i = 0; i < listOfFiles.length; i++) {

                if (listOfFiles[i].isFile() && isValidZipFile(listOfFiles[i])) {
                    good++;

                }//end if
                else {
                    badFile = listOfFiles[i].getName();

                }//end else

            }//end for

            if (good > 0) {

                isTransmitting = true;

                if (u.connectToSite(server)) {//connect to the server

                    serverConnect = getTime() - internetConnect;
                    //unsuccessfulServerConnectAttempts = 0;
                    updateStatusTextArea("Connected to " + prefs.get("serverName", "@@@") + "\n");
                    if (u.login(user, password)) {//login to the ftp server
                        loginTime = getTime() - internetConnect;
                        u.enterLocalPassiveMode();
                        updateStatusTextArea("Entering passive mode\n");

                        updateStatusTextArea("Login successful\n");

                        if (u.binary()) {//switch to binary mode

                            if (unsuccessfulServerConnectAttempts > 0) { // restore check queue timer 

                                unsuccessfulServerConnectAttempts = 0;
                                queueTimer.stop();
                                queueTimer.setInitialDelay(2000);
                                queueTimer.restart();
                            }

                            updateStatusTextArea("Set to binary mode\n");

                            for (int i = 0; i < listOfFiles.length; i++) {
                                success = false;
                                if (listOfFiles[i].isFile()) {

                                    fileName = listOfFiles[i].getName();
                                    if (isValidZipFile(listOfFiles[i])) {
                                        uFile = new File(localFile + folder.separator + fileName);
                                        fileSize = uFile.length();
                                        updateStatusTextArea("Attempting to upload " + fileName + "\n");
                                        uploadStartTime = getTime();
                                        //success = u.appendFile(uFile,remoteFile+fileName,statusTextArea);
                                        success = u.appendFile(uFile, remoteFile + fileName, this);
                                        if (success) {
                                            uploadEndTime = getTime();
                                            averageTransferRate = 8000 * fileSize / (uploadEndTime - uploadStartTime);
                                            updateStatusTextArea(fileName + " successfully uploaded\n");
                                            addFile2DB(fileName);
                                            uFile.delete();
                                            updateTransmitTextArea();
                                        }// end where file upload verification happens if   
                                        else {
                                            uploadEndTime = uploadStartTime;
                                            averageTransferRate = -1;
                                            updateStatusTextArea(folder.separator + fileName + " not sent\n");
                                        }// end else
                                        //uploadEndTime = getTime();

                                    }//end if where zip is checked.
                                    else {
                                        updateStatusTextArea(fileName + " failed crc check, skipping\n");

                                    }//end else

                                }// end list of files if

                                disconnectTime = getTime() - internetConnect;
                                logText(connectionHeader, "," + serverConnect + "," + loginTime + "," + (uploadEndTime - uploadStartTime) + "," + fileSize + "," + averageTransferRate + "," + disconnectTime + "," + fileName + "\n", "connectionLog.csv");
                                //System.out.println("DialTime = " +dialTime+ " Internet Connect Time = " +internetConnect+ " FTP server Connect Time = " +serverConnect+ " File Name = " + fileName + " Login Time = "+loginTime+" Upload Start Time = "+uploadStartTime+" Upload End Time = "+uploadEndTime+" Disconnect Time = "+disconnectTime);

                            }// end for

                        }// end binary mode if
                        else {
                            updateStatusTextArea("Could not set to binary mode\n");
                        }// end else

                    }//end login 
                    else {

                        unsuccessfulServerConnectAttempts++;
                        updateStatusTextArea("Could not log in\n");
                        if (unsuccessfulServerConnectAttempts >= 5) {
                            delaySendTimer();
                        }

                    }// end else

                }// end if where connect to site
                else {
                    unsuccessfulServerConnectAttempts++;
                    updateStatusTextArea("Could not connect to " + prefs.get("serverName", "@@@") + "\n");
                    if (unsuccessfulServerConnectAttempts >= 5) {
                        delaySendTimer();
                    }//end if

                }// end else

                u.closeConnection();

                isTransmitting = false;

            }//end if wherer is good 
            else {
                if (listOfFiles.length > 0) {
                    updateStatusTextArea(badFile + " is not a valid zip file\n");
                }//end if

            }//end else
        }// end try
        catch (Exception e) {
            logExceptions(e);
            e.printStackTrace();

            try {
                u.closeConnection();
            } catch (Exception e1) {
                logExceptions(e1);
                e1.printStackTrace();

            }//end catch

            isTransmitting = false;
            unsuccessfulServerConnectAttempts++;
            updateStatusTextArea("An exception occurred while trying\n");
            updateStatusTextArea("to establish a connection\n");
            if (unsuccessfulServerConnectAttempts >= 5) {
                delaySendTimer();
            }//end if

        }// end catch
//logText(dialTime+","+internetConnect+","+serverConnect+","+loginTime+","+(uploadStartTime-uploadEndTime)+","+disconnectTime+","+fileName+"\n","connectionLog.csv");

    }// end sendFile

    void delaySendTimer() {
        queueTimer.stop();
        queueTimer.setInitialDelay(86400000);
        queueTimer.restart();
        updateStatusTextArea("There have been " + unsuccessfulServerConnectAttempts + " failed\n");
        updateStatusTextArea("attempts to connect to the server\nnext attempt will be in 24h\n");

    }//end delyaSendTimer    

    /**
     * This is the event handler for the timer thread. every time the timer is
     * up, this method is called. This method updates the files listed and
     * starts the transmission process
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {

        /*

         updateTransmitTextArea();
         String fileList = fileLister(prefs.get("queuePath", ""));

         if(!fileList.equals("") && (prefs.get("transmitCheckbox",false)&& !isTransmitting()) &&  !pppConnect.isAlive()){
         pppConnect = new PPPConnectThread();
         pppConnect.start();




  
         }

         */
    }// end actionPerformed

    /**
     * Updates the list of files to transmit
     */
    public void updateTransmitTextArea() {

        String lofs = fileLister(prefs.get("queuePath", ""));

    //transmitTextArea.setText("");
        //transmitTextArea.append(lofs);
        //transmitTextArea.setCaretPosition(0);
        sendMessageOnSocket("<QUEUE>");

        //queueLocationTextField.setText(prefs.get("queuePath",""));
        if (lofs.trim().equals("")) {
            lofs = ";EMPTY;";
            sendMessageOnSocket("<FILES>" + lofs.replace("\n", "").trim() + "</FILES>");
        } else {
            String[] files = lofs.split("\n");
            for (int i = 0; i < files.length; i++) {
                sendMessageOnSocket("<FILE>" + files[i].trim() + "</FILE>");

            }// end for

        }//end else
        sendMessageOnSocket("</QUEUE>");

    }// end

    /**
     * Updates the status of the upload/attempt
     *
     * @param status
     */
    public void updateStatusTextArea(String status) {
        logText(status, "log.txt");
        sendMessageOnSocket("<FTPSTATUS>" + status.replace("\n", "").trim() + "</FTPSTATUS>");
    //int lineCount = statusTextArea.getLineCount();

    //lineCount-=1;
        //if (lineCount<0 )
        //    lineCount = 0;
        //statusTextArea.append(status);
        //statusTextArea.revalidate();
    }// updateStatusTextArea

    /**
     * returns weather or not there is a transmission currently happening
     *
     * @return
     */
    public boolean isTransmitting() {

        return isTransmitting;

    }// end isTransmitting

    /**
     * This method is called to attempt a ppp connection using the windows
     * RASDialer
     */
    public class PPPConnectThread extends Thread {

        @Override
        public void run() {
            boolean rasIsAlive = false;
            resetCounters(); // reset time counters
            dialTime = getTime(); // get dial time
            if (!prefs.getBoolean("phoneBookEntryCheckBox", false)) {
                rasIsAlive = false;
            } else {
                rasIsAlive = rD.isAlive();
            }

            try {

                if (!rasIsAlive && prefs.getBoolean("phoneBookEntryCheckBox", false) && !prefs.get("phoneBookentryTextField", "Iridium").equals("")) {

                    updateStatusTextArea("Dialing phonebook entry " + prefs.get("phoneBookentryTextField", "Iridium") + "\n\n");

                    if (rD.openConnection(prefs.get("phoneBookentryTextField", "Iridium"))) {
                        internetConnect = getTime();
                        updateStatusTextArea("PPP connection Successful\n\n");

                        sendFiles();
                        if (rD.isAlive()) {
                            rD.closeConnection(prefs.get("phoneBookentryTextField", "Iridium"));
                            disconnectTime = getTime() - internetConnect;
                            updateStatusTextArea("Connection to " + prefs.get("phoneBookentryTextField", "Iridium") + " is now closed\n\n");
                        }//end if      .replace("\n", "")      
                        else {
                            disconnectTime = getTime() - internetConnect;
                            updateStatusTextArea("Connection to " + prefs.get("phoneBookentryTextField", "Iridium") + " was already closed\n\n");
                        }// end else
                    }// end if

                }// end if

            }// rnd try
            catch (RasDialerException e) {
                logExceptions(e);
                if (e.getMessage().contains("The port is already in use or is not configured for Remote Access dialout.")) {
                    queueTimer.stop();
                    queueTimer.setInitialDelay(1000);
                    queueTimer.restart();

                }// end if
                updateStatusTextArea(e.getMessage() + "\n");;
            }// end catch
            if (prefs.getBoolean("transmitCheckbox", false) && !prefs.getBoolean("phoneBookEntryCheckBox", false)) {
                //if(connectToServer()){
                internetConnect = getTime();
                sendFiles();
                disconnectTime = getTime() - internetConnect;
                //}
            }// if

            logText(connectionHeader, ",-1,-1,-1,-1,-1," + disconnectTime + ",NONE\n", "connectionLog.csv");

        }// end run

    }// end PPPConnectThread

    /**
     * When called strings passed to it are appended to a file
     *
     * @param line
     * @param logFileName
     */
    public void logText(String line, String logFileName) {

        logFileName = prefs.get("logFilePath", "") + File.separator + logFileName;

        try {
            line = line.replaceAll("\n", "");
            line = getDate() + " : " + line + NL;
            FileWriter logFile = new FileWriter(logFileName, true);
            logFile.append(line);
            logFile.close();
        } catch (Exception e) {

        }// end catch
    }// end logText

    /**
     * When called strings passed to it are appended to a file
     *
     * @param line
     * @param logFileName
     */
    public void logText(String header, String line, String logFileName) {

        logFileName = prefs.get("logFilePath", "") + File.separator + logFileName;
        try {
            File f;
            header = header.replaceAll("\n", "");
            header = header + NL;

            line = line.replaceAll("\n", "");
            line = getDate() + " " + line + NL;
            f = new File(logFileName);

            if (f.exists()) {
                FileWriter logFile = new FileWriter(logFileName, true);
                logFile.append(line);
                logFile.close();
            }//end if
            else {
                FileWriter logFile = new FileWriter(logFileName, true);
                logFile.append(header);
                logFile.append(line);
                logFile.close();
            }// end else

        } catch (Exception e) {

        }// end catch
    }// end logText

    void logExceptions(Exception e) {
        logText("--------------------ERROR-----------------------", "exceptions.txt");
        logText(e.toString(), "exceptions.txt");

    }//end logExceptions

    /**
     * returns the current GMT date
     *
     * @return
     */
    public String getDate() {

        Date currentDate = new Date();
        return currentDate.toGMTString();

    }/// end getDate

    /**
     * returns the current time
     *
     * @return
     */
    public long getTime() {
        return new Date().getTime();
    }// end getTime

    /**
     * this method zips files that are in the queue that don't already have the
     * zip extension. after compressing, it deletes the original file the method
     * does not verify that a file with a zip extension is really a zip file.
     */
    public void compressFiles() {

        IridiumZipper iz = new IridiumZipper();
        File folder = new File(prefs.get("queuePath", pWD.getAbsolutePath()));
        File[] listOfFiles = folder.listFiles();
        String fns[];
        if (listOfFiles != null && listOfFiles.length > 0) {
            for (int i = 0; i < listOfFiles.length; i++) {
                fns = listOfFiles[i].getName().split("\\.");
                if (fns.length > 1 && !fns[1].toLowerCase().equals("zip") && listOfFiles[i].isFile()) {
                    iz = new IridiumZipper();
                    if (iz.compress(listOfFiles[i])) {
                        listOfFiles[i].delete();
                    }
                }// end if

            }//end for

        }//end if

    }//compress the files

    static boolean isValidZipFile(final File file) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (ZipException e) {

            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
            }
        }
    }

    void setDirExists(boolean b) {
        dirExists = b;
    }

    boolean getDirExists() {
        return dirExists;
    }

    void sendMessageOnSocket(String msg) {

        if (messageMan != null) {
            messageMan.sendToAllClients(msg + "\r");
        }//end if
    }
}// end class
