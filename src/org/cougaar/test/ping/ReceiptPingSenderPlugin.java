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
 * Workfile: PingSenderPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-09-18 18:21:38 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.Collections;

import org.cougaar.core.mts.AttributeConstants;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageAddressWithAttributes;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.SimpleMessageAttributes;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/*
 *  Ping sender that tracks the progress of new query field in a relay.
 *  For example, when the Relay's Directive is has been removed from the Destination Queue, 
 *  it is marked with it Delivery Status. Store and Forward link protocols may also add delivery 
 *  status as custody packets are returned. 
 *  A receipt is returned in the reply field of the relay. The delivery status attribute indicates 
 *  the progress
 */
// TODO refactor to extend PingSender
// TODO Do something with receipt besides log
public class ReceiptPingSenderPlugin
      extends TodoPlugin {
   @Cougaar.Arg(name = "preambleCount", defaultValue = "10", description = "Number of pings to send before declaring the pinger has started")
   public int preambleCount;

   @Cougaar.Arg(name = "targetAgent", required = true, description = "Receiver Agent")
   public MessageAddress targetAgent;

   @Cougaar.Arg(name = "targetPlugin", defaultValue = "a", description = "Receiver Plugin")
   public String targetPlugin;

   @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Sender Plugin Id")
   public String pluginId;

   private long lastQueryTime;
   private SimpleRelay sendRelay;
   private StartRequest startRequest;
   private StopRequest stopRequest;
   private String sessionName;
   private boolean failed = false;
   private int payloadBytes;
   private long waitTime;

   @Override
   public void load() {
      super.load();
      if (targetAgent.equals(agentId)) {
         throw new IllegalArgumentException("Target matches self: " + targetAgent);
      }
      sessionName = "[" + agentId + ":" + pluginId + "]->[" + targetAgent + ":" + targetPlugin + "]";

      // remake the target address so we can add attributes
      MessageAttributes attrs = new SimpleMessageAttributes();
      attrs.setAttribute(AttributeConstants.RECEIPT_REQUESTED, "true");
      // attrs.setAttribute(AttributeConstants.MESSAGE_SEND_TIMEOUT_ATTRIBUTE,
      // 5);
      targetAgent = MessageAddressWithAttributes.getMessageAddressWithAttributes(targetAgent, attrs);
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStartRun(StartRequest request) {
      startRequest = request;
      // Ping count starts negative, when zero the pinger has started
      waitTime = startRequest.getWaitTimeMillis();
      payloadBytes = startRequest.getPayloadBytes();
      byte[] payload = new byte[payloadBytes];
      // TODO initialize array to something that compresses normally
      UID uid = uids.nextUID();
      // Ping count starts negative, when zero the pinger has started
      Object query =
            new PingQuery(uids,
                          -preambleCount,
                          StatisticKind.ANOVA.makeStatistic(sessionName),
                          agentId,
                          pluginId,
                          targetAgent,
                          targetPlugin,
                          payload);
      sendRelay = new SimpleRelaySource(uid, agentId, targetAgent, query);
      lastQueryTime = System.nanoTime();
      blackboard.publishAdd(sendRelay);
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStopRun(StopRequest request) {
      stopRequest = request;
   }

   @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isMyReceipt")
   public void executeMessageReceipt(SimpleRelay receipt) {
      Object rawReply = receipt.getReply();
      if (rawReply instanceof MessageAttributes) {
         MessageAttributes reply = (MessageAttributes) rawReply;
         Object status = reply.getAttribute(AttributeConstants.DELIVERY_ATTRIBUTE);
         log.shout("Receipt for " + receipt.getUID() + " was " + status);
         reply.clearAttributes(); // signal to show we've already processed
                                  // the receipt
      }
   }

   @Cougaar.Execute(on = {
      Subscribe.ModType.ADD,
      Subscribe.ModType.CHANGE
   }, when = "isMyPingReply")
   public void executePingResponse(SimpleRelay recvRelay) {
      PingReply reply = (PingReply) recvRelay.getQuery();
      final PingQuery query = (PingQuery) sendRelay.getQuery();

      // Validate that the counts match
      if (reply.getCount() != query.getCount()) {
         failed = true;
         log.warn("Counts don't match reply=" + reply.getCount() + " query=" + query.getCount());
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Count=" + reply.getCount());
         }
      }
      // Update statistics from incoming ack
      Statistic stat = query.getStatistic();
      long now = System.nanoTime();
      stat.newValue(now - lastQueryTime);
      // check if pinger has finished starting
      if (query.getCount() == 0) {
         stat.reset();
         startRequest.inc();
         blackboard.publishChange(startRequest);
      }
      // check if test should stop
      if (stopRequest != null) {
         stopRequest.inc();
         if (failed) {
            stopRequest.forceFailed();
         }
         blackboard.publishChange(stopRequest);
         blackboard.publishRemove(sendRelay);
         return;
      }
      // Do the next ping
      if (waitTime == 0) {
         sendNextQuery(query);
      } else {
         Runnable work = new Runnable() {
            public void run() {
               sendNextQuery(query);
            }
         };
         executeLater(waitTime, work);
      }
   }

   private void sendNextQuery(PingQuery sendQuery) {
      sendQuery.inc();
      lastQueryTime = System.nanoTime();
      // Note the change in Query
      blackboard.publishChange(sendRelay, Collections.singleton(sendQuery));
   }

   public boolean isMyReceipt(SimpleRelay relay) {
      if (sendRelay.equals(relay)) {
         Object rawReply = relay.getReply();
         if (rawReply instanceof MessageAttributes) {
            MessageAttributes reply = (MessageAttributes) rawReply;
            return !reply.isEmpty();
         }
      }
      return false;
   }

   public boolean isMyPingReply(SimpleRelay relay) {
      if (agentId.equals(relay.getTarget()) && targetAgent.getPrimary().equals(relay.getSource().getPrimary())) {
         if (relay.getQuery() instanceof PingReply) {
            PingReply pingReply = (PingReply) relay.getQuery();
            return pluginId.equals(pingReply.getSenderPlugin()) && targetPlugin.equals(pingReply.getReceiverPlugin());
         }
      }
      return false;
   }

}
