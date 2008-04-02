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
 * $Revision: 1.8 $
 * $Date: 2008-04-02 14:56:24 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.regression;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.cougaar.core.qos.stats.Anova;
import org.cougaar.core.qos.stats.CsvFormat;
import org.cougaar.core.qos.stats.CsvWriter;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.test.ping.PingRunSummaryBean;
import org.cougaar.test.ping.PingRunSummaryCsvFormat;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;
import org.cougaar.test.sequencer.StatisticsAccumulator;
import org.cougaar.test.sequencer.StatisticsReport;
import org.cougaar.test.sequencer.regression.AbstractRegressionSequencerPlugin;
import org.cougaar.test.sequencer.regression.RegressionStep;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Ping-specific RegressionSequncer
 */
public class PingSequencerPlugin 
    extends AbstractRegressionSequencerPlugin<Report> {

    @Cougaar.Arg(name="collectionLength", defaultValue="3000",
                      description="Milliseconds to run collection")
    public long collectionLengthMillis; 
    
    @Cougaar.Arg(name="steadyStateWait", defaultValue="3000",
                      description="MilliSeconds to wait after test has started, before starting collection")
    public long steadyStateWaitMillis; 
    
    @Cougaar.Arg(name="csvFileName", defaultValue="",
                      description="File name to append results, default directory is run")
    public String csvFileName; 


    // Override to defer some steps
    @Cougaar.Execute(on=Subscribe.ModType.ADD)
    public void executeSocietyCompletion(SocietyCompletionEvent<RegressionStep, Report> event) {
        if (!event.isSuccessful()) {
            sequenceFailed(event);
            return;
        }
        switch (event.getStep()) {
            case START_TEST:
                deferSocietyCompletion(event, steadyStateWaitMillis);
                break;
                
            case START_STEADY_STATE_COLLECTION:
                deferSocietyCompletion(event, collectionLengthMillis);
                break;
                
            case SUMMARY:
                processStats(event.getReports().values());
                resumeSocietyCompletion(event);
                break;
                               
            default:
                resumeSocietyCompletion(event);
                break;
        }
    }

   
    
    private void processStats(Collection<Set<Report>> reportsCollection) {
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
    
    protected Set<Report> makeNodeTimoutFailureReport(RegressionStep step, String reason) {
        Report report = new StatisticsReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
