/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.coordinations.oneway.OneWay.EventType;
import org.cougaar.core.qos.coordinations.oneway.OnewayServerFacePlugin;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.annotations.Cougaar;

public class ImageOnewaySenderFacePlugin
      extends OnewayServerFacePlugin {
   static final String ANY_STREAM = "AnyStream";

   @Cougaar.Arg(defaultValue = ANY_STREAM, description = "Name of the Stream. If not set to " + ANY_STREAM  + " then all streams will be sent")
   public String streamName;

   public boolean match(EventType type, UniqueObject object) {
      if (type == EventType.SEND && object instanceof ImageHolder) {
         ImageHolder image = (ImageHolder) object;
         String name = image.getStreamName();
         int length = image.length();
         return length != 0 && (name.equals(streamName) || streamName.equals(ANY_STREAM));
      }
      return false;
   }

   @Override
   protected MessageAddress makeMessageAddress(Object objectToSend, MessageAddress clientAddress, int defaultTimeoutMillis, UID uid) {
      MessageAddress addr = super.makeMessageAddress(objectToSend, clientAddress, defaultTimeoutMillis, uid);
      // TODO extract attributes from objectToSend, which is a ImageHolder
      return addr;
   }
}
