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
package org.cougaar.test.coordinations.sweep.bundled;

import java.util.HashSet;
import java.util.Set;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.service.identity.PendingRequestException;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.FacePlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public abstract class BundledSweepFollowerFacePlugin 
        extends FacePlugin<BundledSweep.Follower>
        implements BundledSweep.Matcher<BundledSweep.Follower> {
    
    private SimpleRelay replyRelay;
    private SimpleRelay sendRelay;
    private final Set<UniqueObject> pendingAddResponses = new HashSet<UniqueObject>();
    private final Set<UniqueObject> pendingChangeResponses = new HashSet<UniqueObject>();
    private final Set<UniqueObject> pendingRemoveResponses = new HashSet<UniqueObject>();
    private boolean ourTurnToSendRelay=false;
    
    @Cougaar.Arg(name = "leaderAgent", required = true)
    public MessageAddress leaderAgent;
    
    public BundledSweepFollowerFacePlugin() {
        super(new BundledSweep.Follower());
    }
    
    private boolean somethingToSend() {
    	return !pendingAddResponses.isEmpty() || 
    		   !pendingChangeResponses.isEmpty() ||
    		   !pendingRemoveResponses.isEmpty();
    }
  
    /** 
     * At the end of an execute, deal with any pending responses
     * if it's our turn to send.
     */
    public void execute () {
        super.execute();
        if (somethingToSend() && ourTurnToSendRelay) {
            Bundle pending = new Bundle(pendingAddResponses,
                                        pendingRemoveResponses,
                                        pendingChangeResponses);
            pendingAddResponses.clear();
            pendingRemoveResponses.clear();
            pendingChangeResponses.clear();
            sendRelay.setQuery(pending);
            blackboard.publishChange(sendRelay);
            ourTurnToSendRelay=false;
        }
    }

    public boolean isLeaderSetup(SimpleRelay relay) {
        return relay.getSource().equals(leaderAgent) &&
            relay.getQuery() == ConnectionSetup.LEADER;
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isLeaderSetup")
    public final void leaderConnection(SimpleRelay relay) {
       // Leader has found us
        this.replyRelay = relay;
        UID uid = uids.nextUID();
        sendRelay = new SimpleRelaySource(uid, agentId, leaderAgent, ConnectionSetup.FOLLOWER);
        blackboard.publishAdd(sendRelay);
    }
    
    public boolean isReplyRelay (SimpleRelay relay) {
        return relay == replyRelay;
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isReplyRelay")
    public final void leaderChange(SimpleRelay relay) {
        Bundle bundle = (Bundle) relay.getQuery();
        for (UniqueObject object : bundle.getAdds()) {
            blackboard.publishAdd(object);
        }
        for (UniqueObject object : bundle.getChanges()) {
            blackboard.publishChange(object);
        }
        for (UniqueObject object : bundle.getRemoves()) {
            blackboard.publishRemove(object);
        }
        ourTurnToSendRelay=true;
        //TODO  if we have something to send, schedule a bundleAndSend to run at end of execute
        
    }
    
    public boolean isResponse(UniqueObject event) {
        return match(BundledSweep.EventType.RESPONSE, event);
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when="isResponse")
    public void squirrelAddResponse(UniqueObject event) {
        pendingAddResponses.add(event);
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when="isResponse")
    public void squirrelChangeResponse(UniqueObject event) {
        pendingChangeResponses.add(event);
    }
    
    
    @Cougaar.Execute(on = Subscribe.ModType.REMOVE, when="isResponse")
    public void squirrelDeleteResponse(UniqueObject event) {
        pendingRemoveResponses.add(event);
    }



}
