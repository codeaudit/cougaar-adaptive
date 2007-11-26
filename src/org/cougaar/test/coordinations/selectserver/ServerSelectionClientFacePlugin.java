package org.cougaar.test.coordinations.selectserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.agent.service.alarm.Alarm;
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

    private Map<MessageAddress,SimpleRelay> clientRelays = 
        new HashMap<MessageAddress,SimpleRelay>();
    
    @Cougaar.Arg(name="serverName", required=true)
    public List<MessageAddress> serverAddresses;
    
    @Cougaar.Arg(name="logicalServerName", required=true)
    public MessageAddress logicalServerAddress;
    
    @Cougaar.Arg(name="selectionPolicy", defaultValue="ROUND_ROBIN")
    public SelectionPolicyFactory selectionPolicyFactory;
    
    private SelectionPolicy selectionPolicy;

    @SuppressWarnings("unused") // will be used later
    private Alarm retryTimer;

    public ServerSelectionClientFacePlugin() {
        super(new ServerSelection.Client());
    }
    
    /**
     * Hook for changing the Reply before before sending it
     * to the client. This is usually used to change the Server address 
     * from a logical to physical address
     */
     abstract public void remapResponse(UniqueObject object);
     
     protected void setupSubscriptions() {
         super.setupSubscriptions();
         selectionPolicy = selectionPolicyFactory.makePolicy();
         selectionPolicy.setup(getServiceBroker(), log, serverAddresses);
     }    
     
     // Relay changes from the other end of the coordination
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isResponse")
    public void executeNewClientRelay(SimpleRelay serverRelay) {
        Envelope env = (Envelope) serverRelay.getQuery();
        remapResponse(env.getContents());
        env.publish(blackboard);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isResponse")
    public void executeModClientRelay(SimpleRelay serverRelay) {
        // Change to existing connection
        Envelope env = (Envelope) serverRelay.getQuery();
        remapResponse(env.getContents());
        env.publish(blackboard);
    }

    // Blackboard changes from the local user of this coordination's
    // client face
    
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isRequest")
    public void executeNewServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.ADD);
        forwardRequest(env, 1);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.CHANGE, when="isRequest")
    public void executeChangedServerRelay(UniqueObject object,
                                          IncrementalSubscription sub) {
        Set<?> changeReports = sub.getChangeReports(object);
        Envelope env = new Envelope(object, Envelope.Operation.CHANGE, changeReports);
        forwardRequest(env, 1);
    }
    
    @Cougaar.Execute(on=Subscribe.ModType.REMOVE, when="isRequest")
    public void executeRemovedServerRelay(UniqueObject object) {
        Envelope env = new Envelope(object, Envelope.Operation.REMOVE);
        forwardRequest(env, 1);
    }
    
    private void forwardRequest(Envelope env, int retryCount){
        if (selectionPolicy == null) {
            log.error("Attempted to forward Request with No Selection Policy=" + 
                      selectionPolicyFactory.name());
            return;
        } 
        MessageAddress serverPhysicalAddress = selectionPolicy.select(serverAddresses);
        if (serverPhysicalAddress== null) {
            if (log.isInfoEnabled()) 
                log.info("No valid Physical Server. Retry Later");
            retryTimer = executeLater(5000, new RetryForward(env, retryCount));
            return;
        }

        sendRequest(serverPhysicalAddress,env);
    }
        
    private void sendRequest(MessageAddress server, Envelope env) {
        SimpleRelay clientRelay = clientRelays.get(server);
        if (clientRelay == null) {
            UID uid = uids.nextUID();
            clientRelay = new SimpleRelaySource(uid, agentId, server, null);
            clientRelays.put(server,clientRelay);
            // TODO relay connection should not have envelope?
            clientRelay.setQuery(env);
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
    
    private final class RetryForward implements Runnable {
        private Envelope env;
        private int retryCount;
        
        public RetryForward(Envelope env, int retryCount) {
            this.env=env;
            this.retryCount=retryCount;
        }

        public void run() {
            retryTimer = null;
            forwardRequest(env,retryCount+1);
        }
    }
  
}

