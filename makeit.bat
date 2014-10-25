echo off

javac -source 1.7 -target 1.7 -d .\ -cp .\lib\commons-net-3.3.jar;.\lib\sqlitejdbc-v056.jar .\src\autoftp\*.java   
jar cfm AutoFTP.jar manifest.txt autoftp\*.class

IF EXIST .\dist goto deletedist

:deletedist
del /q /s .\dist  > nul
rmdir /q /s .\dist  > nul
:exit

mkdir .\dist
mkdir .\dist\lib
move /y AutoFTP.jar .\dist > nul
copy /y .\lib .\dist\lib > nul
del /s /q .\autoftp  > nul
rmdir /s /q .\autoftp  > nul


