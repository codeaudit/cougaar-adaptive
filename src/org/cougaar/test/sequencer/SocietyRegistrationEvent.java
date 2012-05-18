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
* Created : Sep 4, 2007
* Workfile: NodeRegistrationEvent.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class SocietyRegistrationEvent extends UniqueObjectBase {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final boolean successful;
    private final int numberWorkers;
    private final int numberNodes;
    
    public SocietyRegistrationEvent(UIDService uids, int numberWorkers, int numberNodes, boolean success) {
        super(uids.nextUID());
        this.successful = success;
        this.numberWorkers = numberWorkers;
        this.numberNodes = numberNodes;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public int getNumberWorkers() {
        return numberWorkers;
    }

    public int getNumberNodes() {
        return numberNodes;
    }
}