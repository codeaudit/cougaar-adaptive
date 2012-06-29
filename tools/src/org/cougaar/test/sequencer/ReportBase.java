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
 * $Date: 2008-02-26 18:08:00 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

public class ReportBase
      implements Report {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private boolean successful;
   private String reason;
   private final String worker;

   public ReportBase(String worker, boolean success, String reason) {
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