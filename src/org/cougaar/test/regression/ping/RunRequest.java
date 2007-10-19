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
* Created : Aug 14, 2007
* Workfile: RunRequest.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression.ping;

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObjectBase;

abstract public class RunRequest extends UniqueObjectBase
   {
    public RunRequest(UID uid) {
        super(uid);
    }

    private int runners;
    private boolean failed;
    
    // XXX: Is ++ atomic?
    public synchronized void inc() {
        ++runners;
    }

    public int getRunners() {
        return runners;
    }

    public boolean isFailed() {
        return failed;
    }

    public void forceFailed() {
        this.failed = true;
    }
}
