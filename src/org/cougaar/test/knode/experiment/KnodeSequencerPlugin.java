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
 * $Date: 2008-02-28 16:16:15 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.knode.experiment;

import java.util.Collections;
import java.util.Set;

import org.cougaar.test.ping.SummaryReport;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;
import org.cougaar.test.sequencer.experiment.ExperimentStep;

/**
 * KNode test case
 */
public class KnodeSequencerPlugin 
    extends AbstractExperimentSequencerPlugin<Report> 
    implements KnodeSteps {

    public void load() {
        super.load();
        addStep(SOCIETY_READY, 0, null);
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 163 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 164 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 165 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 166 162");
        addStep(KNODE_DEL_LINK, 10000, null, LINK_PROPERTY+"= 167 162");
        
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 164 162");
        addStep(KNODE_DEL_LINK, 10000, null, LINK_PROPERTY+"= 163 162");
        
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 165 162");
        addStep(KNODE_DEL_LINK, 10000, null, LINK_PROPERTY+"= 164 162");
        
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 166 162");
        addStep(KNODE_DEL_LINK, 10000, null, LINK_PROPERTY+"= 165 162");
        
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 167 162");
        addStep(KNODE_DEL_LINK, 10000, null, LINK_PROPERTY+"= 166 162");
        
 
        addStep(SHUTDOWN, 0, null);
        logExperimentDescription();
    }

    protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
        Report report = new SummaryReport(agentId.getAddress(), reason);
        return Collections.singleton(report);
    }
}
