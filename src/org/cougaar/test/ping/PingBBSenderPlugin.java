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
 * $Revision: 1.3 $
 * $Date: 2008-03-04 21:40:58 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.Collection;
import java.util.Collections;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingBBSenderPlugin extends TodoPlugin {
    @Cougaar.Arg(name = "preambleCount", defaultValue = "10", description = "Number of pings to send before declaring the pinger has started")
    public int preambleCount;

    @Cougaar.Arg(name = "targetAgent", required = true, description = "Receiver Agent")
    public MessageAddress targetAgentId;

    @Cougaar.Arg(name = "targetPlugin", defaultValue = "a", description = "Receiver Plugin")
    public String targetPluginId;

    @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Sender Plugin Id")
    public String pluginId;
    
    @Cougaar.Arg(name = "waitTime", defaultValue = "0", description = "Time between pings")
    public int waitTime;

    private long lastQueryTime;
    private PingQuery sendQuery;
    private RunRequest startRequest, stopRequest;
    private String sessionName;
    private boolean failed = false;


    public void load() {
        super.load();
        sessionName =
                "[" + agentId + ":" + pluginId + "]->[" + targetAgentId + ":" + targetPluginId
                        + "]";
        if (targetAgentId.equals(agentId) && targetPluginId.equals(pluginId)) {
            throw new IllegalArgumentException("Target matches self: " + sessionName);
        }

    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStartRun(StartRequest request) {
        startRequest = request;
        // Ping count starts negative, when zero the pinger has started
        sendQuery =
                new PingQuery(uids,
                              -preambleCount,
                              new Anova(sessionName),
                              agentId,
                              pluginId,
                              targetAgentId,
                              targetPluginId);
        lastQueryTime = System.nanoTime();
        blackboard.publishAdd(sendQuery);
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeStopRun(StopRequest request) {
        stopRequest = request;
    }

    @Cougaar.Execute(on = {Subscribe.ModType.ADD, Subscribe.ModType.CHANGE}, when = "isMyPingReply")
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
        Statistic<?> stat = sendQuery.getStatistic();
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
            if (failed)
                stopRequest.forceFailed();
            blackboard.publishChange(stopRequest);
            blackboard.publishRemove(sendQuery);
            // get ready for a restart
            stopRequest=null;
            startRequest=null;
            return;
        }
        // Do the next ping
        if (waitTime == 0) {
        	sendNextQuery();
        }	else {
        	Runnable work = new Runnable() {
        		public void run() {
        			sendNextQuery();
        		}
        	};
        	executeLater(waitTime, work);
        }
    }

	private void sendNextQuery() {
		sendQuery.inc();
        lastQueryTime = System.nanoTime();
        // Note the change in Query
        Collection<?> changeList = Collections.singleton(sendQuery.getCount());
        blackboard.publishChange(sendQuery, changeList);
	}

    public boolean isMyPingReply(PingReply reply) {
        return agentId.equals(reply.getSenderAgent())
                && pluginId.equals(reply.getSenderPlugin())
                && targetAgentId.equals(reply.getReceiverAgent())
                && targetPluginId.equals(reply.getReceiverPlugin());
    }

}
