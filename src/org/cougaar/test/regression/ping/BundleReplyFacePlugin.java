/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.regression.ping;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.sweep.bundled.BundledSweepFollowerFacePlugin;
import org.cougaar.test.coordinations.sweep.bundled.BundledSweep.EventType;

public class BundleReplyFacePlugin extends BundledSweepFollowerFacePlugin {
   public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.RESPONSE && object instanceof PingReply) {
            PingReply reply = (PingReply) object;
             return reply.getTargetAgent().equals(leaderAgent);
        }
        return false;
    }
}
