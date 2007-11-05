/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.regression.ping;

import org.cougaar.core.util.UniqueObject;
import org.cougaar.test.coordinations.sweep.bundled.BundledSweepLeaderFacePlugin;
import org.cougaar.test.coordinations.sweep.bundled.BundledSweep.EventType;

public class BundleQueryFacePlugin extends BundledSweepLeaderFacePlugin {
   public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.REQUEST && object instanceof PingQuery) {
             PingQuery query = (PingQuery) object;
             return query.getReceiverAgent().equals(followerAgent);
        }
        return false;
    }
}
