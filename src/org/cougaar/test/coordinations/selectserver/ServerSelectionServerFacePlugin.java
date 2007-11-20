/* *************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2007 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 * $Id: ServerSelectionServerFacePlugin.java,v 1.2 2007-11-20 21:25:15 rshapiro Exp $
 *
 * ************************************************************************/
package org.cougaar.test.coordinations.selectserver;

import java.util.Collections;
import java.util.Set;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.Face;
import org.cougaar.test.coordinations.FacePlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * Subclasses should implement {@link #match}.
 */
abstract public class ServerSelectionServerFacePlugin extends FacePlugin<ServerSelection.Server>
    implements ServerSelection.Matcher<Face<ServerSelection.EventType>> {

    protected MessageAddress clientAddress;
    private SimpleRelay serverRelay;
   
    public ServerSelectionServerFacePlugin() {
        super(new ServerSelection.Server());
    }
    
    // Relay changes from the other end of the coordination
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isRequest")
    public void executeNewClientRelay(SimpleRelay clientRelay) {
        // New connection
        this.clientAddress = clientRelay.getSource();
        Envelope env = (Envelope) clientRelay.getQuery();
        env.publish(blackboard);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isRequest")
    public void executeModClientRelay(SimpleRelay clientRelay) {
        // Change to existing connection
        Envelope env = (Envelope) clientRelay.getQuery();
        env.publish(blackboard);
    }
    
    // Blackboard changes from the local user of this coordination's
    // server face

    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isResponse")
    public void executeNewServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.ADD);
        ensureServerRelay(env);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isResponse")
    public void executeChangedServerRelay(UniqueObject object) {
        // XXX: Need the subscription to get the changeReports
        Set<Object> changeReports = Collections.emptySet();
        Envelope env = new Envelope(object, Envelope.Operation.CHANGE, changeReports);
        ensureServerRelay(env);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.REMOVE, when="isResponse")
    public void executeRemovedServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.REMOVE);
        ensureServerRelay(env);
    }
    
    private void ensureServerRelay(Envelope env) {
        if (serverRelay == null) {
            UID uid = uids.nextUID();
            serverRelay = new SimpleRelaySource(uid, agentId, clientAddress, env);
            blackboard.publishAdd(serverRelay);
        } else {
            serverRelay.setQuery(env);
            blackboard.publishChange(serverRelay);
        }
    }
    
    public boolean isRequest(SimpleRelay relay) {
        return agentId.equals(relay.getTarget()) && (relay.getQuery() instanceof Envelope);
    }
    
    public boolean isResponse(UniqueObject event) {
        return match(ServerSelection.EventType.RESPONSE, event);
    }
}
