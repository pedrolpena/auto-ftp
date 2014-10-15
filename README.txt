
This is an FTP program that can automatically upload files from a specified queue
to an FTP server. The program can be configured to check the queue every X minutes.
It was originally written to upload data from ships via an Iridium satellite
connection but can be used to upload data over an established network connection.
When using it on Microsoft Windows, it can dial using the Windows rasdialer,
so long as there is an address book entry.

**Compiling and running.
To compile, make sure that a java sdk version of at least 1.6 is installed
and that the jar archive tool is installed.

You can check by typing "javac -version"
you should see something like "javac 1.6.0_32"

Type "jar" and your screen should scroll with many jar options.

When you list the contents of the directory you should see the following.

lib             directory containing libraries  
makeit.bat      batch script to compile and archive the program 
makeit.sh       bash  script to compile and archive the program 
manifest.txt    info that will be added to the manifest in the resulting jar file
README.txt      this document
src             directory with source files.

In Windows run makeit.bat in linux/unix run makeit.sh .
In linux/unix you will have to make makeit.sh executable.
To make it executable type

"chmod +x makeit.sh"

After running, everything you need will be in the dist directory.

To run the program enter the dist directory and type.

"java -jar autoFTP.jar "

If for some reason the script doesn't work you can compile and archive with the following commands.

For linux/unix
"javac -source 1.6 -target 1.6 -d ./ -cp ./lib/commons-net-3.3.jar:./lib/sqlitejdbc-v056.jar src/autoftp/*.java"
"jar cfm AutoFTP.jar manifest.txt autoftp/*.class"


For Windows
"javac -source 1.6 -target 1.6 -d .\ -cp .\lib\commons-net-3.3.jar;.\lib\sqlitejdbc-v056.jar .\src\autoftp\*.java"   
"jar cfm AutoFTP.jar manifest.txt autoftp\*.class"

Keep the AutoFTP.jar file and the lib folder together.
You can create a directory like "dist" and copy AutoFTP.jar and the lib folder into it.
