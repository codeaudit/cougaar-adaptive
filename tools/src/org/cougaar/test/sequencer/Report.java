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
 * Created : Aug 8, 2007
 * Workfile: Report.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import java.io.Serializable;

public interface Report
      extends Serializable {
   String getWorker();

   boolean isSuccessful();

   String getReason();
}
