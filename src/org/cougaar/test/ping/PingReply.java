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
* Workfile: PingReply.java
* $Revision: 1.2 $
* $Date: 2008-03-21 18:46:21 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class PingReply extends UniqueObjectBase {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private int count;
    private MessageAddress senderAgent;
    private String senderPlugin;
    private MessageAddress receiverAgent;
    private String receiverPlugin;
    private byte[] payload;
 

   public PingReply(UIDService uids,
                     int count,
                     MessageAddress originatorAgent,
                     String orginatorPlugin,
                     MessageAddress targetAgent,
                     String targetPlugin,
                     byte[] payload) {
        super(uids.nextUID());
        this.count = count;
        this.senderAgent = originatorAgent;
        this.senderPlugin = orginatorPlugin;
        this.receiverAgent = targetAgent;
        this.receiverPlugin = targetPlugin;
        this.payload=payload;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public MessageAddress getSenderAgent() {
        return senderAgent;
    }

    public String getSenderPlugin() {
        return senderPlugin;
    }

    public MessageAddress getReceiverAgent() {
        return receiverAgent;
    }

    public String getReceiverPlugin() {
        return receiverPlugin;
    }

    public void setReceiverAgent(MessageAddress logicalServerAddress) {
        receiverAgent = logicalServerAddress;
    }


	public byte[] getPayload() {
		return payload;
	}


	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
}
