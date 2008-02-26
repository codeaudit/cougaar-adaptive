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
 * $Date: 2008-02-26 21:10:05 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.experiment;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.CSVLog;
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Ping-specific RegressionSequncer
 */
public class PingSequencerPlugin 
    extends AbstractExperimentSequencerPlugin<Report> 
    implements PingSteps {

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
    public void executeSocietyCompletion(SocietyCompletionEvent<ExperimentStep, Report> event) {
        if (!event.isSuccessful()) {
            sequenceFailed(event);
            return;
        }
        ExperimentStep step = event.getStep();
        if (step == START_TEST) {
            deferSocietyCompletion(event, steadyStateWaitMillis);
        } else if (step == START_STEADY_STATE) {
            deferSocietyCompletion(event, collectionLengthMillis);
        } else if (step == SUMMARY_TEST) {
            processStats(event.getReports().values());
            resumeSocietyCompletion(event);
        } else {
            resumeSocietyCompletion(event);
        }
    }
    
    protected ExperimentStep getNextStep(ExperimentStep step) {
        if (SOCIETY_READY == step) {
            return START_TEST;
        } else if (START_TEST == step) { 
           return START_STEADY_STATE;
        } else if (START_STEADY_STATE == step) {
           return END_STEADY_STATE;    
        } else if (END_STEADY_STATE == step) {
            return END_TEST;
        } else if (END_TEST == step) {
            return SUMMARY_TEST;
        } else if (SUMMARY_TEST == step) {
            return SHUTDOWN;
        }
        return null;
    }

    private void processStats(Collection<Set<Report>> reportsCollection) {
        Anova summary = new Anova("Throughput");

        for (Set<Report> reports : reportsCollection) {
            for (Report report : reports) {
                if (!report.isSuccessful()) {
                    log.error("Summary step was successful, but unsuccessful report" + report);
                }
                log.info(report.toString());
                if (report instanceof SummaryReport) {
                    for (Anova stat : ((SummaryReport) report).getRawStats()) {
                        long itemsPerSecond = Math.round(stat.itemPerSec());
                        summary.newValue(itemsPerSecond);
                    }
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
    
    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
