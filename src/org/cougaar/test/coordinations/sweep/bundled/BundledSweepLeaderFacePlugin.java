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
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.FacePlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public abstract class BundledSweepLeaderFacePlugin 
        extends FacePlugin<BundledSweep.Leader>
        implements BundledSweep.Matcher<BundledSweep.Leader> {
    
    private SimpleRelay replyRelay;
    private SimpleRelay sendRelay;
    private final Set<UniqueObject> pendingRequests = new HashSet<UniqueObject>();
    
    @Cougaar.Arg(name = "followerAgent", required = true)
    public MessageAddress followerAgent;
    
    public BundledSweepLeaderFacePlugin() {
        super(new BundledSweep.Leader());
    }
  
    /**
     * Must be called in the execute thread
     */
    private void bundleAndSend() {
        Bundle pending = new Bundle(pendingRequests);
        pendingRequests.clear();
        sendRelay.setQuery(pending);
        blackboard.publishChange(sendRelay);
    }
    
    protected void setupSubscriptions() {
        super.setupSubscriptions();
        UID uid = uids.nextUID();
        sendRelay = new SimpleRelaySource(uid, agentId, followerAgent, ConnectionSetup.LEADER);
        blackboard.publishAdd(sendRelay);
    }
    
    public boolean isFollowerSetup(SimpleRelay relay) {
        return relay.getSource().equals(followerAgent) &&
            relay.getQuery() == ConnectionSetup.FOLLOWER;
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isFollowerSetup")
    public final void followerConnection(SimpleRelay relay) {
        // Follower has found us
        this.replyRelay = relay;
        // send all the currently pending data
        bundleAndSend();
    }
    
    public boolean isReplyRelay (SimpleRelay relay) {
        return relay == replyRelay;
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isReplyRelay")
    public final void followerChange(SimpleRelay relay) {
        Bundle bundle = (Bundle) relay.getQuery();
        for (UniqueObject object : bundle) {
            blackboard.publishAdd(object);
        }
        bundleAndSend();
    }
    
    public boolean isRequest(UniqueObject event) {
        return match(BundledSweep.EventType.REQUEST, event);
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when="isRequest")
    public void squirrelRequest(UniqueObject event) {
        pendingRequests.add(event);
    }

}
