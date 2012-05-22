package org.cougaar.test.ping;

import org.cougaar.core.qos.coordinations.selectserver.ServerSelection.EventType;
import org.cougaar.core.qos.coordinations.selectserver.ServerSelectionClientFacePlugin;
import org.cougaar.core.util.UniqueObject;

public class PingSSClientFacePlugin
      extends ServerSelectionClientFacePlugin {

   public boolean match(EventType type, UniqueObject object) {
      if (type == EventType.REQUEST && object instanceof PingQuery) {
         PingQuery query = (PingQuery) object;
         return logicalServerAddress.equals(query.getReceiverAgent());
      }
      return false;
   }

   @Override
   public void remapResponse(UniqueObject object) {
      PingReply reply = (PingReply) object;
      reply.setReceiverAgent(logicalServerAddress);
   }

}
