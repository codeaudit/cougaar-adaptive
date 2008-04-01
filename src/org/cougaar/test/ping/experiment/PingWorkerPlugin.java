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
* Workfile: PingWorkerPlugin.java
* $Revision: 1.7 $
* $Date: 2008-04-01 09:19:54 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping.experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.PingQuery;
import org.cougaar.test.ping.StartRequest;
import org.cougaar.test.ping.StopRequest;
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentWorkerPlugin;
import org.cougaar.util.TypedProperties;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingWorkerPlugin extends ExperimentWorkerPlugin implements PingSteps {
    private StopRequest stopRequest;
    private StartRequest startRequest;
    private Map<String, Anova> initialStatistics, finalStatistics;
    private boolean failed = false;
    private String reason = "no reason";
    
    @Cougaar.Arg(name="pingerCount", required=true)
    public int pingerCount;
    
    protected void doStep(ExperimentStep step, Context context) {
    	TypedProperties tprops = new TypedProperties(context.getProperties());
        if (START_TEST.equals(step)) { 
        	long wait = tprops.getLong(PING_DELAY_PROPERTY, 0);
        	int size = tprops.getInt(PING_SIZE_PROPERTY, 0);
        	String statKind = tprops.getString(PING_STATISTICS_PROPERTY, "ANOVA");
            startRequest = new StartRequest(uids.nextUID(), wait, size, statKind );
            blackboard.publishAdd(startRequest);
            // defer until all start requests have returned
        } else if (START_STEADY_STATE.equals(step)) {
           initialStatistics = gatherStatistics();
            stepCompeleted(step, makeReport(step));    
        } else if (END_STEADY_STATE.equals(step)) {
            finalStatistics = gatherStatistics();
            stepCompeleted(step, makeReport(step)); 
        } else if (END_TEST.equals(step)) {
            blackboard.publishRemove(startRequest);
            startRequest=null;
            stopRequest = new StopRequest(uids.nextUID());
            blackboard.publishAdd(stopRequest);
            // defer until all Stop requests have returned
        } else if (SUMMARY_TEST.equals(step)) {
            Report report = new SummaryReport(workerId, reason, initialStatistics, finalStatistics);
            stepCompeleted(SUMMARY_TEST, report);
        } else {
            stepCompeleted(step, new ReportBase(workerId, true, reason));
        }
        
    }
    
    // query blackboard for all ping queries
    // snapshot the statistics
    // store the statistics for later processing
    protected Map<String, Anova> gatherStatistics() {
		Map<String, Anova> statistics = new HashMap<String, Anova>();
		@SuppressWarnings("unchecked")
		Collection<SimpleRelay> relays = blackboard.query(new IsQueryRelay());
		for (SimpleRelay relay : relays) {
			PingQuery query = (PingQuery) relay.getQuery();
			Anova statistic = (Anova) query.getStatistic();
			String sessionName = statistic.getName();
			statistics.put(sessionName, statistic.snapshot());
		}
		return statistics;
	}
    
    protected Report makeReport(ExperimentStep step) {
        return new ReportBase(workerId,!failed,reason);
    }
    
    @Cougaar.Execute(on={Subscribe.ModType.ADD, Subscribe.ModType.CHANGE})
    public void executeStartRequest(StartRequest start) {
        if (start.equals(startRequest) && (startRequest.getRunners() == pingerCount)) {
            failed = startRequest.isFailed();
            stepCompeleted(START_TEST, makeReport(START_TEST));
        }
    }
    
    @Cougaar.Execute(on={Subscribe.ModType.ADD, Subscribe.ModType.CHANGE})
    public void executeStopRequest(StopRequest stop) {
        if (stop.equals(stopRequest) && (stopRequest.getRunners() == pingerCount)) {
            failed = stopRequest.isFailed();
            stepCompeleted(END_TEST, makeReport(END_TEST));
            // Get Ready for restart
            blackboard.publishRemove(stopRequest);
            stopRequest=null;
        }
    }
    /**
     * Blackboard query predicate to gather all the relays
     * holding PingQuery objects.
     */
    private class IsQueryRelay implements UnaryPredicate {
        public boolean execute(Object arg) {
            if (arg instanceof SimpleRelay) {
                SimpleRelay relay = (SimpleRelay) arg;
                return relay.getQuery() instanceof PingQuery;
            }
            return false;
        }
    }
}
