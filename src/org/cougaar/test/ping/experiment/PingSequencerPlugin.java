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
 * $Revision: 1.12 $
 * $Date: 2008-08-08 16:17:15 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.experiment;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.qos.stats.Anova;
import org.cougaar.core.qos.stats.CsvWriter;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.test.ping.PingRunSummaryBean;
import org.cougaar.test.ping.PingRunSummaryCsvFormat;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.StatisticsAccumulator;
import org.cougaar.test.sequencer.StatisticsReport;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.LoopDescriptor;
import org.cougaar.util.annotations.Cougaar;

/**
 * Ping-specific RegressionSequncer
 */
public class PingSequencerPlugin 
    extends AbstractExperimentSequencerPlugin<Report> 
    implements PingSteps {
	private static final String RUN_NAME="run1";

    @Cougaar.Arg(name="collectionLength", defaultValue="3000",
                      description="Milliseconds to run collection")
    public long collectionLengthMillis; 
    
    @Cougaar.Arg(name="steadyStateWait", defaultValue="3000",
                      description="MilliSeconds to wait after test has started," +
                      		" before starting collection")
    public long steadyStateWaitMillis; 
    
    @Cougaar.Arg(name="payloadSize", defaultValue="0", description="Payload Sizes in Bytes")
    public long payloadSize; 

    @Cougaar.Arg(name="interPingDelay", defaultValue="0", 
    		description="Time between sending next ping after receiving reply (in milliseconds)")
    public long interPingDelay; 
    
    @Cougaar.Arg(name="loops", defaultValue="3", 
                 description="Number of Steady State Collection Loops")
         public int loops; 


    // TODO JAZ why can't I use StatisticKind.ANOVA.toString()
    @Cougaar.Arg(name="statisticsKind", defaultValue="ANOVA", 
    		description="Kind of statistics to collect (ANOVA, TRACE, or BOTH)")
    public StatisticKind statisticsKind; 

    @Cougaar.Arg(name="csvFileName", defaultValue="",
                      description="File name to append results, default directory is run")
    public String csvFileName;

    private final StepRunnable summaryWork = new StepRunnable() {
        public void run() {
            processStats(getEvent().getReports().values(),getProps());
        }
    }; 

    public void load() {
        super.load();
        addStep(SOCIETY_READY, 0, null);
        addStep(START_TEST, steadyStateWaitMillis, null,
        		PING_SIZE_PROPERTY+"="+payloadSize,
        		PING_DELAY_PROPERTY+"="+ interPingDelay,
        		PING_STATISTICS_PROPERTY+"="+statisticsKind );
        LoopDescriptor<ExperimentStep, Report> loop = makeLoopDescriptor(loops);
        loop.addStep(START_STEADY_STATE, collectionLengthMillis, null);
        loop.addStep(END_STEADY_STATE, 0, null);
        loop.addStep(SUMMARY_TEST, 0, summaryWork, PING_RUN_PROPERTY+"="+RUN_NAME);
        addLoop(loop);
        addStep(END_TEST, 0, null);
        addStep(SHUTDOWN, 0, null);
        logExperimentDescription();
    }
    

    private void processStats(Collection<Set<Report>> reportsCollection,Properties props) {
        final Anova summary = (Anova) StatisticKind.ANOVA.makeStatistic("Throughput");
        StatisticsAccumulator acc = new StatisticsAccumulator(log) {
            protected void accumulate(Statistic statistic) {
                Anova anova = (Anova) statistic;
                summary.newValue(anova.itemPerSec());
            }
        };
        acc.accumulate(reportsCollection);
        
        PingRunSummaryBean row = new PingRunSummaryBean(summary, null, suiteName);
        log.shout(row.toString());
        CsvWriter.writeRow(row, new PingRunSummaryCsvFormat(), csvFileName, log);
        
    }
    
    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new StatisticsReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
    
    
}
