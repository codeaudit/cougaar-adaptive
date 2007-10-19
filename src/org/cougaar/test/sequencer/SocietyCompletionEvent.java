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
* Workfile: NodeCompletionEvent.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer;

import java.util.Map;
import java.util.Set;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class SocietyCompletionEvent<S extends Step, R extends Report>
        extends UniqueObjectBase {
    private final S step;
    private final Map<String, Set<R>> reports;
    private final boolean successful;
    
    public SocietyCompletionEvent(UIDService uids, S step, Map<String, Set<R>> reports, boolean success) {
        super(uids.nextUID());
        this.step = step;
        this.reports = reports;
        this.successful = success;
    }
    
    public S getStep() {
        return step;
    }

    public Map<String, Set<R>> getReports() {
        return reports;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
}