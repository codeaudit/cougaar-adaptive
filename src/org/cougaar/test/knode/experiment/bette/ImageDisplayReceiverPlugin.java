package org.cougaar.test.knode.experiment.bette;

import org.cougaar.test.ping.PingBBReceiverPlugin;

public class ImageDisplayReceiverPlugin
      extends PingBBReceiverPlugin {

   // TODO use Annotated Service lookup
   TimedImageService imageService;

   @Override
   public void start() {
      super.start();
      imageService = getServiceBroker().getService(this, TimedImageService.class, null);
   }

   // Return image as payload.
   // use query payload as time
   @Override
   protected byte[] nextPayload(byte[] queryPayload) {
      long time = 0;
      if (queryPayload != null && queryPayload.length >= 1) {
         time = TimeBytesConverter.bytesToTime(queryPayload);
      }
      if (log.isInfoEnabled()) {
         log.info("Looking for Image at time " + time);
      }
      byte[] image = imageService.getImage(time);
      return image;
   }
}
