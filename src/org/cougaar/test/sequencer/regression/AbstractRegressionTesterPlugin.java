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
* Workfile: AbstractRegressionTesterPlugin.java
* $Revision: 1.2 $
* $Date: 2008-02-26 21:10:06 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.sequencer.regression;

import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.WorkerPlugin;

abstract public class AbstractRegressionTesterPlugin<R extends Report>
    extends WorkerPlugin<RegressionStep, R, Context> {
    
    abstract protected R makeReport(RegressionStep step);
    
    @Override
    protected void doStep(RegressionStep step, Context context) {
        switch (step) {
            case START_TEST:
                doStartTest(context);
                break;
                
            case START_STEADY_STATE_COLLECTION:
                doStartSteadyStateCollection(context);
                break;
                
            case END_STEADY_STATE_COLLECTION:
                doEndSteadyStateCollection(context);
                break;
                
            case END_TEST:
                doEndTest(context);
                break;
                
            case SUMMARY:
                doSummary(context);
                break;
                
            case SHUTDOWN:
                doShutdown(context);
                break;
        }
    }


    protected void doStartTest(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do Start Test: "+context);
        }
        doneStartTest(makeReport(RegressionStep.START_TEST));
    }

    protected void doStartSteadyStateCollection(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do Start Collection: "+context);
        }
        doneStartSteadyStateCollection(makeReport(RegressionStep.START_STEADY_STATE_COLLECTION));
    }
    
    protected void doEndSteadyStateCollection(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do End Collection: "+context);
        }
        doneEndSteadyStateCollection(makeReport(RegressionStep.END_STEADY_STATE_COLLECTION));
    }
    

    protected void doEndTest(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do End Test: "+context);
        }
        doneEndTest(makeReport(RegressionStep.END_TEST));
    }

    protected void doSummary(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do Summary: "+context);
        }
        doneSummary(makeReport(RegressionStep.SUMMARY));
    }
    
    protected void doShutdown(Context context) {
        if (log.isInfoEnabled()) {
            log.info(workerId+ ": Do Shutdown: "+context);
        }
        doneShutdown(makeReport(RegressionStep.SHUTDOWN));
    }
    
    public void doneStartTest(R report) {
        stepCompeleted(RegressionStep.START_TEST, report);
    }
    
    public void doneStartSteadyStateCollection(R report) {
        stepCompeleted(RegressionStep.START_STEADY_STATE_COLLECTION, report);
    }
    
    public void doneEndSteadyStateCollection(R report) {
        stepCompeleted(RegressionStep.END_STEADY_STATE_COLLECTION, report);
    }
    
    public void doneEndTest(R report) {
        stepCompeleted(RegressionStep.END_TEST, report);
    }
    
    public void doneSummary(R report) {
        stepCompeleted(RegressionStep.SUMMARY, report);
    }
    
    public void doneShutdown(R report) {
        stepCompeleted(RegressionStep.SHUTDOWN, report);
    }
}
