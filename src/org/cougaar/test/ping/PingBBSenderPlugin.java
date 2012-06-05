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
 * $Revision: 1.9 $
 * $Date: 2008-04-05 03:55:47 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.Collections;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.qos.stats.Statistic;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingBBSenderPlugin
      extends TodoPlugin {
   @Cougaar.Arg(name = "preambleCount", defaultValue = "10", description = "Number of pings to send before declaring the pinger has started")
   public int preambleCount;

   @Cougaar.Arg(name = "targetAgent", required = true, description = "Receiver Agent")
   public MessageAddress targetAgentId;

   @Cougaar.Arg(name = "targetPlugin", defaultValue = "a", description = "Receiver Plugin")
   public String targetPluginId;

   @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Sender Plugin Id")
   public String pluginId;

   private long lastQueryTime;
   private PingQuery sendQuery;
   private StartRequest startRequest;
   private StopRequest stopRequest;
   private String sessionName;
   private boolean failed = false;
   private long waitTime;
   private int payloadBytes;

   @Override
   public void load() {
      super.load();
      sessionName = "[" + agentId + ":" + pluginId + "]->[" + targetAgentId + ":" + targetPluginId + "]";
      if (targetAgentId.equals(agentId) && targetPluginId.equals(pluginId)) {
         throw new IllegalArgumentException("Target matches self: " + sessionName);
      }

   }

   // These methods can be overridden to send interesting payloads based on the
   // reply payload
   // The default implementation just sends the same array of bytes all the
   // time.
   private byte[] staticPayload;

   protected byte[] initialPayload() {
      // TODO intialize payload to something less compressable
      staticPayload = new byte[payloadBytes];
      for (int i = 0; i < payloadBytes; i++) {
         staticPayload[i] = 64;
      }
      log.shout("Length =" + staticPayload.length + "spec=" + payloadBytes);
      return staticPayload;
   }

   protected byte[] nextPayload(byte[] replyPayload) {
      return staticPayload;
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStartRun(StartRequest request) {
      startRequest = request;
      // Ping count starts negative, when zero the pinger has started
      waitTime = startRequest.getWaitTimeMillis();
      payloadBytes = startRequest.getPayloadBytes();
      Statistic stat = request.makeStatistic(sessionName);
      sendQuery = new PingQuery(uids, -preambleCount, stat, agentId, pluginId, targetAgentId, targetPluginId, initialPayload());
      lastQueryTime = System.nanoTime();
      blackboard.publishAdd(sendQuery);
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStopRun(StopRequest request) {
      stopRequest = request;
   }

   @Cougaar.Execute(on = {
      Subscribe.ModType.ADD,
      Subscribe.ModType.CHANGE
   }, when = "isMyPingReply")
   public void executePingResponse(PingReply reply) {
      // Validate that the counts match
      int replyCount = reply.getCount();
      if (replyCount != sendQuery.getCount()) {
         failed = true;
         log.warn("Counts don't match reply=" + replyCount + " query=" + sendQuery.getCount());
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Count=" + replyCount);
         }
      }
      // Update statistics from incoming ack
      Statistic stat = sendQuery.getStatistic();
      long now = System.nanoTime();
      stat.newValue(now - lastQueryTime);
      // check if pinger has finished starting
      if (sendQuery.getCount() == 0) {
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
         blackboard.publishRemove(sendQuery);
         // get ready for a restart
         stopRequest = null;
         startRequest = null;
         return;
      }
      // Do the next ping
      if (waitTime == 0) {
         sendNextQuery(reply.getPayload());
      } else {
         executeLater(waitTime, new sendNextQueryRunnable(reply.getPayload()));
      }
   }

   private void sendNextQuery(byte[] replyPayload) {
      sendQuery.inc();
      sendQuery.setPayload(nextPayload(replyPayload));
      lastQueryTime = System.nanoTime();
      // Note the change in Query
      
      /*
       * The ChangeReport used to be the sendQuery's count! 
       * 
       * FIXME: find thesubscribers and adjust them accordingly.
       */
      blackboard.publishChange(sendQuery, Collections.singleton(sendQuery));
   }

   private class sendNextQueryRunnable
         implements Runnable {
      byte[] replyPayload;

      public sendNextQueryRunnable(byte[] replyPayload) {
         this.replyPayload = replyPayload;
      }

      public void run() {
         sendNextQuery(replyPayload);
      }
   }

   public boolean isMyPingReply(PingReply reply) {
      return agentId.equals(reply.getSenderAgent()) && pluginId.equals(reply.getSenderPlugin())
            && targetAgentId.equals(reply.getReceiverAgent()) && targetPluginId.equals(reply.getReceiverPlugin());
   }

}
