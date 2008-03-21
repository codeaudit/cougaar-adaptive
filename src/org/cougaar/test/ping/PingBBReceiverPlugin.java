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
 * Workfile: PingReceiverPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-03-21 18:46:21 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Blackboard version of Ping Receiver that does not use Relays This can be used
 * either to ping the blackboard of a single agent or relayed to another agent
 * using a coordination
 */
public class PingBBReceiverPlugin extends AnnotatedSubscriptionsPlugin {
    private Map<String, PingReply> returnRelays = new HashMap<String, PingReply>();

    @Cougaar.Arg(name = "pluginId", defaultValue = "a", description = "Receiver Plugin Id")
    public String pluginId;

    @Cougaar.Execute(on = {Subscribe.ModType.ADD, Subscribe.ModType.CHANGE}, when = "isMyPingQuery")
    public void executeNewQueryRelay(PingQuery query) {
        String senderPluginId = query.getSenderPlugin();
        MessageAddress senderAgentId = query.getSenderAgent();
        byte[] payload =query.getPayload();
        String senderKey = makeSenderKey(senderAgentId, senderPluginId);
        PingReply reply = returnRelays.get(senderKey);
        if (reply == null) {
            // originator is relative to the original query.
            reply = new PingReply(uids,
                                  query.getCount(),
                                  senderAgentId,
                                  senderPluginId,
                                  agentId,
                                  pluginId,
                                  payload);
            returnRelays.put(senderKey, reply);
            blackboard.publishAdd(reply);
        } else {
            int count = query.getCount();
            reply.setCount(count);
            reply.setPayload(payload);
            Collection<?> changes = Collections.singleton(count);
            blackboard.publishChange(reply, changes);            
        }
    }


    public boolean isMyPingQuery(PingQuery query) {
        return pluginId.equals(query.getReceiverPlugin());
    }

    private String makeSenderKey(MessageAddress agent, String pluginId) {
        return agent.getAddress() + ":" + pluginId;
    }
}
