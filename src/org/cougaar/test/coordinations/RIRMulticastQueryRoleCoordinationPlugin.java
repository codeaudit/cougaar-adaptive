/* 
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.test.coordinations;

import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Send registrations and completions to sequencer agent and receive requests
 * from sequencer agent. Transfer is accomplished by copying the blackboard
 * events onto the query field of a simple relay.
 * 
 * RIR == receiver initiated registration"
 */
public abstract class RIRMulticastQueryRoleCoordinationPlugin 
        extends TodoPlugin
        implements RIRMulticastBlackboardPredicates {
    
    private final Map<MessageAddress, SimpleRelay> replyRelays =
        new HashMap<MessageAddress, SimpleRelay>();
  
    // Node Registration
    public boolean isRegistrationRelay(SimpleRelay relay) {
        UniqueObject query = (UniqueObject) relay.getQuery();
        return agentId.equals(relay.getTarget()) && isRegistration(query);
    }

    // Forward Node Registration to Blackboard from Relay
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isRegistrationRelay")
    public final void forwardRegistration(SimpleRelay relay) {
        MessageAddress nodeId = relay.getSource();
        blackboard.publishAdd(relay.getQuery());
        UID uid = uids.nextUID();
        SimpleRelaySource agentRelay = new SimpleRelaySource(uid, agentId, nodeId, null);
        replyRelays.put(nodeId, agentRelay);
        blackboard.publishAdd(agentRelay);
    }

    // Node completion
    public boolean isResponseRelay(SimpleRelay relay) {
        UniqueObject query = (UniqueObject) relay.getQuery();
        return agentId.equals(relay.getTarget()) && isResponse(query);
    }

    // Forward completion to blackboard from on relay
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isResponseRelay")
    public final void forwardCompletion(SimpleRelay relay) {
        blackboard.publishAdd(relay.getQuery());
    }
   
    // Node Request
    // Forward from blackboard to relay
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when="isQuery")
    public void forwardRequest(UniqueObject event) {
        for (SimpleRelay replyRelay : replyRelays.values()) {
            replyRelay.setQuery(event);
            blackboard.publishChange(replyRelay);
        }
    }

}
