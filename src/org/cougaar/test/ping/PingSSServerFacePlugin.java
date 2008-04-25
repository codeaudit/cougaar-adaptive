package org.cougaar.test.ping;

import org.cougaar.core.qos.coordinations.selectserver.ServerSelectionServerFacePlugin;
import org.cougaar.core.qos.coordinations.selectserver.ServerSelection.EventType;
import org.cougaar.core.util.UniqueObject;

public class PingSSServerFacePlugin extends ServerSelectionServerFacePlugin {

    public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.RESPONSE && object instanceof PingReply) {
            PingReply reply = (PingReply) object;
            // reply address are relative to the original query
            return reply.getSenderAgent().equals(clientAddress);
        }
        return false;
    }

    public void remapRequest(UniqueObject object) {
        // No remap necessary        
    }

}
