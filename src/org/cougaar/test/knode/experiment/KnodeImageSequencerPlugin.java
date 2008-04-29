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
 * Workfile: PingNodeLocalSequencerPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-04-29 19:01:38 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.knode.experiment;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.qos.stats.Anova;
import org.cougaar.core.qos.stats.CsvWriter;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.test.ping.experiment.PingSteps;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.StatisticsAccumulator;
import org.cougaar.test.sequencer.StatisticsReport;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.util.annotations.Cougaar;

/**
 * KNode test case
 */
public class KnodeImageSequencerPlugin 
    extends AbstractKnodExpSequencerPlugin
    implements PingSteps {
	
	
	@Cougaar.Arg(name="collectionLength", defaultValue="3000",
			description="Milliseconds to run collection")
			public long collectionLengthMillis; 

	@Cougaar.Arg(name="steadyStateWait", defaultValue="3000",
			description="MilliSeconds to wait after test has started," +
	" before starting collection")
	public long steadyStateWaitMillis; 

	@Cougaar.Arg(name="csvFileName", defaultValue="",
			description="File name to append results, default directory is run")
			public String csvFileName;

	private final StepRunnable summaryWork = new StepRunnable() {
		public void run() {
			processStats(getEvent().getReports().values(), getProps());
		}
	}; 
	
    @Cougaar.Arg(name="payloadSize", defaultValue="0", description="Payload Sizes in Bytes")
    public long payloadSize; 

    @Cougaar.Arg(name="interPingDelay", defaultValue="0", 
    		description="Time between sending next ping after receiving reply (in milliseconds)")
    public long interPingDelay; 

    // TODO JAZ why can't I use StatisticKind.ANOVA.toString()
    @Cougaar.Arg(name="statisticsKind", defaultValue="ANOVA", 
    		description="Kind of statistics to collect (ANOVA, TRACE, or BOTH)")
    public StatisticKind statisticsKind; 
    
   
    
	private void addPingSteps(String runName, String hops, String minSlots, String topology) {
		addStep(START_STEADY_STATE, collectionLengthMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, 
        		PING_RUN_PROPERTY+"="+runName,
        		KNODE_HOPS_PROPERTY+"="+hops,
        		KNODE_MIN_SLOTS_PROPERTY+"="+minSlots,
        		KNODE_TOPOLOGY_TYPE_PROPERTY+"="+topology);
 	}
	

    public void load() {
        super.load();
        addRestartKnodeSteps();
        addStep(START_TEST, steadyStateWaitMillis, null);
        addPingSteps("1hop", "1", "50", "Image");
        addMoveLinkSteps("163","164","40.0");
        addPingSteps("2hop", "2", "50", "Image");
        addMoveLinkSteps("164","165","50.0");
        addPingSteps("3hop", "3", "50", "Image");
        addMoveLinkSteps("165","166","60.0");
        addPingSteps("4hop", "4", "50", "Image");
        addMoveLinkSteps("166","167","70.0");
        addPingSteps("5hop", "5", "50", "Image");
        addMoveLinkSteps("167","166","60.0");
        addPingSteps("4hop", "4", "50", "Image");
        addMoveLinkSteps("166","165","50.0");
        addPingSteps("3hop", "3", "50", "Image");
        addMoveLinkSteps("165","164","40.0");
        addPingSteps("2hop", "2", "50", "Image");
        addMoveLinkSteps("164","163","30.0");
        addPingSteps("1hop", "1", "50", "Image");
        addStep(END_TEST, 0, null);
		addStep(SHUTDOWN, 0, null);
    }
    
    private void processStats(Collection<Set<Report>> reportsCollection, Properties props) {
        final Anova thrpSummary = (Anova) StatisticKind.ANOVA.makeStatistic("Throughput");
        final Anova delaySummary = (Anova) StatisticKind.ANOVA.makeStatistic("Delay");
        StatisticsAccumulator acc = new StatisticsAccumulator(log) {
            protected void accumulate(Statistic statistic) {
                double itemPerSec = ((Anova) statistic).itemPerSec();
                thrpSummary.newValue(itemPerSec);
                delaySummary.accumulate(statistic);
            }
        };
        acc.accumulate(reportsCollection);
        
       
        KnodeRunSummaryBean row = new KnodeRunSummaryBean(thrpSummary, delaySummary, props, suiteName);
        log.shout(row.toString());
        CsvWriter.writeRow(row, new KnodeRunSummaryCvsFormat(), csvFileName, log);
    }
    
    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new StatisticsReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
