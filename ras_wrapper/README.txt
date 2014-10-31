*************************************************************************
*                                                                       *
* rasdial, a bash wrapper around pppd to mimic windows rasdial.exe.     *
* Copyright (C) 2014  Pedro Pena                                        *
*                                                                       *
* This program is free software: you can redistribute it and/or modify  *
* it under the terms of the GNU General Public License as published by  *
* the Free Software Foundation, either version 3 of the License, or     *
* any later version.                                                    *
*                                                                       * 
* This program is distributed in the hope that it will be useful,       *
* but WITHOUT ANY WARRANTY; without even the implied warranty of        *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
* GNU General Public License for more details.                          *
*                                                                       *
* You should have received a copy of the GNU General Public License     *
* along with this program.  If not, see <http://www.gnu.org/licenses/>. *
*                                                                       *                                         
*************************************************************************

To use the wrapper, you will have to have pppd installed and with appropriate permissions
to use it.

-----------------
-INSTALLING PPPD-
-----------------
These instructions work under debian.

to install
"sudo apt-get install ppp"

in order to use pppd the user must be part of the dip group
too add to group dip

"sudo usermod -a -G dip USERNAME"

in some cases it is necessary for a user to run pppd with root privileges.
DO THIS ONLY IF NECESSARY, it is highly recommended against.
"chmod u+s /usr/sbin/pppd"

-------------------------------------------------------
-INSTALLING WRAPPER FOR RASDIAL (ONLY FOR NON WINDOWS)-
-------------------------------------------------------

here is a list of the included files.

aspergarria - a test script I made to login to my server
cmd         - a script to mimic a single case of windows cmd.exe
install.sh  - the rasdial wrapper install script 
iridium.tmp - used to create a chat script for logging into the iridium dialup gateway
options     - option file for pppd
ppp-off     - bash script that closes a connection on ppp0
ppp-on      - bash script to start pppd(this should actually not be there,
                  it will be created when running install.sh) 
rasdial.tmp - used to create rasdial, the wrapper that mimics  certain aspects of rasdial.exe
start_chat  - bash script that starts the chat program(this should actually not be there,
                  it will be created when running install.sh) 





For this wrapper to work it must be installed somewhere in the path.
This script must be run as sudo unless the -o and -l are supplied with paths
for which the user has rw privileges.

make the script executeable by typing
"chmod +x install.sh"


you can get the install options by typing 
"./install.sh -h"


To run without any options type
"sudo ./install.sh"

By default the bash scripts will be copied to 
"/usr/local/sbin"
The chat scripts will be copied to
"/usr/local/chat-scripts"
The pppd options file will be copied to 
"/etc/ppp"
The serial port will be set to
"/dev/ttyUSB0"
The baudrate will be set to.
"19200"

Example with options

"sudo ./install.sh -l /etxe/kepa -p /dev/ttyS7 -b 9600 -o /etxe/kepa/temp"


---------------------------
-MAKING SURE RASDIAL WORKS-
---------------------------

Modify the aspergarria chat script for your specific needs.
include a username and password if required.

"rasdial aspergarria"

you shoud see stuff on the screen and activity on your modem.

If you're using an iridium modem then the included script should work.
Modify it if you have to.

"rasdial iridium"



















