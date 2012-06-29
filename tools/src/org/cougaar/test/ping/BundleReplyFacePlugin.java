/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.ping;

import org.cougaar.core.qos.coordinations.sweep.bundled.BundledSweep.EventType;
import org.cougaar.core.qos.coordinations.sweep.bundled.BundledSweepFollowerFacePlugin;
import org.cougaar.core.util.UniqueObject;

public class BundleReplyFacePlugin
      extends BundledSweepFollowerFacePlugin {
   public boolean match(EventType type, UniqueObject object) {
      if (type == EventType.RESPONSE && object instanceof PingReply) {
         PingReply reply = (PingReply) object;
         // reply address are relative to the original query
         return reply.getSenderAgent().equals(leaderAgent);
      }
      return false;
   }
}
