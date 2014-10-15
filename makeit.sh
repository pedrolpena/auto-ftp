#!/bin/bash
javac -source 1.6 -target 1.6 -d ./ -cp ./lib/commons-net-3.3.jar:./lib/sqlitejdbc-v056.jar src/autoftp/*.java 
jar cfm AutoFTP.jar manifest.txt autoftp/*.class 
if [ -d "dist" ]; then
    rm -r dist
fi
mkdir ./dist
rm -r ./autoftp
mv -t ./dist ./AutoFTP.jar
cp -r ./lib ./dist


