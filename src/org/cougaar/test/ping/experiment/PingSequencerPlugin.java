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
 * $Revision: 1.5 $
 * $Date: 2008-03-03 22:30:20 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.experiment;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.CSVLog;
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
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
        addStep(START_TEST, steadyStateWaitMillis, null);
        addStep(START_STEADY_STATE, collectionLengthMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(END_TEST, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, PING_RUN_PROPERTY+"="+RUN_NAME);
        addStep(SHUTDOWN, 0, null);
        logExperimentDescription();
    }
    

    private void processStats(Collection<Set<Report>> reportsCollection,Properties props) {
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
                                  "Pingers, Ping/sec, Min, Avg, Max, Run, Test", log);
            csv.field(summary.getValueCount());
            csv.field(summary.getSum());
            csv.field(summary.min());
            csv.field(summary.average());
            csv.field(summary.max());
            csv.field(props.getProperty(PING_RUN_PROPERTY));
            csv.printLn(suiteName);
            csv.close();
        }
    }
    
    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
    
    
}
