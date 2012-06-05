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
 * $Date: 2008-02-26 15:31:56 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObjectBase;

abstract public class RunRequest
      extends UniqueObjectBase {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

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
