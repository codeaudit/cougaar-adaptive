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
 * Workfile: Condition.java
 * $Revision: 1.2 $
 * $Date: 2008-02-28 15:41:49 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import java.io.Serializable;

public interface Context extends Serializable {
    public boolean hasFailed();
    public int getWorkerTimeout();
    public String getParameter(String key);
}
