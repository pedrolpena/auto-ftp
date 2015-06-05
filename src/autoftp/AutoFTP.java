/**
 * AutoFTP is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * AutoFTP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * AutoFTP. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/*
 * AutoFTP.java
 *@author Pedro Pena
 * Created on Jan 20, 2011, 10:39:30 AM
 */
package autoftp;

import java.io.*;
import java.util.zip.*;
import java.util.prefs.*;
import java.util.Date;
import javax.swing.Timer;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
    int unsuccessfulLoginAttempts = 0;    // this holds the number of failed login attempts
    int unsuccessfulServerConnectAttempts = 0; //  this holds the number of failed server connects 
    int queueRefreshInterval = 5;

    long dialTime; //holds time when a dialout attempt is made
    long internetConnect; //holds the time the actual internet connection is made
    long serverConnect; //holds the time when connection to the FTP server is made
    long loginTime; //holds the time when a successful login to the ftp server is made
    long uploadStartTime;//holds the time when an upload/append is started
    long uploadEndTime; //holds the time when an upload/append is completed
    long disconnectTime; //holds the time when the modem disconnects from the internet
    long fileSize; //holds the number of bytes
    long averageTransferRate;//holds the average transfer rate
    String args[];
    String fileName = "None", temp;
    String connectionHeader = "Time_Stamp,Server_Connect(ms),Login_Time(ms),Upload_duration(ms),File_Size(Bytes),Transfer_Rate(bps),Total_Time_Connected(ms),File_Name\n";

    /**
     * Creates new form IridiumFTP
     */
    public AutoFTP() {

        init();

    }
    
        public AutoFTP(String args[]) {
            this.args=args;

        init();

    }

    public static void main(String args[]) {

        new AutoFTP(args);

    }

    /**
     * this method is called from the constructor and this is where
     * initialization stuff should be placed.
     */
    private void init() {
        String remoteFile = "";

        try {

            pWD = new File(System.getProperty("user.home") + File.separatorChar + "auto_ftp_queue");
            logDIR = new File(System.getProperty("user.home") + File.separatorChar + "auto_ftp_logs");

            if (!pWD.exists()) {
                pWD.mkdir();
            }//end if

            if (!logDIR.exists()) {
                logDIR.mkdir();
            }//end if

            if (prefs.get("queuePath", "").equals("")) {
                prefs.put("queuePath", pWD.getAbsolutePath());

            }//end if

            int tempInt = 0;

            //--------initialize prefs-----------------//
            temp = prefs.get("password", "@@@");
            if (temp.equals("@@@")) {
                prefs.put("password", "password");
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
                prefs.put("userName", "username");
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
            int num;
            num = prefs.getInt("fileSizeLimit", -2);

            if (num == -2) {
                prefs.putInt("fileSizeLimit", -1);
            }//end if   

            //--------------------------------------//
            //database
            String dbPath = prefs.get("logFilePath", "") + File.separator;

            //---------------check for running instance--------------
            comClient = new CommandClient(host, port);
            comClient.start();

            if (comClient.isConnected()) {
                printToStdOut("shutting down because an instance of this program is already running");
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
            unsuccessfulLoginAttempts = 0;    // this holds the number of failed login attempts
            unsuccessfulServerConnectAttempts = 0; //  this holds the number of failed server connects

            compressFiles();
            queueStatusMessage();
            remoteFile = prefs.get("uploadPath", "/default/");
            if (remoteFile.length() > 0 && !remoteFile.endsWith("/")) {
                remoteFile += "/";
                prefs.put("uploadPath", remoteFile);
            }// end if 
            resetCounters();

            //startTimer();
            startQueueTimer();
            startPreferenceLoaderTimer();

            messageMan = new MessagingServer(host, port);
            messageThread = new Thread(messageMan);
            messageThread.start();

            pppConnect = new PPPConnectThread();
            rD = new RasDialer();
            compressFiles();

            statusMessage("AOML Auto FTPer version 2.1\n");
            statusMessage("java vendor " + System.getProperty("java.vendor") + "\n");
            statusMessage("java version " + System.getProperty("java.version") + "\n");
            queueStatusMessage();
            //sendFiles();

        }// end try
        catch (Exception e) {
            String error = e.toString();
            logExceptions(e);
            if (!error.contains("SocketTimeoutException")) {
                e.printStackTrace();
            }//end if
            else {
                printToStdOut("listening for socket");

            }//end else

            pWD = new File(System.getProperty("user.home") + File.separatorChar + "auto_ftp_queue");
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
                        this.statusMessage(fileName + " will not be transmitted because it was previously sent.\n");
                        try {
                            listOfFiles[i].delete();
                        }//end try
                        catch (Exception e) {
                            this.statusMessage(fileName + " could not be deleted, make sure the current user has\n");
                            this.statusMessage(" permission to delete this file\n");
                            this.logExceptions(e);

                        }//end catch
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
        long fileSizeLimit = -2;
        long fileLength = 0;
        boolean transmitted = false;
        boolean noLimit = false;
        boolean tooLarge = false;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                fileLength = listOfFiles[i].length();
                fileSizeLimit = prefs.getInt("fileSizeLimit", -1);
                transmitted = wasTransmitted(fileName);
                noLimit = (fileSizeLimit == -1);
                tooLarge = (fileLength > fileSizeLimit);

                if (!transmitted && (!tooLarge || noLimit)) {
                    ff[j++] = listOfFiles[i];

                }// end if
                else {

                    if (transmitted) {
                        this.statusMessage(fileName + " will not be transmitted because it was previously sent.\n");
                    }

                    if (tooLarge) {

                        this.statusMessage(fileName + " will not be transmitted because the file size exceeds " + fileSizeLimit + " bytes\n");
                    }

                    try {

                        listOfFiles[i].delete();

                    }//end try
                    catch (Exception e) {
                        this.statusMessage(fileName + " could not be deleted, make sure the current user has\n");
                        this.statusMessage(" permission to delete this file\n");
                        this.logExceptions(e);

                    }//end catch

                }// end else

            }// end if

        }// end for
        File[] f2 = new File[j];
        for (int i = 0; i < j; i++) {
            f2[i] = ff[i];
        }// end for
        return f2;
    }//end filesInQueue

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
            

                queueStatusMessage();
                String fileList = fileLister(prefs.get("queuePath", ""));

                if (!fileList.equals("") && (prefs.getBoolean("transmitCheckbox", false) && !isTransmitting()) && !pppConnect.isAlive()) {
                    pppConnect = new PPPConnectThread();
                    pppConnect.start();
                }//end if    

            }//end action performed
        };// end action listener 

        queueTimer = new Timer((new Integer(prefs.get("queueRefresh", "5")).intValue()) * 60000, queueTimerActionListener);
        queueTimer.start();

    }// end startTimer

    /**
     * this method starts the timer thread that refreshes the user preferences
     */
    public void startPreferenceLoaderTimer() {

        prefsActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
             

                //**********************************
                if (prefs.getBoolean("phoneBookEntryCheckBox", true)
                        && rD != null && !rD.isAlive()
                        && u != null && u.isConnected()) {
                    u.closeAllSockets();

                }//endif

                //**********************************                 
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
        prefsTimer.start();

    }// end startTimer

    /**
     * The bulk of the logic for transmitting files is probably done in this
     * method this method will attempt to connect to an ftp server and login to
     * the ftp server. if all three are successful then it will attempt to
     * upload a file or restart a previously interrupted upload.
     */
    public void sendFiles() {
        u = new UploadIt();
        u.setIridiumFTP(this);
        compressFiles();
        File folder = new File(prefs.get("queuePath", ""));
        File uFile;
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

                if (u.connectToSite(server, 30)) {//connect to the server

                    serverConnect = getTime() - internetConnect;
                    statusMessage("Connected to " + prefs.get("serverName", "@@@") + "\n");
                    if (u.login(user, password)) {//login to the ftp server
                        loginTime = getTime() - internetConnect;
                        u.enterLocalPassiveMode();
                        statusMessage("Entering passive mode\n");

                        statusMessage("Login successful\n");

                        if (u.binary()) {//switch to binary mode

                            if (unsuccessfulServerConnectAttempts > 0) { // restore check queue timer 

                                unsuccessfulServerConnectAttempts = 0;
                                queueTimer.stop();
                                queueTimer.setInitialDelay(2000);
                                queueTimer.restart();
                            }

                            statusMessage("Set to binary mode\n");

                            for (int i = 0; i < listOfFiles.length; i++) {
                                success = false;
                                if (listOfFiles[i].isFile()) {

                                    fileName = listOfFiles[i].getName();
                                    if (isValidZipFile(listOfFiles[i])) {
                                        uFile = new File(localFile + folder.separator + fileName);
                                        fileSize = uFile.length();
                                        statusMessage("Attempting to upload " + fileName + "\n");
                                        uploadStartTime = getTime();
                                        success = u.appendFile(uFile, remoteFile + fileName);
                                        if (success) {
                                            uploadEndTime = getTime();
                                            averageTransferRate = 8000 * fileSize / (uploadEndTime - uploadStartTime);
                                            statusMessage(fileName + " successfully uploaded\n");
                                            addFile2DB(fileName);

                                            try {
                                                uFile.delete();
                                            }//end try
                                            catch (Exception e) {
                                                this.statusMessage(fileName + " could not be deleted, make sure the current user has\n");
                                                this.statusMessage(" permission to delete this file\n");
                                                this.logExceptions(e);

                                            }//end catch

                                            queueStatusMessage();
                                        }// end where file upload verification happens if   
                                        else {
                                            uploadEndTime = uploadStartTime;
                                            averageTransferRate = -1;
                                            statusMessage(folder.separator + fileName + " not sent\n");
                                        }// end else

                                    }//end if where zip is checked.
                                    else {
                                        statusMessage(fileName + " failed crc check, skipping\n");

                                    }//end else

                                }// end list of files if

                                disconnectTime = getTime() - internetConnect;
                                logText(connectionHeader, "," + serverConnect + "," + loginTime + "," + (uploadEndTime - uploadStartTime) + "," + fileSize + "," + averageTransferRate + "," + disconnectTime + "," + fileName + "\n", "connectionLog.csv");
                            }// end for

                        }// end binary mode if
                        else {
                            statusMessage("Could not set to binary mode\n");
                        }// end else

                    }//end login 
                    else {

                        unsuccessfulServerConnectAttempts++;
                        statusMessage("Could not log in\n");
                        if (unsuccessfulServerConnectAttempts >= 5) {
                            delaySendTimer();
                        }

                    }// end else

                }// end if where connect to site
                else {
                    unsuccessfulServerConnectAttempts++;
                    statusMessage("Could not connect to " + prefs.get("serverName", "@@@") + "\n");
                    if (unsuccessfulServerConnectAttempts >= 5) {
                        delaySendTimer();
                    }//end if

                }// end else

                u.closeConnection();

                isTransmitting = false;

            }//end if wherer is good 
            else {
                if (listOfFiles.length > 0) {
                    statusMessage(badFile + " is not a valid zip file\n");
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
            statusMessage("An exception occurred while trying\n");
            statusMessage("to establish a connection\n");
            if (unsuccessfulServerConnectAttempts >= 5) {
                delaySendTimer();
            }//end if

        }// end catch

    }// end sendFile

    void delaySendTimer() {
        queueTimer.stop();
        queueTimer.setInitialDelay(86400000);
        queueTimer.restart();
        statusMessage("There have been " + unsuccessfulServerConnectAttempts + " failed\n");
        statusMessage("attempts to connect to the server\nnext attempt will be in 24h\n");

    }//end delyaSendTimer    

    /**
     * This is the event handler for the timer thread. every time the timer is
     * up, this method is called. This method updates the files listed and
     * starts the transmission process
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {

    }// end actionPerformed

    /**
     * Updates the list of files to transmit
     */
    public void queueStatusMessage() {

        String lofs = fileLister(prefs.get("queuePath", ""));

        sendMessageOnSocket("<QUEUE>");
        printToStdOut("\n=====Files in queue=====");

        if (lofs.trim().equals("")) {
            lofs = ";EMPTY;";
            sendMessageOnSocket("<FILES>" + lofs.replace("\n", "").trim() + "</FILES>");
            printToStdOut("\n   no files in queue\n");
        } else {
            String[] files = lofs.split("\n");
            for (int i = 0; i < files.length; i++) {
                printToStdOut("    " + files[i].trim());
                sendMessageOnSocket("<FILE>" + files[i].trim() + "</FILE>");

            }// end for

        }//end else
        sendMessageOnSocket("</QUEUE>");
        printToStdOut("========================\n");

    }// end

    /**
     * Updates the status of the upload/attempt
     *
     * @param status
     */
    public void statusMessage(String status) {
        printToStdOut(status.replace("\n", "").trim());
        logText(status, "log.txt");
        sendMessageOnSocket("<FTPSTATUS>" + status.replace("\n", "").trim() + "</FTPSTATUS>");

    }// statusMessage

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

                    statusMessage("Dialing phonebook entry " + prefs.get("phoneBookentryTextField", "Iridium") + "\n\n");

                    if (rD.openConnection(prefs.get("phoneBookentryTextField", "Iridium"))) {
                        internetConnect = getTime();
                        statusMessage("PPP connection Successful\n\n");

                        sendFiles();
                        if (rD.isAlive()) {
                            rD.closeAllConnections();
                            disconnectTime = getTime() - internetConnect;
                            statusMessage("Connection to " + prefs.get("phoneBookentryTextField", "Iridium") + " is now closed\n\n");
                        }//end if      
                        else {
                            disconnectTime = getTime() - internetConnect;
                            statusMessage("Connection to " + prefs.get("phoneBookentryTextField", "Iridium") + " was already closed\n\n");
                        }// end else
                    }// end if

                }// end if

            }// end try
            catch (RasDialerException e) {
                logExceptions(e);
                if (e.getMessage().contains("The port is already in use or is not configured for Remote Access dialout.")) {
                    queueTimer.stop();
                    queueTimer.setInitialDelay(1000);
                    queueTimer.restart();

                }// end if
                statusMessage(e.getMessage() + "\n");;
            }// end catch
            if (prefs.getBoolean("transmitCheckbox", false) && !prefs.getBoolean("phoneBookEntryCheckBox", false)) {
               
                internetConnect = getTime();
                sendFiles();
                disconnectTime = getTime() - internetConnect;
               
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
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(currentDate);

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
                        try {
                            listOfFiles[i].delete();
                        }//end try
                        catch (Exception e) {
                            this.statusMessage(fileName + " could not be deleted, make sure the current user has\n");
                            this.statusMessage(" permission to delete this file\n");
                            this.logExceptions(e);

                        }//end catch//end catch
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
    
    
    
    public void printToStdOut(String text)
    {
           if((args.length > 0 && !(args[0].toLowerCase().trim().equals("false"))) || args.length == 0 )
        {
            System.out.println(text);
            

        }
    
    }//end printToStdOut
}// end class
