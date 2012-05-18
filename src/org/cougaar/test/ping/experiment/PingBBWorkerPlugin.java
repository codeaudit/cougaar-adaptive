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
* Created : Sep 28, 2007
* Workfile: PingBBTesterPlugin.java
* $Revision: 1.3 $
* $Date: 2008-04-02 13:42:58 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping.experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.test.ping.PingQuery;
import org.cougaar.util.UnaryPredicate;

public class PingBBWorkerPlugin extends PingWorkerPlugin {
    
    @Override
   protected Map<String, Statistic> gatherStatistics() {
		Map<String, Statistic> statistics = new HashMap<String, Statistic>();
		@SuppressWarnings("unchecked")
		Collection<PingQuery> querys = blackboard.query(new IsPingQuery());
		for (PingQuery query : querys) {
			Statistic statistic = query.getStatistic();
			String sessionName = statistic.getName();
			statistics.put(sessionName, statistic.snapshot());
		}
		return statistics;
	}
   
    
    /**
	 * Blackboard query predicate to gather all the relays holding PingQuery
	 * objects.
	 */
    private class IsPingQuery implements UnaryPredicate {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public boolean execute(Object arg) {
            return arg instanceof PingQuery ;
        }
    }
}
