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
* Created : Aug 15, 2007
* Workfile: RegressionTesterPlugin.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:53 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression;

import org.cougaar.util.annotations.Cougaar;


// Used for testing, domain tester plugins should extend AbstractRegressionTesterPlugin
public class RegressionTesterPlugin extends AbstractRegressionTesterPlugin<RegressionReportBase> {
    
    @Cougaar.Arg(name = "failStep", required=false)
    public RegressionStep failStep;

    @Cougaar.Arg(name = "delayStep", required=false)
    public RegressionStep delayStep;
 
    @Cougaar.Arg(name = "delayTime", required=false)
    public long delayTime;
    
    @Cougaar.Arg(name = "reason", defaultValue="no reason")
    public String reason;
  
   
    protected RegressionReportBase makeReport(RegressionStep step) {
        if (delayStep == step) {
            // nasty delay holding thread
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return new RegressionReportBase(workerId, step != failStep, reason);
    }

}
