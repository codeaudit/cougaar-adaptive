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
 * $Revision: 1.7 $
 * $Date: 2008-09-06 22:47:12 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.mts.InetMulticastMessageAddress;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.qos.stats.Anova;
import org.cougaar.core.qos.stats.StatisticKind;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class MulticastPingSenderPlugin
      extends TodoPlugin {
   @Cougaar.Arg(name = "preambleCount", defaultValue = "1", description = "Number of pings to send before declaring the pinger has started")
   public int preambleCount;

   @Cougaar.Arg(name = "minInterPingMillis", defaultValue = "1000", description = "Milliseconds between pings")
   public int minIntraPingMillis;

   @Cougaar.Arg(name = "expectedRepliesPerPing", defaultValue = "1", description = "Expected Number of Replies per Ping")
   public int expectedRepliesPerPing;

   @Cougaar.Arg(name = "multicastAddress", required = true, description = "Multicast Address")
   public InetAddress multicastAddress;

   @Cougaar.Arg(name = "multicastPort", required = true, description = "Multicast Port")
   public int multicastPort;

   @Cougaar.Arg(name = "targetPlugin", defaultValue = "a", description = "Receiver Plugin")
   public String targetPlugin;

   @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Sender Plugin Id")
   public String pluginId;

   private SimpleRelay sendRelay;
   private StartRequest startRequest;
   private StopRequest stopRequest;
   private String sessionName;
   private boolean failed = false;
   private int payloadBytes;
   private long waitTime;
   private Alarm sendNextAlarm;
   private long lastQueryTime;
   private Anova responseTimeStats;
   private Anova lateResponseTimeStats;
   private MessageAddress targetMulticastGroup;

   @Override
   public void start() {
      super.start();
      targetMulticastGroup = new InetMulticastMessageAddress(multicastAddress, multicastPort);
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStartRun(StartRequest request) {
      startRequest = request;
      // Ping count starts negative, when zero the pinger has started
      waitTime = Math.max(minIntraPingMillis, startRequest.getWaitTimeMillis());
      payloadBytes = startRequest.getPayloadBytes();
      byte[] payload = new byte[payloadBytes];
      // TODO initialize array to something that compresses normally
      UID uid = uids.nextUID();
      // Ping count starts negative, when zero the pinger has started
      PingQuery query =
            new PingQuery(uids,
                          -preambleCount,
                          StatisticKind.ANOVA.makeStatistic(sessionName),
                          agentId,
                          pluginId,
                          targetMulticastGroup,
                          targetPlugin,
                          payload);
      sendRelay = new SimpleRelaySource(uid, agentId, targetMulticastGroup, query);
      initializeStatisticsForNewQuery(-preambleCount);
      blackboard.publishAdd(sendRelay);
      sendNextAlarm = executeLater(waitTime, new sendNextQueryRunnable(query));
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeStopRun(StopRequest request) {
      stopRequest = request;
   }

   @Cougaar.Execute(on = {
      Subscribe.ModType.ADD,
      Subscribe.ModType.CHANGE
   }, when = "isMyPingReply")
   public void executePingResponse(SimpleRelay recvRelay) {
      if (log.isDebugEnabled()) {
         log.debug("Received response from " + recvRelay.getSource());
      }
      PingReply reply = (PingReply) recvRelay.getQuery();
      final PingQuery query = (PingQuery) sendRelay.getQuery();
      final long now = System.nanoTime();
      final long responseTime = now - lastQueryTime;
      final int distanceFromCurrent = reply.getCount() - query.getCount();

      // Validate that the counts match
      if (distanceFromCurrent > 0) {
         // Something is wrong
         failed = true;
         log.warn("Reply for future query=" + reply.getCount() + " query=" + query.getCount());
      } else if (distanceFromCurrent < 0) {
         // Update statistic for late reply
         lateResponseTimeStats.newValue((waitTime * distanceFromCurrent) + responseTime);
      } else {
         // Update statistics from incoming reply
         responseTimeStats.newValue(responseTime);
      }
   }

   private class sendNextQueryRunnable
         implements Runnable {
      PingQuery lastQuery;

      public sendNextQueryRunnable(PingQuery lastQuery) {
         this.lastQuery = lastQuery;
      }

      public void run() {
         // calculate results from last query
         int replyCount = responseTimeStats.getValueCount();
         int percentReplies = (100 * replyCount) / expectedRepliesPerPing;
         int lateCount = lateResponseTimeStats.getValueCount();
         int percentLate = (100 * lateCount) / expectedRepliesPerPing;
         double responseTime = responseTimeStats.max(); // worst case

         // remember results
         lastQuery.getStatistic().newValue(responseTime);
         if (log.isInfoEnabled()) {
            log.info(responseTimeStats.getSummaryString() + " replyCount=" + replyCount + " (" + percentReplies + "%)"
                  + " lateCount=" + lateCount + " (" + percentLate + "%)");
         }

         // check if pinger has finished starting
         if (lastQuery.getCount() == 0) {
            lastQuery.getStatistic().reset();
            startRequest.inc();
            blackboard.publishChange(startRequest);
         }

         // check if test should stop
         if (stopRequest != null) {
            stopRequest.inc();
            if (failed) {
               stopRequest.forceFailed();
            }
            sendNextAlarm.cancel();
            blackboard.publishChange(stopRequest);
            blackboard.publishRemove(sendRelay);
            return;
         }

         // Setup next Query
         lastQuery.inc();
         initializeStatisticsForNewQuery(lastQuery.getCount());

         // publish Query
         Collection<?> changeList = Collections.singleton(lastQuery);
         blackboard.publishChange(sendRelay, changeList);

         // Schedule Query after Next
         sendNextAlarm = executeLater(waitTime, new sendNextQueryRunnable(lastQuery));
      }

   }

   private void initializeStatisticsForNewQuery(int count) {
      // TODO statistics should really be stored in query
      responseTimeStats = (Anova) StatisticKind.ANOVA.makeStatistic("responseTime:" + count);
      lateResponseTimeStats = (Anova) StatisticKind.ANOVA.makeStatistic("lateTime:" + count);
      lastQueryTime = System.nanoTime();
   }

   public boolean isMyPingReply(SimpleRelay relay) {
      if (agentId.equals(relay.getTarget())) {
         if (relay.getQuery() instanceof PingReply) {
            PingReply pingReply = (PingReply) relay.getQuery();
            return pluginId.equals(pingReply.getSenderPlugin()) && targetPlugin.equals(pingReply.getReceiverPlugin());
         }
      }
      return false;
   }

}
