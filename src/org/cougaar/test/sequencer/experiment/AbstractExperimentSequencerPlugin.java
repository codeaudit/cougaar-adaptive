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
 * Workfile: RegressioNodeSequencerPlugin.java
 * $Revision: 1.1 $
 * $Date: 2008-02-26 21:10:05 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer.experiment;

import java.util.Map;
import java.util.Set;

import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.ContextBase;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SequencerPlugin;
import org.cougaar.test.sequencer.SocietyCompletionEvent;
import org.cougaar.util.annotations.Cougaar;

abstract public class AbstractExperimentSequencerPlugin<R extends Report>
    extends SequencerPlugin<ExperimentStep, R, Context>
    implements ExperimentSteps {

    @Cougaar.Arg(name = "suiteName", defaultValue = "", description = "Name for suite of tests")
    public String suiteName;
    
    boolean hasAttemptedToShutdown = false;

    protected Context makeContext(ExperimentStep step, boolean hasFailed, int workerTimeout) {
        return new ContextBase(workerTimeout, hasFailed);
    }

    protected ExperimentStep getFirstStep() {
        return SOCIETY_READY;
    }

//    protected RegressionStep getNextStep(RegressionStep step) {
//        
//    }

    // This is run only after all steps (including Shutdown Step) have been successfully run
    // Since the Sequencer can run in an agent, it does not have permission to run ncs.shutdown();
    // Log just an informative message, and expect that the Local node aggregator will kill the
    // Node as instructed during the Shutdown Step
    protected void sequenceCompleted() {
        // Ran through all steps successfully, shutdown cleanly
        String status = failedDuringSequence ? "Failed" : "Successful";
        log.shout("Regression suite " + suiteName + " " + status);
     }

    // This can be called after any step
    protected void sequenceFailed(SocietyCompletionEvent<ExperimentStep, R> evt) {
        StringBuffer msg = new StringBuffer();
        msg.append("Regression suite ").append(suiteName).append(" FAILED at step ").append(evt.getStep());
        Map<String, Set<R>> reportsMap = evt.getReports();
        if (!reportsMap.isEmpty()) {
            String seperator = " ";
            msg.append(" because");
            for (Map.Entry<String, Set<R>> entry : reportsMap.entrySet()) {
                String nodeId = entry.getKey();
                for (R report : entry.getValue()) {
                    if (!report.isSuccessful()) {
                        msg.append(seperator).append("worker ").append(report.getWorker());
                        msg.append(" on node ").append(nodeId);
                        msg.append(" said: \"").append(report.getReason()).append("\"");
                        seperator = ", ";
                    }
                }
            }
        }
        log.shout(msg.toString());
        // Submit a request to perform the Shutdown step....
        // All bets are off if this will actually complete, because there is some failure out there
        // but we do not know what it is.
        if (hasAttemptedToShutdown) {
            log.warn("Already Attempted to Shutdown, so doing nothing more");
        } else {
            log.warn("Attempting to shutdown the Regression society by skipping to Shutdown step");
            hasAttemptedToShutdown=true;
            publishNodeRequestStep(ExperimentSteps.SHUTDOWN);
        }
    }

   
}
