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
 * $Revision: 1.4 $
 * $Date: 2008-03-04 18:04:26 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.knode.experiment;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.CsvWriter;
import org.cougaar.test.ping.PingRunSummaryBean;
import org.cougaar.test.ping.PingRunSummaryCsvFormat;
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.ping.experiment.PingSteps;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.util.annotations.Cougaar;

/**
 * KNode test case
 */
public class KnodeDiffServSequencerPlugin 
    extends AbstractExperimentSequencerPlugin<Report> 
    implements KnodeSteps,PingSteps {
	
	
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
	
	private void addPingSteps(String runName) {
        addStep(START_TEST, steadyStateWaitMillis, null);
        addStep(START_STEADY_STATE, collectionLengthMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(END_TEST, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, PING_RUN_PROPERTY+"="+runName);
 	}
	
	private void addRestartKnodeSteps() {
		String from = "192.168.162.100";
		String to = "192.168.163.100";
		String path = "IpFlow("+from+"," +to+ "):CapacityMax()";
		addStep(KNODE_SET_METRIC, 0, null, METRIC_PATH_PROPERTY +"="+ path);
        addStep(SOCIETY_READY, 0, null);
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 163 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 164 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 165 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 166 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 162");
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"=70.0");
	}
	
	private void addMoveLinkSteps(String from, String to, String desiredCapacity) {
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 " + to);
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 162 " +from);
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"="+desiredCapacity);
 	}

    public void load() {
        super.load();
        addRestartKnodeSteps();
        addPingSteps("5hops");
        addMoveLinkSteps("163","164","60.0");
        addPingSteps("4hops");
        addMoveLinkSteps("164","165","50.0");
        addPingSteps("3hops");
        addMoveLinkSteps("165","166","40.0");
        addPingSteps("2hops");
        addMoveLinkSteps("166","167","30.0");
        addPingSteps("1hop");
        addStep(SHUTDOWN, 0, null);
        logExperimentDescription();
    }
    
    private void processStats(Collection<Set<Report>> reportsCollection, Properties props) {
        Anova summary = new Anova("Throughput");

        for (Set<Report> reports : reportsCollection) {
            for (Report report : reports) {
                if (!report.isSuccessful()) {
                    log.error("Summary step was successful, but unsuccessful report" + report);
                }
                log.info(report.toString());
                if (report instanceof SummaryReport) {
                    for (Anova stat : ((SummaryReport) report).getRawStats()) {
                        double itemsPerSecond = stat.itemPerSec();
                        summary.newValue(itemsPerSecond);
                    }
                }
            }
        }
        PingRunSummaryBean row = new PingRunSummaryBean(summary, props, suiteName);
        log.shout(row.toString());
        if (!csvFileName.equals("")) {
        	try {
				PingRunSummaryCsvFormat csvFormat = new PingRunSummaryCsvFormat();
				CsvWriter<PingRunSummaryBean> writer = 
					new CsvWriter<PingRunSummaryBean>(csvFormat, csvFileName, log);
				writer.writeRow(row);
			} catch (IntrospectionException e) {
				log.error("Error writing a csv row", e);
			}
        }
    }
    
    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
