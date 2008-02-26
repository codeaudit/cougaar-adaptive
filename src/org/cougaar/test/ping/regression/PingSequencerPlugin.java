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
 * $Date: 2008-02-26 15:31:57 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.regression;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.CSVLog;
import org.cougaar.test.regression.AbstractRegressionSequencerPlugin;
import org.cougaar.test.regression.RegressionStep;
import org.cougaar.test.sequencer.SocietyCompletionEvent;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Ping-specific RegressionSequncer
 */
public class PingSequencerPlugin 
    extends AbstractRegressionSequencerPlugin<SummaryReport> {

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
    public void executeSocietyCompletion(SocietyCompletionEvent<RegressionStep, SummaryReport> event) {
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

    private void processStats(Collection<Set<SummaryReport>> reportsCollection) {
        Anova summary = new Anova("Throughput");

        for (Set<SummaryReport> reports : reportsCollection) {
            for (SummaryReport report : reports) {
                if (!report.isSuccessful()) {
                    log.error("Summary step was successful, but unsuccessful report" + report);
                }
                log.info(report.toString());
                for (Anova stat : report.getRawStats()) {
                    long itemsPerSecond = Math.round(stat.itemPerSec());
                    summary.newValue(itemsPerSecond);
                }
            }
        }
        log.shout("Pingers=" + summary.getValueCount() +
                  " Pings/second=" + summary.getSum()+
                  " Min/Avg/Max=" +summary.min()+ "/" 
                  +summary.average() + "/" + summary.max());
        if (!csvFileName.equals("")) {
            CSVLog csv=new CSVLog(csvFileName,
                                  "Pingers, Ping/sec, Min, Avg, Max, Test", log);
            csv.field(summary.getValueCount());
            csv.field(summary.getSum());
            csv.field(summary.min());
            csv.field(summary.average());
            csv.field(summary.max());
            csv.printLn(suiteName);
            csv.close();
        }
    }
    
    protected Set<SummaryReport> makeNodeTimoutFailureReport(RegressionStep step, String reason) {
        SummaryReport report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
