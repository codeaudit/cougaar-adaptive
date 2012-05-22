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
 * $Revision: 1.4 $
 * $Date: 2008-04-02 13:42:58 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class PingQuery
      extends UniqueObjectBase {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private int count;
   private transient Statistic statistic;
   private MessageAddress senderAgent;
   private String senderPlugin;
   private MessageAddress receiverAgent;
   private String receiverPlugin;
   private byte[] payload;

   public PingQuery(UIDService uids, int count, Statistic statistic, MessageAddress senderAgent, String senderPlugin,
                    MessageAddress receiverAgent, String receiverPlugin, byte[] payload) {
      super(uids.nextUID());
      this.count = count;
      this.statistic = statistic;
      this.senderAgent = senderAgent;
      this.senderPlugin = senderPlugin;
      this.receiverAgent = receiverAgent;
      this.receiverPlugin = receiverPlugin;
      this.payload = payload;
   }

   public int getCount() {
      return count;
   }

   public void inc() {
      ++count;
   }

   public Statistic getStatistic() {
      return statistic;
   }

   public void setStatistic(Statistic statistic) {
      this.statistic = statistic;
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

   public void setReceiverAgent(MessageAddress receiverAgent) {
      this.receiverAgent = receiverAgent;
   }

   public byte[] getPayload() {
      return payload;
   }

   public void setPayload(byte[] payload) {
      this.payload = payload;
   }
}
