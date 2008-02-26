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
 * $Revision: 1.1 $
 * $Date: 2008-02-26 15:31:56 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.Collection;
import java.util.Collections;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingSenderPlugin extends AnnotatedSubscriptionsPlugin {
    @Cougaar.Arg(name = "preambleCount", defaultValue="10", 
                      description = "Number of pings to send before declaring the pinger has started")
    public int preambleCount;

    @Cougaar.Arg(name = "targetAgent", required = true, description = "Receiver Agent")
    public MessageAddress targetAgent;
    
    @Cougaar.Arg(name = "targetPlugin", defaultValue="a", description = "Receiver Plugin")
    public String targetPlugin;
    
    @Cougaar.Arg(name = "pluginId", defaultValue="a", description = "Sender Plugin Id")
    public String pluginId;
 
 
    
    private long lastQueryTime;
    private SimpleRelay sendRelay;
    private RunRequest startRequest, stopRequest;
    private String sessionName;
    private boolean failed = false;

    public void load() {
        super.load();
        if (targetAgent.equals(agentId)) {
            throw new IllegalArgumentException("Target matches self: " + targetAgent);
        }
        sessionName="[" +agentId+ ":"+pluginId+ "]->[" +targetAgent+ ":"+targetPlugin+ "]";
    }

    @Cougaar.Execute(on=Subscribe.ModType.ADD)
    public void executeStartRun(StartRequest request) {
        startRequest = request;
        UID uid = uids.nextUID();
        // Ping count starts negative, when zero the pinger has started
        Object query = new PingQuery(uids, -preambleCount, new Anova(sessionName),
                                     agentId, pluginId, targetAgent, targetPlugin);
        sendRelay = new SimpleRelaySource(uid, agentId, targetAgent, query);
        lastQueryTime = System.nanoTime();
        blackboard.publishAdd(sendRelay);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD)
    public void executeStopRun(StopRequest request) {
        stopRequest = request;
    }
    
    @Cougaar.Execute(on={Subscribe.ModType.ADD, Subscribe.ModType.CHANGE}, when="isMyPingReply")
    public void executePingResponse(SimpleRelay recvRelay) {
        PingReply reply = (PingReply) recvRelay.getQuery();
        PingQuery query = (PingQuery) sendRelay.getQuery();

        // Validate that the counts match
        if (reply.getCount() != query.getCount()) {
            failed=true;
            log.warn("Counts don't match reply="+ reply.getCount()+
                     " query="+query.getCount());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Count=" + reply.getCount());
            }
        }
        // Update statistics from incoming ack
        Statistic<?> stat = query.getStatistic();
        long now = System.nanoTime();
        stat.newValue(now-lastQueryTime);
        // check if pinger has finished starting
        if (query.getCount() == 0) {
            stat.reset();
            startRequest.inc();
            blackboard.publishChange(startRequest);
        }
        // check if test should stop
        if (stopRequest != null) {
            stopRequest.inc();
            if (failed) stopRequest.forceFailed();
            blackboard.publishChange(stopRequest);
            blackboard.publishRemove(sendRelay);
            return;
        }
        // Do the next ping and note the change
        query.inc();
        lastQueryTime = now;
        Collection<?> changeList = Collections.singleton(query);
        blackboard.publishChange(sendRelay, changeList);
    }
    
    public boolean isMyPingReply(SimpleRelay relay) {
         if (agentId.equals(relay.getTarget()) && 
                 targetAgent.equals(relay.getSource())) {
             if (relay.getQuery() instanceof PingReply) {
                PingReply pingReply = (PingReply) relay.getQuery();
                return pluginId.equals(pingReply.getSenderPlugin()) && 
                        targetPlugin.equals(pingReply.getReceiverPlugin());
             }
         }
         return false;
    }

}
