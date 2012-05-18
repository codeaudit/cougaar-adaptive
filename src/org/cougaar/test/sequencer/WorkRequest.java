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
* Workfile: WorkRequest.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class WorkRequest<S extends Step, C extends Context> extends UniqueObjectBase {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final S step;
    private final C context;

    public WorkRequest(UIDService uids, S step, C context) {
        super(uids.nextUID());
        this.step = step;
        this.context = context;
    }

    public S getStep() {
        return step;
    }

    public C getContext() {
        return context;
    }
}