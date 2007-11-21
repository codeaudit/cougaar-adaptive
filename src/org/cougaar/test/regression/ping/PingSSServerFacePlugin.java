package org.cougaar.test.regression.ping;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.selectserver.ServerSelectionServerFacePlugin;
import org.cougaar.test.coordinations.selectserver.ServerSelection.EventType;

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
