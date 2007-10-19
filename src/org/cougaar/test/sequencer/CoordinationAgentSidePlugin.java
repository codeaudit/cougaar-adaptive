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
 * Created : Aug 13, 2007
 * Workfile: NodeLocalSequencerPlugin.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 */
public class CoordinationAgentSidePlugin<S extends Step, R extends Report, C extends Context>
    extends
        TodoPlugin {

    private final Map<MessageAddress, SimpleRelay> replyRelays =
            new HashMap<MessageAddress, SimpleRelay>();

    // Node Registration
    public boolean isRegistration(SimpleRelay relay) {
        return agentId.equals(relay.getTarget())
                && relay.getQuery() instanceof NodeRegistrationEvent;
    }

    // Forward Node Registration to Blackboard from Relay
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isRegistration")
    public final void forwardRegistration(SimpleRelay relay) {
        MessageAddress nodeId = relay.getSource();
        blackboard.publishAdd(relay.getQuery());
        UID uid = uids.nextUID();
        SimpleRelaySource agentRelay = new SimpleRelaySource(uid, agentId, nodeId, null);
        replyRelays.put(nodeId, agentRelay);
        blackboard.publishAdd(agentRelay);
    }

    // Node completion
    public boolean isCompletion(SimpleRelay relay) {
        return agentId.equals(relay.getTarget()) && relay.getQuery() instanceof NodeCompletionEvent;
    }

    // Forward completion to blackboard from on relay
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isCompletion")
    public final void forwardCompletion(SimpleRelay relay) {
        blackboard.publishAdd(relay.getQuery());
    }

    // Node Request
    // Forward from blackboard to relay
    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void forwardRequest(NodeRequest<S, C> event) {
        for (SimpleRelay replyRelay : replyRelays.values()) {
            replyRelay.setQuery(event);
            blackboard.publishChange(replyRelay);
        }
    }

}
