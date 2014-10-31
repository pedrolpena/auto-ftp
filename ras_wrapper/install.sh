#!/bin/bash
# I got this options processing skeleton from http://wiki.bash-hackers.org/howto/getopts_tutorial

#default install values
baseInstallDir="/usr/local"
serialPort="/dev/ttyUSB0"
portBaud="19200"
pppdConfigFiles="/etc/ppp"

while getopts ":l:hp:b:o:" opt; do
  case $opt in
    l)
      baseInstallDir=$OPTARG
      ;;
    p)
      serialPort=$OPTARG
      ;;
    b)
      portBaud=$OPTARG
      ;;
    o)
      pppdConfigFiles=$OPTARG
      ;;
    h)
      echo "options are"
      echo "-h this help"
      echo "-l install location"
      echo "-p serial port device to which the modem is connected."
      echo "-b serial port baud rate."
      echo "-o where to copy pppd option file."
      exit 0
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

chatInstallDir=$baseInstallDir"/chat-scripts"
binLocation=$baseInstallDir"/sbin"
chatInstallDir=${chatInstallDir//\/\//\/}
binLocation=${binLocation//\/\//\/}


if [ ! -d "$chatInstallDir" ]; then
    mkdir "$chatInstallDir"
fi

if [ ! -d "$binLocation" ]; then
    mkdir "$binLocation"
fi

# create the ppp-on script with passed options
echo "#!/bin/bash" > ppp-on
echo "#the ppp-on script" >> ppp-on
echo "pppd "$serialPort" "$portBaud" connect \"$binLocation/start_chat \$1\"" >> ppp-on

# create the start_chat script with passed options
echo "#!/bin/bash" > start_chat
echo "#starts the chat script" >> start_chat
echo "chat -e -v -f "$chatInstallDir/"\$1" >> start_chat

# create the rasdial script with passed options
echo "#!/bin/bash" > rasdial
echo "#mimics windows rasdial.exe" >> rasdial
echo "chatInstallDir=\"$chatInstallDir\"" >> rasdial
echo "binLocation=\"$binLocation\"" >> rasdial

cat rasdial.tmp >> rasdial


# create the iridium chat script with passed options
cp iridium.tmp iridium
echo $portBaud" pppd" >> iridium


#change ownership to $SUDO_USER
chown $SUDO_USER:$SUDO_USER ppp-on
chown $SUDO_USER:$SUDO_USER start_chat
chown $SUDO_USER:$SUDO_USER rasdial
chown $SUDO_USER:$SUDO_USER iridium

#enable the executable bits
chmod +x ppp-on
chmod +x ppp-off
chmod +x start_chat
chmod +x rasdial
chmod +x cmd


#backup pppd option file in case its already there
if [ -f "$pppdConfigFiles/options" ]; then
    mv "$pppdConfigFiles/options" "$pppdConfigFiles/options"`date +%s`
fi

#copy files to destinations
cp iridium "$chatInstallDir"
cp aspergarria "$chatInstallDir"
cp ppp-on "$binLocation"
cp ppp-off "$binLocation"
cp start_chat "$binLocation"
cp rasdial "$binLocation"
cp cmd "$binLocation"
cp options "$pppdConfigFiles"













