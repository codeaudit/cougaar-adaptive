/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.coordinations.oneway.OnewayServerFacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay.EventType;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

public class ImageOnewaySenderFacePlugin extends OnewayServerFacePlugin {
   public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.SEND && object instanceof ImageHolder) {
             ImageHolder image = (ImageHolder) object;
             return image.length() != 0;
        }
        return false;
    }
   
   protected MessageAddress makeMessageAddress( Object objectToSend, MessageAddress clientAddress, 
   		int defaultTimeoutMillis, UID uid) { 
	   MessageAddress addr = super.makeMessageAddress(objectToSend, clientAddress, defaultTimeoutMillis, uid);
	   // TODO extract attributes from objectToSend, which is a ImageHolder
	   return addr;
   }
}
