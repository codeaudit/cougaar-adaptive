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
* $Date: 2008-02-26 18:23:40 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer.regression;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.util.annotations.Cougaar;


// Used for testing, domain tester plugins should extend AbstractRegressionTesterPlugin
public class RegressionTesterPlugin extends AbstractRegressionTesterPlugin<Report> {
    
    @Cougaar.Arg(name = "failStep", required=false)
    public RegressionStep failStep;

    @Cougaar.Arg(name = "delayStep", required=false)
    public RegressionStep delayStep;
 
    @Cougaar.Arg(name = "delayTime", required=false)
    public long delayTime;
    
    @Cougaar.Arg(name = "reason", defaultValue="no reason")
    public String reason;
  
   
    protected Report makeReport(RegressionStep step) {
        if (delayStep == step) {
            // nasty delay holding thread
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return new ReportBase(workerId, step != failStep, reason);
    }

}
