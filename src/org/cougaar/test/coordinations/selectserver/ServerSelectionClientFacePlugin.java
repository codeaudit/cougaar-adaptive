package org.cougaar.test.coordinations.selectserver;

import java.util.Set;

import org.cougaar.core.blackboard.IncrementalSubscription;
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
abstract public class ServerSelectionClientFacePlugin extends FacePlugin<ServerSelection.Client>
implements ServerSelection.Matcher<Face<ServerSelection.EventType>> {

    private SimpleRelay clientRelay;
    
    @Cougaar.Arg(name="serverName", required=true)
    public MessageAddress serverAddress;

    public ServerSelectionClientFacePlugin() {
        super(new ServerSelection.Client());
    }

    // Relay changes from the other end of the coordination
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isResponse")
    public void executeNewClientRelay(SimpleRelay serverRelay) {
        Envelope env = (Envelope) serverRelay.getQuery();
        env.publish(blackboard);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isResponse")
    public void executeModClientRelay(SimpleRelay serverRelay) {
        // Change to existing connection
        Envelope env = (Envelope) serverRelay.getQuery();
        env.publish(blackboard);
    }

    // Blackboard changes from the local user of this coordination's
    // client face
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isRequest")
    public void executeNewServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.ADD);
        ensureClientRelay(env);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isRequest")
    public void executeChangedServerRelay(UniqueObject object,
                                          IncrementalSubscription sub) {
        Set<?> changeReports = sub.getChangeReports(object);
        Envelope env = new Envelope(object, Envelope.Operation.CHANGE, changeReports);
        ensureClientRelay(env);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.REMOVE, when="isRequest")
    public void executeRemovedServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.REMOVE);
        ensureClientRelay(env);
    }
    
    
    private void ensureClientRelay(Envelope env) {
        if (clientRelay == null) {
            UID uid = uids.nextUID();
            clientRelay = new SimpleRelaySource(uid, agentId, serverAddress, env);
            blackboard.publishAdd(clientRelay);
        } else {
            clientRelay.setQuery(env);
            blackboard.publishChange(clientRelay);
        }
    }
    
    public boolean isResponse(SimpleRelay relay) {
        return agentId.equals(relay.getTarget()) && (relay.getQuery() instanceof Envelope);
    }
    
    public boolean isRequest(UniqueObject event) {
        return match(ServerSelection.EventType.REQUEST, event);
    }
}

