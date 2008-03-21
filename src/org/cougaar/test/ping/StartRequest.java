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
* $Revision: 1.2 $
* $Date: 2008-03-21 18:46:21 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import org.cougaar.core.util.UID;


public class StartRequest extends RunRequest {
	private long waitTimeMillis;
	private int payloadBytes;

    public long getWaitTimeMillis() {
		return waitTimeMillis;
	}

	public int getPayloadBytes() {
		return payloadBytes;
	}

	public StartRequest(UID uid) {
		this(uid, 0, 0);
    }
	
	public StartRequest(UID uid, long delay, int length) {
		super(uid);
		this.waitTimeMillis=delay;
		this.payloadBytes=length;
	}
}
