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
import org.cougaar.util.annotations.Cougaar;



public class ClipOnewaySenderFacePlugin extends OnewayServerFacePlugin {
    static final String ANY_CLIP = "AnyClip";
    
    @Cougaar.Arg(name = "clipName", defaultValue = ANY_CLIP, 
                 description = "Name of the Clip. If not set to " + ANY_CLIP +
                 " then all clips will be sent")
     public String clipName;

    // TODO extract images from clip and send
    // TODO set loop to null or transient

    public boolean match(EventType type, UniqueObject object) {
        if (type == EventType.SEND && object instanceof ClipHolder) {
             ClipHolder clip = (ClipHolder) object;
             String name = clip.getClipName();
             int length = clip.getImageCount();
             boolean isSend = clip.isSend();
             return isSend && length != 0 && (name.equals(clipName) || clipName.equals(ANY_CLIP));
        }
        return false;
    }
   
   protected MessageAddress makeMessageAddress( Object objectToSend, MessageAddress clientAddress, 
   		int defaultTimeoutMillis, UID uid) { 
	   MessageAddress addr = super.makeMessageAddress(objectToSend, clientAddress, defaultTimeoutMillis, uid);
	   // TODO extract attributes from objectToSend, which is a ClipHolder
	   return addr;
   }
}
