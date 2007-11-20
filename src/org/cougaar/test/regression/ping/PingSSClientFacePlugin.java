package org.cougaar.test.regression.ping;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.selectserver.ServerSelectionClientFacePlugin;
import org.cougaar.test.coordinations.selectserver.ServerSelection.EventType;

public class PingSSClientFacePlugin extends ServerSelectionClientFacePlugin {

    public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.REQUEST && object instanceof PingQuery) {
            PingQuery query = (PingQuery) object;
            return logicalServerAddress.equals(query.getReceiverAgent());
        }
        return false;
    }
    
    public void remap(UniqueObject object) {
        PingReply reply = (PingReply) object;
        reply.setReceiverAgent(logicalServerAddress);
    }

}
