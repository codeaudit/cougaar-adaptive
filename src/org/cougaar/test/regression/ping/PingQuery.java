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
* Workfile: PingQuery.java
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression.ping;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class PingQuery  extends UniqueObjectBase {
    private int count;
    private Statistic<?> statistic;
    private MessageAddress originatorAgent;
    private String orginatorPlugin;
    private MessageAddress targetAgent;
    private String targetPlugin;
 
    public PingQuery(UIDService uids,
                     int count,
                     Statistic<?> statistic,
                     MessageAddress originatorAgent,
                     String orginatorPlugin,
                     MessageAddress targetAgent,
                     String targetPlugin) {
        super(uids.nextUID());
        this.count = count;
        this.statistic = statistic;
        this.originatorAgent = originatorAgent;
        this.orginatorPlugin = orginatorPlugin;
        this.targetAgent = targetAgent;
        this.targetPlugin = targetPlugin;
    }
 
    public int getCount() {
        return count;
    }
    
    public void inc() {
        ++count;
    }
    
    public Statistic<?> getStatistic() {
        return statistic;
    }
    
    public void setStatistic(Statistic<?> statistic) {
        this.statistic = statistic;
    }

    public MessageAddress getOriginatorAgent() {
        return originatorAgent;
    }

    public String getOrginatorPlugin() {
        return orginatorPlugin;
    }

    public MessageAddress getTargetAgent() {
        return targetAgent;
    }

    public String getTargetPlugin() {
        return targetPlugin;
    }
}
