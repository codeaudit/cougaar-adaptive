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
* Workfile: StartRequest.java
* $Revision: 1.4 $
* $Date: 2008-04-02 13:42:58 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.core.util.UID;


public class StartRequest extends RunRequest {
	/**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final long waitTimeMillis;
	private final int payloadBytes;
	private final StatisticKind statisticKind;

	public StartRequest(UID uid) {
		this(uid, 0, 0, StatisticKind.ANOVA.name());
	}
	
	public StartRequest(UID uid, long delay, int length, String statKind) {
		super(uid);
		this.waitTimeMillis=delay;
		this.payloadBytes=length;
		this.statisticKind = StatisticKind.valueOf(statKind);
	}
	
    public long getWaitTimeMillis() {
		return waitTimeMillis;
	}

	public int getPayloadBytes() {
		return payloadBytes;
	}
	
	public Statistic makeStatistic(String name) {
		return statisticKind.makeStatistic(name);
	}

}
