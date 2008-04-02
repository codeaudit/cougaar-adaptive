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
* $Revision: 1.8 $
* $Date: 2008-04-02 14:56:24 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping.regression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.test.ping.PingQuery;
import org.cougaar.test.ping.StartRequest;
import org.cougaar.test.ping.StopRequest;
import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.test.sequencer.StatisticsReport;
import org.cougaar.test.sequencer.regression.AbstractRegressionTesterPlugin;
import org.cougaar.test.sequencer.regression.RegressionStep;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingTesterPlugin extends AbstractRegressionTesterPlugin<Report> {
    private StopRequest stopRequest;
    private StartRequest startRequest;
    private Map<String, Statistic> initialStatistics, finalStatistics;
    private boolean failed = false;
    private String reason = "no reason";
    
    @Cougaar.Arg(name="pingerCount", required=true)
    public int pingerCount;
    
    @Cougaar.Arg(name="payloadSize", defaultValue="0", description="Payload Sizes in Bytes")
    public int payloadSize; 

    @Cougaar.Arg(name="interPingDelay", defaultValue="0", 
    		description="Time between sending next ping after receiving reply (in milliseconds)")
    public long interPingDelay; 

    // TODO JAZ why can't I use StatisticKind.ANOVA.toString()
    @Cougaar.Arg(name="statisticsKind", defaultValue="ANOVA", 
    		description="Kind of statistics to collect (ANOVA, TRACE, or BOTH)")
    public StatisticKind statisticsKind; 
 
    protected void doStartTest(Context context) {
        startRequest = new StartRequest(uids.nextUID(), interPingDelay, payloadSize, statisticsKind.name());
        blackboard.publishAdd(startRequest);
        //defer until all Start requests have returned
    }

    public void doneStartTest(Report report) {
         failed = startRequest.isFailed();
         super.doneStartTest(report);
     }

    protected void doStartSteadyStateCollection(Context context) {
        // query blackboard for all ping queries
        // snapshot the statistics
        // store the statistics for later processing
        initialStatistics = gatherStatistics();
        super.doStartSteadyStateCollection(context);
    }

    protected void doEndSteadyStateCollection(Context context) {
        finalStatistics = gatherStatistics();
        super.doEndSteadyStateCollection(context);
    }

    protected void doEndTest(Context context) {
        blackboard.publishRemove(startRequest);
        stopRequest = new StopRequest(uids.nextUID());
        blackboard.publishAdd(stopRequest);
        //defer until all Stop requests have returned
    }
    
    public void doneEndTest(Report report) {
        failed = stopRequest.isFailed();
        super.doneEndTest(report);
    }

    protected void doSummary(Context context) {
        if (log.isInfoEnabled()) {
            log.info("Do Summary context="+context);
        }
        Report report = new StatisticsReport(workerId, reason,initialStatistics, finalStatistics);
        doneSummary(report);
    }

    protected void doShutdown(Context context) {
        super.doShutdown(context);
    }
    
    protected Map<String, Statistic> gatherStatistics() {
		Map<String, Statistic> statistics = new HashMap<String, Statistic>();
		@SuppressWarnings("unchecked")
		Collection<SimpleRelay> relays = blackboard.query(new IsQueryRelay());
		for (SimpleRelay relay : relays) {
			PingQuery query = (PingQuery) relay.getQuery();
			Statistic statistic = query.getStatistic();
			String sessionName = statistic.getName();
			statistics.put(sessionName, statistic.snapshot());
		}
		return statistics;
	}
    
    protected Report makeReport(RegressionStep step) {
        return new ReportBase(workerId,!failed,reason);
    }
    
    @Cougaar.Execute(on={Subscribe.ModType.ADD, Subscribe.ModType.CHANGE})
    public void executeStartRequest(StartRequest start) {
        if (start.equals(startRequest) && (startRequest.getRunners() == pingerCount)) {
            doneStartTest(makeReport(RegressionStep.START_TEST));
        }
    }
    
    @Cougaar.Execute(on={Subscribe.ModType.ADD, Subscribe.ModType.CHANGE})
    public void executeStopRequest(StopRequest stop) {
        if (stop.equals(stopRequest) && (stopRequest.getRunners() == pingerCount)) {
            doneEndTest(makeReport(RegressionStep.END_TEST));
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
