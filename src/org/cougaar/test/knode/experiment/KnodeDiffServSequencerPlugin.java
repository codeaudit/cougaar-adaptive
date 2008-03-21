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
 * $Revision: 1.14 $
 * $Date: 2008-03-21 22:02:54 $
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
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.ping.experiment.PingSteps;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.util.annotations.Cougaar;

/**
 * KNode test case
 */
public class KnodeDiffServSequencerPlugin 
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
	
	private void addPingSteps(String runName,String hops, String minSlots, String topology) {
		long collectionTimeMillis= false? collectionLengthMillis : 180000;
        addStep(START_TEST, steadyStateWaitMillis, null,
           		PING_SIZE_PROPERTY+"="+"0",
        		PING_DELAY_PROPERTY+"="+"0",
        		PING_STATISTIC_PROPERTY+"="+"TRACE");
        addStep(START_STEADY_STATE, collectionTimeMillis, null);
        addStep(END_STEADY_STATE, 0, null);
        addStep(END_TEST, 0, null);
        addStep(SUMMARY_TEST, 0, summaryWork, 
        		PING_RUN_PROPERTY+"="+runName,
        		PING_SIZE_PROPERTY+"="+"0",
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
	

	private void addLineShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addRunLineExpSteps("33","50","line");
		logExperimentDescription();
	}
	
	private void add1HairyLineShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addAddLinkSteps("163","140");
        addAddLinkSteps("164","143");
        addAddLinkSteps("165","146");
        addAddLinkSteps("166","149");
        addAddLinkSteps("167","152");
		addRunLineExpSteps("25","33","Hairy1Line");
		logExperimentDescription();
	}
	
	private void add2HairyLineShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addAddLinkSteps("163","140");
        addAddLinkSteps("164","143");
        addAddLinkSteps("165","146");
        addAddLinkSteps("166","149");
        addAddLinkSteps("167","152");
        addAddLinkSteps("163","141");
        addAddLinkSteps("164","144");
        addAddLinkSteps("165","147");
        addAddLinkSteps("166","150");
        addAddLinkSteps("167","153");
		addRunLineExpSteps("20","25","Hairy2Line");
		logExperimentDescription();
	}
	
	private void add3HairyLineShapedExperimentSteps() {	
		addRestartKnodeSteps();
		addAddLinkSteps("163","140");
        addAddLinkSteps("164","143");
        addAddLinkSteps("165","146");
        addAddLinkSteps("166","149");
        addAddLinkSteps("167","152");
        addAddLinkSteps("163","141");
        addAddLinkSteps("164","144");
        addAddLinkSteps("165","147");
        addAddLinkSteps("166","150");
        addAddLinkSteps("167","153");
        addAddLinkSteps("163","142");
        addAddLinkSteps("164","145");
        addAddLinkSteps("165","148");
        addAddLinkSteps("166","151");
        addAddLinkSteps("167","154");
		addRunLineExpSteps("16","20","Hairy3Line");
		logExperimentDescription();
	}


	// assumes single server is on host app170, node170 off router 169
	private void addRunLineExpSteps(String slots, String oneHopSlots, String topologyName) {
		addPingSteps("5hops", "5", slots,topologyName);
		addDeleteLinkSteps("163", "164");
		addMoveLinkSteps("163","164","40.0");
		addPingSteps("4hops", "4", slots,topologyName);
		addDeleteLinkSteps("164", "165");
		addMoveLinkSteps("164","165","50.0");
		addPingSteps("3hops", "3", slots,topologyName);
		addDeleteLinkSteps("165", "166");
		addMoveLinkSteps("165","166","60.0");
		addPingSteps("2hops", "2", slots,topologyName);
		addDeleteLinkSteps("166", "167");
		addMoveLinkSteps("166","167","70.0");
		addPingSteps("1hop", "1", oneHopSlots,topologyName);
		addStep(SHUTDOWN, 0, null);
	}
	
	
	private void addStarShapedExperimentSteps() {	
//		addRestartKnodeSteps();
//		addDeleteLinkSteps("163", "164");
//		addDeleteLinkSteps("164", "165");
//		addDeleteLinkSteps("165", "166");
//		addDeleteLinkSteps("166", "167");
//		addMoveLinkSteps("163","167","30.0");		
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
        //add1HairyLineShapedExperimentSteps();
        //add2HairyLineShapedExperimentSteps();
        //add3HairyLineShapedExperimentSteps();
        //addTeeShapedExperimentSteps();
        //addLineShapedExperimentSteps();
        addStarShapedExperimentSteps();
    }
    
    private void processStats(Collection<Set<Report>> reportsCollection, Properties props) {
        Anova thrpSummary = new Anova("Throughput");
        Anova delaySummary = new Anova("Delay");

        for (Set<Report> reports : reportsCollection) {
            for (Report report : reports) {
                if (!report.isSuccessful()) {
                    log.error("Summary step was successful, but unsuccessful report" + report);
                }
                log.info(report.toString());
                if (report instanceof SummaryReport) {
                    for (Anova stat : ((SummaryReport) report).getRawStats()) {
                        thrpSummary.newValue(stat.itemPerSec());
                        delaySummary.addTable(stat);
                    }
                }
            }
        }
        KnodeRunSummaryBean row = new KnodeRunSummaryBean(thrpSummary, delaySummary, props, suiteName);
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
