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
* Created : Sep 13, 2007
* Workfile: RegressionSequencerPlugin.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:53 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression;

import java.util.Collections;
import java.util.Set;

public class RegressionSequencerPlugin extends AbstractRegressionSequencerPlugin<RegressionReportBase> {
    
    protected Set<RegressionReportBase> makeNodeTimoutFailureReport(RegressionStep step, String reason) {
        RegressionReportBase report = new RegressionReportBase(agentId.getAddress(), false, reason);
        return Collections.singleton(report);
    }
}
