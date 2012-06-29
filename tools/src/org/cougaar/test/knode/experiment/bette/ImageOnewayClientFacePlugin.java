/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.qos.coordinations.oneway.OneWay.EventType;
import org.cougaar.core.qos.coordinations.oneway.OnewayClientFacePlugin;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.annotations.Cougaar;

public class ImageOnewayClientFacePlugin
      extends OnewayClientFacePlugin {

   public static final String ANY_STREAM = "AnyStream";

   @Cougaar.Arg(defaultValue = ANY_STREAM, description = "Name of the Stream. If set to " + ANY_STREAM  + " then all streams will be recieved")
   public String streamName;

   public boolean match(EventType type, UniqueObject object) {
      if (type == EventType.RECEIVE && object instanceof ImageHolder) {
         ImageHolder image = (ImageHolder) object;
         return image.getStreamName().equals(streamName) || streamName.equals(ANY_STREAM);
      }
      return false;
   }
}
