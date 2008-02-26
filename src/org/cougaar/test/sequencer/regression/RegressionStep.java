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
 * Created : Aug 9, 2007
 * Workfile: RegressionStep.java
 * $Revision: 1.1 $
 * $Date: 2008-02-26 18:23:40 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer.regression;

import org.cougaar.test.sequencer.Step;

public enum RegressionStep implements Step {
    START_TEST,
    START_STEADY_STATE_COLLECTION,
    END_STEADY_STATE_COLLECTION,
    END_TEST,
    SUMMARY,
    SHUTDOWN;
    
    /**
     * This is here only for the Task servlet object xml
     * display, since that will only show the values of
     * bean reader methods.
     */
    public String getStepName() {
        return name();
    }
}
