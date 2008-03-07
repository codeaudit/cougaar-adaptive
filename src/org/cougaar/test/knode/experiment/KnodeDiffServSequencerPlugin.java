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
 * $Revision: 1.6 $
 * $Date: 2008-03-07 14:20:48 $
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
	
	private void addPingSteps(String runName,String hops, String minSlots, String topology) {
        addStep(START_TEST, steadyStateWaitMillis, null);
        addStep(START_STEADY_STATE, collectionLengthMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(END_TEST, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, 
        		PING_RUN_PROPERTY+"="+runName,
        		KNODE_HOPS_PROPERTY+"="+hops,
        		KNODE_MIN_SLOTS_PROPERTY+"="+minSlots,
        		KNODE_TOPOLOGY_TYPE_PROPERTY+"="+topology);
 	}
	
	// 5 hop line from 162->167
	private void addRestartKnodeSteps() {
		String from = "192.168.162.100";
		String to = "192.168.167.100";
		String path = "IpFlow("+from+"," +to+ "):CapacityMax()";
		addStep(KNODE_SET_METRIC, 0, null, METRIC_PATH_PROPERTY +"="+ path);
        addStep(SOCIETY_READY, 0, null);
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 163");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 163 164");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 164 165");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 165 166");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 166 167");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 164 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 165 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 166 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 163");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 164");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 165");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 163");
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"=30.0");
	}
	
	private void addMoveLinkSteps(String from, String to, String desiredCapacity) {
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 " + to);
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 162 " +from);
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"="+desiredCapacity);
 	}
	
	private void addDeleteLinkSteps(String from, String to) {
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= "+ from +" "+ to);
   	}

	private void addAddLinkSteps(String from, String to,int wait) {
        addStep(KNODE_ADD_LINK, wait, null, LINK_PROPERTY+"= "+ from +" "+ to);
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
	
	// assumes singele server is on node170
	private void addLineShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addPingSteps("5hops", "5", "33","Line");
		addDeleteLinkSteps("163", "164");
		addMoveLinkSteps("163","164","40.0");
		addPingSteps("4hops", "4", "33","Line");
		addDeleteLinkSteps("164", "165");
		addMoveLinkSteps("164","165","50.0");
		addPingSteps("3hops", "3", "33","Line");
		addDeleteLinkSteps("165", "166");
		addMoveLinkSteps("165","166","60.0");
		addPingSteps("2hops", "2", "33","Line");
		addDeleteLinkSteps("166", "167");
		addMoveLinkSteps("166","167","70.0");
		addPingSteps("1hop", "1", "50","Line");
		addStep(SHUTDOWN, 0, null);
		logExperimentDescription();
	}
	
	private void addStarShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addDeleteLinkSteps("163", "164");
		addDeleteLinkSteps("164", "165");
		addDeleteLinkSteps("165", "166");
		addDeleteLinkSteps("166", "167");
		addMoveLinkSteps("163","167","30.0");		
		addPingSteps("2nodes", "1", "50","Star");
		
		addAddLinkSteps("166", "167",30000);
		addPingSteps("3nodes", "1", "33","Star");
		
		addAddLinkSteps("165", "167",30000);
		addPingSteps("4nodes", "1", "25","Star");
		
		addAddLinkSteps("164", "167",30000);
		addPingSteps("5nodes", "1", "20","Star");
		
		addAddLinkSteps("163", "167",30000);
		addPingSteps("6nodes", "1", "16","Star");
		
		addStep(SHUTDOWN, 0, null);
		logExperimentDescription();
	}

    public void load() {
        super.load();
        addTeeShapedExperimentSteps();
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
                        summary.newValue(stat.itemPerSec());
                    }
                }
            }
        }
        KnodeRunSummaryBean row = new KnodeRunSummaryBean(summary, props, suiteName);
        log.shout(row.toString());
        if (!csvFileName.equals("")) {
        	try {
				KnodeRunSummaryCvsFormat csvFormat = new KnodeRunSummaryCvsFormat();
				CsvWriter<KnodeRunSummaryBean> writer = 
					new CsvWriter<KnodeRunSummaryBean>(csvFormat, csvFileName, log);
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
