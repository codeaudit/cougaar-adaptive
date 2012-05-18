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
 * Workfile: RegressionAggregatorPlugin.java
 * $Revision: 1.1 $
 * $Date: 2008-02-26 18:23:40 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer.regression;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.NodeAggregatorPlugin;
import org.cougaar.test.sequencer.NodeCompletionEvent;
import org.cougaar.test.sequencer.NodeRequest;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;
/**
 * Handles node termination based on the status held in the
 * {@link RegressionStep#SHUTDOWN} Context.
 */
public class RegressionAggregatorPlugin
    extends
        NodeAggregatorPlugin<RegressionStep, Report, Context> {

    @Cougaar.Arg(name = "maxIdleTime", defaultValue = "120000", // two minutes
                 description="Timeout for max time between steps. The Node will Crash when this timeout is exceeded") 
    public int maxIdleTime;

    private Context shutdownContext;
    private Alarm deadManAlarm;
    
    @Override
   protected Report makeWorkerTimoutFailureReport(RegressionStep step, String reason) {
        return new ReportBase(nodeId,false,reason);
    }
    
    @Override
   protected void setupSubscriptions() {
        super.setupSubscriptions();
        deadManAlarm=executeLater(maxIdleTime, new DeadManTimer());
    }
        
    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeCompletionEvent(NodeRequest<RegressionStep, Context> event) {
        RegressionStep step = event.getStep();
        deadManAlarm=restartDeadManTimer(deadManAlarm, maxIdleTime);
        switch (step) {
            case SHUTDOWN:
                shutdownContext = event.getContext();
                break;
        }
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeCompletionEvent(NodeCompletionEvent<RegressionStep, Report> event) {

        switch (event.getStep()) {
            case SHUTDOWN:
                log.info("Shutdown: Delaying shutdown by " + shutdownContext.getWorkerTimeout()
                +" hasFailed=" + shutdownContext.hasFailed());
                executeLater(shutdownContext.getWorkerTimeout(), new Terminator());
                break;
        }
    }

  
        
    private final class Terminator implements Runnable {
        public void run() {
            if (shutdownContext.hasFailed()) {
                log.shout("Crashing Node now");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                System.exit(1);
            } else {
                log.shout("Shutting down Node Cleanly");
                //TODO: clean shutdown is not clean
                //getNodeControlService().shutdown();
                System.exit(0);
            }
        }
    }
    
    // Helper method to restart an DeadManTimer
    Alarm restartDeadManTimer(Alarm alarm, int timeOut){
        if (alarm != null) {
            alarm.cancel();
        }
        return executeLater(timeOut, new DeadManTimer());
    }
    private final class DeadManTimer implements Runnable {
        public void run() {
            log.error("Crashing Node Now!!!! Exceeded Max Idle Time " + maxIdleTime);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            System.exit(1);
        }
 
       
    }

}
