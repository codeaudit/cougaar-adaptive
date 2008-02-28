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
* Created : Aug 9, 2007
* Workfile: RegressionCondition.java
* $Revision: 1.2 $
* $Date: 2008-02-28 15:41:49 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer;

import java.util.Properties;


public class ContextBase implements Context {
    private final int workerTimout;
    private final boolean failed;
    private final Properties properties;

    public ContextBase(int workerTimeout, boolean failed) {
        this(workerTimeout, failed, new Properties());
    }
    
    public ContextBase(int workerTimeout, boolean failed, Properties properties) {
        this.workerTimout = workerTimeout;
        this.failed = failed;
        this.properties = properties;
    }

    public int getWorkerTimeout() {
        return workerTimout;
    }

    public boolean hasFailed() {
        return failed;
    }
    
    public String getParameter(String key) {
        return properties.getProperty(key);
    }
    
    public String toString() {
        return "Context: hasFailed="+failed+" workerTimeout="+workerTimout;
    }

    
}
