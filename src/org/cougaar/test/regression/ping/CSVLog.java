/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Aug 24, 2007
 * Workfile: CSVLog.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

//Manages a CVS File in the run directory
package org.cougaar.test.regression.ping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.cougaar.util.log.Logger;

public class CSVLog {
    private java.io.File file;
    private FileOutputStream out;
    private PrintStream p;
    private String fileName;
    private Logger log;

    public CSVLog(String fileName, String header, Logger log) {
        this.fileName=fileName;
        this.log=log;
        try {
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
                out = new FileOutputStream(file);
                p = new PrintStream( out );
                printLn(header);
                log.info("Created new csv file="+fileName);
            }else {
                out = new FileOutputStream(file,true);
                p = new PrintStream( out );
                log.info("Reused csv file="+fileName);
            }
        }   
        catch (Exception e)  {
            log.error("Error creating file=" +fileName);
        }
    }

    public void field(String field) {
        if (p!=null) {
            p.print(field +", ");
        }else {
            log.error("PrintLn failed to file="+fileName);
        }
    }
    
    public void field(int field) {
       field(Integer.toString(field));       
    }
    
    public void field(long field) {
        field(Long.toString(field));      
     }
    
    public void field(double field) {
        field(Double.toString(field));      
     }
 
    public void field(float field) {
        field(Float.toString(field));      
     }
     
    public void printLn(String line) {
        if (p!=null) {
            p.println(line);
        }else {
            log.error("PrintLn failed to file="+fileName);
        }
    }

    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            log.error("Could not close file="+fileName);
        }        
    }


}
