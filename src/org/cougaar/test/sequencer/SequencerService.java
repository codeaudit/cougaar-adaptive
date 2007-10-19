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
 * Workfile: RegressionSequencer.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import org.cougaar.core.component.ServiceBroker;

/**
 * A generic service that walks through a sequence of steps
 * passing each step to each member collection of {@link Worker}
 * engines.  Each engine in turn constructs and returns a {@link Report}.
 * A step is only complete when every engine has reported back.
 * 
 * NB: The {@link ServiceBroker} api doesn't support generic
 * services yet, so for now a given broker can only have one
 * SequencerService registered.
 * 
 * @param S Defines the steps, typically as an enum
 * @param R Defines the reports returned by the processing engines
 * @param C Defines the processing context
 */
public interface SequencerService<S extends Step, R extends Report, C extends Context> {

    /**
     * Registers a {@link Worker} that will be asked to create
     * a report for each step.
     * 
     * @param id  An id for this worker; must be unique across the society.
     * 
     * @param worker The engine that will perform the processing
     * for each step in the given context.
     * 
     */
    void registerWorker(String id, Worker<S, C> worker);

    
    /**
     * Invoked to indicate that one of the registered engines
     * has completed processing a given step.
     * 
     * @param name The id of the processing engine that completed
     * @param step The step completed
     * @param report The generated report of results
     */
    void done(String name, S step, R report);
}
