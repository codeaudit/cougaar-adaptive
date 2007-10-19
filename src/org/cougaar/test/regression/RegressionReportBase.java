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
* Created : Sep 11, 2007
* Workfile: RegressionReportBase.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:53 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression;


public class RegressionReportBase implements RegressionReport {
    private final boolean successful;
    private final String reason;
    private final String worker;
    
    public RegressionReportBase( String worker, boolean success, String reason) {
        this.worker = worker;
        this.successful = success;
        this.reason=reason; 
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getReason() {
        return reason;
    }

    public String getWorker() {
        return worker;
    }
}