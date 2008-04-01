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
* Created : Aug 15, 2007
* Workfile: SummaryReport.java
* $Revision: 1.3 $
* $Date: 2008-04-01 09:19:52 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cougaar.test.sequencer.ReportBase;

public class SummaryReport extends ReportBase {
    private List<Anova> rawStats;
    
    /**
     * Use this constructor if there was a failure (no stats)
     */
    public SummaryReport(String worker, String reason) {
        super(worker, false, reason);
    }
    
    public SummaryReport(String worker, String reason, 
                         Map<String, Anova> initialStats, 
                         Map<String, Anova> finalStats) {
        super(worker,true,"no reason");
        rawStats = new ArrayList<Anova>(initialStats.size());
        if (initialStats.size() != finalStats.size()) {
            setSuccessful(false);
            return;
        }
        for (Map.Entry<String, Anova> entry : initialStats.entrySet()) {
            Anova initStat = entry.getValue();
            Anova finalStat = finalStats.get(entry.getKey());
            if (finalStat == null) {
                // no match for this address
                setSuccessful(false);
                return;
            } else {
                rawStats.add(finalStat.delta(initStat));
            }
        }
        setSuccessful(true);
    }
    
    public String toString() {
        if (!isSuccessful()) {
            return "Failed Test";
        }
        StringBuffer buf = new StringBuffer();
        buf.append("Statistics");
        for (Anova statistic : rawStats) {
            buf.append(statistic.getSummaryString());
        } 
        return buf.toString();
    }

    public List<Anova> getRawStats() {
        return rawStats;
    }
}
