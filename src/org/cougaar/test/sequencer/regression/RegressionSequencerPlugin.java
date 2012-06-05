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
 * Created : Sep 13, 2007
 * Workfile: RegressionSequencerPlugin.java
 * $Revision: 1.1 $
 * $Date: 2008-02-26 18:23:40 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer.regression;

import java.util.Collections;
import java.util.Set;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.ReportBase;

public class RegressionSequencerPlugin
      extends AbstractRegressionSequencerPlugin<Report> {

   @Override
   protected Set<Report> makeNodeTimoutFailureReport(RegressionStep step, String reason) {
      Report report = new ReportBase(agentId.getAddress(), false, reason);
      return Collections.singleton(report);
   }
}
