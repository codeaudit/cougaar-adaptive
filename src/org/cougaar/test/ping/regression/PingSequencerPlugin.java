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
 * $Date: 2008-03-04 21:38:17 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping.regression;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.CsvWriter;
import org.cougaar.test.ping.PingRunSummaryBean;
import org.cougaar.test.ping.PingRunSummaryCsvFormat;
import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;
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
        PingRunSummaryBean row = new PingRunSummaryBean(summary, null, suiteName);
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
    
    protected Set<Report> makeNodeTimoutFailureReport(RegressionStep step, String reason) {
        Report report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
