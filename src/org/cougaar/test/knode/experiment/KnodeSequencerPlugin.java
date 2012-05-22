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
 * $Revision: 1.3 $
 * $Date: 2008-04-02 14:56:23 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.knode.experiment;

import java.util.Collections;
import java.util.Set;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.StatisticsReport;
import org.cougaar.test.sequencer.experiment.ExperimentStep;

/**
 * KNode test case
 */
public class KnodeSequencerPlugin
      extends AbstractKnodExpSequencerPlugin
      implements KnodeSteps {

   @Override
   public void load() {
      super.load();
      addRestartKnodeSteps();
      addMoveLinkSteps("163", "164", "40.0");
      addMoveLinkSteps("164", "165", "50.0");
      addMoveLinkSteps("165", "166", "60.0");
      addMoveLinkSteps("166", "167", "70.0");
      addStep(SHUTDOWN, 0, null);
      logExperimentDescription();
   }

   @Override
   protected Set<Report> makeNodeTimoutFailureReport(ExperimentStep step, String reason) {
      Report report = new StatisticsReport(agentId.getAddress(), reason);
      return Collections.singleton(report);
   }
}
