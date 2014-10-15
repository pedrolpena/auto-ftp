
/*
 * this is an example of how not to program but I'm in a hurry cause
 * hte boat will be here soon
 * 
 * 
 */


package autoftp;
import java.io.*;
import java.util.zip.*;


/**
 *
 * @author Pedro.Pena
 */
public class IridiumZipper {
    public IridiumZipper(){};
    public IridiumZipper(File f){
        //base64.exe

        //File f = new File("c:\\junk\\backupaaa.jpg");
        compress(f);



    }


    boolean compress(File theFile){ // accepts a file and compresses it
        final int BUFFER = 2048;
        String fn[] = theFile.getName().split("\\.");
        String zippedFileName = "";

        try {
            if(fn.length > 1){

                zippedFileName = fn[0]+".zip";
               
            }// end if
            else{
                zippedFileName = theFile.getName();
            }// end else

            zippedFileName =  theFile.getParent()+theFile.separator+zippedFileName;
            //System.out.println(zippedFileName);

            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zippedFileName);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
            //out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER];
            // get a list of files from current directory
            //File f = new File(".");
            //String files[] = f.list();
            
            //for (int i=0; i<files.length; i++) {
                //System.out.println("Adding: "+theFile.getName());
                FileInputStream fi = new FileInputStream(theFile);
                origin = new BufferedInputStream(fi, BUFFER);


                
                ZipEntry entry = new ZipEntry(theFile.getName());
                out.putNextEntry(entry);
                int count;
            
                while((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
             }// end while
                origin.close();
         //}// end for
            out.close();
            //System.out.println("done!");
            return true;
      }// end try
   
      
      catch(Exception e) {
          e.printStackTrace();
          return false;
      }// end catch

    
    }// end compress

}
