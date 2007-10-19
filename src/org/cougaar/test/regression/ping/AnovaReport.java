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
* Workfile: AnovaReport.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression.ping;

import org.cougaar.test.regression.RegressionReport;

public class AnovaReport implements RegressionReport {
    private boolean successful;
    private String reason;
    private final String worker;
          
    public AnovaReport(String worker, boolean success, String reason) {
        this.worker = worker;
        this.successful = success;
        this.reason = reason;
    }
  
    public boolean isSuccessful() {
        return successful;
    }
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getWorker() {
        return worker;
    }
}
