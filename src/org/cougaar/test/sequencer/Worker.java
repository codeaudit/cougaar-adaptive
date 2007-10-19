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
 * Workfile: TesterCallback.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

/**
 * An arbitrary processing engine that will be asked
 * to construct a report for each step in a sequence.
 * The work is asynchronous and can happen in a thread
 * other than the one in which {@link #perform} invoked.
 * 
 * The Worker must register with some {@link SequencerService}, 
 * and will invoke its 'done' method when a report is available.
 * 
 * @see SequencerService
 * 
 */
public interface Worker<S extends Step, C extends Context> {
    void perform(S step, C context);
}
