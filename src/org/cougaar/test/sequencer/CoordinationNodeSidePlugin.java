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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/*
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 */
public class CoordinationNodeSidePlugin<S extends Step, R extends Report, C extends Context>
    extends
        TodoPlugin {

    private SimpleRelay sendRelay;
    @Cougaar.Arg(name = "sequencerName", required = true)
    public MessageAddress sequencerAgent;

    // Send Registration to sequencer on new relay
    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public final void forwardRegistration(NodeRegistrationEvent registration) {
        UID uid = uids.nextUID();
        sendRelay = new SimpleRelaySource(uid, agentId, sequencerAgent, registration);
        blackboard.publishAdd(sendRelay);
    }

    // Send Completion events on send relay to sequencerAgent
    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void forwardCompletion(NodeCompletionEvent<S, R> event) {
        sendRelay.setQuery(event);
        blackboard.publishChange(sendRelay);
    }

    // Forward Requests from receive Relay to Blackboard
    public boolean isRequest(SimpleRelay relay) {
        return sequencerAgent.equals(relay.getSource()) && relay.getQuery() instanceof NodeRequest;
    }

    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isRequest")
    public void forwardRequest(SimpleRelay relay) {
        blackboard.publishAdd(relay.getQuery());
    }
}
