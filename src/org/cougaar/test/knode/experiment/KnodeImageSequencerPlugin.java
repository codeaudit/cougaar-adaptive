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
 * $Revision: 1.1 $
 * $Date: 2008-04-29 17:41:10 $
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
    
    
    private void addPingSteps(String runName, String hops, String minSlots, String topology)
    {
    	addPingSteps(payloadSize, interPingDelay, statisticsKind, runName, hops, minSlots, topology);
    }
	
	private void addPingSteps(long size, long delay, StatisticKind statKind,
			String runName, String hops, String minSlots, String topology) {
		long collectionTimeMillis= true? collectionLengthMillis : 180000;
        addStep(START_TEST, steadyStateWaitMillis, null,
        		PING_SIZE_PROPERTY+"="+size,
        		PING_DELAY_PROPERTY+"="+ delay,
        		PING_STATISTICS_PROPERTY+"="+statKind );
        addStep(START_STEADY_STATE, collectionTimeMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(END_TEST, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, 
        		PING_RUN_PROPERTY+"="+runName,
        		PING_SIZE_PROPERTY+"="+size,
        		KNODE_HOPS_PROPERTY+"="+hops,
        		KNODE_MIN_SLOTS_PROPERTY+"="+minSlots,
        		KNODE_TOPOLOGY_TYPE_PROPERTY+"="+topology);
 	}
	
	private void addTeeShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addPingSteps("5hops", "5", "33","Tee");
		addMoveLinkSteps("163","164","40.0");
		addPingSteps("4hops", "4", "25","Tee");
		addMoveLinkSteps("164","165","50.0");
		addPingSteps("3hops", "3", "25","Tee");
		addMoveLinkSteps("165","166","60.0");
		addPingSteps("2hops", "2", "25","Tee");
		addMoveLinkSteps("166","167","70.0");
		addPingSteps("1hop", "1", "33","Tee");
		addStep(SHUTDOWN, 0, null);
		logExperimentDescription();
	}
	

    public void load() {
        super.load();
        suiteName="Length";
        addTeeShapedExperimentSteps();
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
