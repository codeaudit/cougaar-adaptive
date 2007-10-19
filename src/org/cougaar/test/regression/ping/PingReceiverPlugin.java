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
* $Revision: 1.1 $
* $Date: 2007-10-19 15:01:52 $
* $Author: rshapiro $
*
* =============================================================================
*/
 
package org.cougaar.test.regression.ping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class PingReceiverPlugin extends AnnotatedSubscriptionsPlugin {
    private Map<MessageAddress,SimpleRelay> returnRelays 
        = new HashMap<MessageAddress,SimpleRelay>();
    
    @Cougaar.Arg(name = "pluginId", defaultValue="a", description = "Receiver Plugin Id")
    public String pluginId;
 
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isMyPingQuery")
    public void executeNewQueryRelay(SimpleRelay senderRelay) {
        MessageAddress sender = senderRelay.getSource();
        SimpleRelay receiverRelay = returnRelays.get(sender);
        if (receiverRelay == null) {
            PingQuery query = (PingQuery) senderRelay.getQuery();
            PingReply reply = 
                new PingReply(uids,query.getCount(),
                              query.getOriginatorAgent(), query.getOrginatorPlugin(), 
                              agentId,pluginId);
            UID uid = uids.nextUID();
            receiverRelay = new SimpleRelaySource(uid, agentId, sender, reply);
            returnRelays.put(sender, receiverRelay);
            blackboard.publishAdd(receiverRelay);   
        } else {
            log.error("Multiple request from same sender=" +sender);
        }
    }

    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isMyPingQuery")
    public void executeQuery(SimpleRelay senderRelay) {
        MessageAddress sender = senderRelay.getSource();
        SimpleRelay receiverRelay = returnRelays.get(sender);
        if (receiverRelay != null) {
            PingQuery query = (PingQuery) senderRelay.getQuery();
            PingReply reply = new PingReply(uids,query.getCount(),
                                            query.getOriginatorAgent(), query.getOrginatorPlugin(), 
                                            agentId,pluginId);
            receiverRelay.setQuery(reply);
            Collection<?> changes = Collections.singleton(reply);
            blackboard.publishChange(receiverRelay, changes);
        } else {
            log.error("No returnRelay for query from sender=" +sender);
        }
    }
    
    public boolean isMyPingQuery(SimpleRelay relay) {
        if (agentId.equals(relay.getTarget())) {
            if (relay.getQuery() instanceof PingQuery) {
               PingQuery pingQuery = (PingQuery) relay.getQuery();
               return pluginId.equals(pingQuery.getTargetPlugin());
            }
        }
        return false;
     }
}
