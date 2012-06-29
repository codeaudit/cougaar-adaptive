package org.cougaar.test.knode.experiment.bette;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.util.annotations.Cougaar;

public class TimedImageFromURLPlugin
      extends AnnotatedSubscriptionsPlugin
      implements TimedImageService {
   private static final long NOW_THRESHOLD = 1000;
   private static final long SAME_THRESHOLD = 99; // max sample frame rate of
                                                  // 10fps

   @Cougaar.Arg(name = "imageURL", required = true, description = "URL for Web-cam")
   public URI imageURI;

   private ServiceProvider provider;
   private byte[] lastImage;
   private long lastImageTime;

   @Override
   public void load() {
      super.load();
      // Find the Image Files
      // Advertise the Timed Image service
      provider = new MyServiceProvider();
      getServiceBroker().addService(TimedImageService.class, provider);
   }

   @Override
   public void unload() {
      if (provider != null) {
         getServiceBroker().revokeService(TimedImageService.class, provider);
      }
      super.unload();
   }

   public byte[] getCurrentImage() {
      long now = System.currentTimeMillis();
      // simple cache of last image
      if (Math.abs(now - lastImageTime) < SAME_THRESHOLD) {
         return lastImage;
      }
      InputStream is = null;
      DataInputStream dis = null;
      byte[] image;
      try {
         URL url = imageURI.toURL();
         URLConnection urlConnection = url.openConnection();
         int length = urlConnection.getContentLength();
         image = new byte[length];
         is = urlConnection.getInputStream();
         dis = new DataInputStream(new BufferedInputStream(is));
         dis.readFully(image);
         lastImageTime = now;
         lastImage = image;
         return image;
      } catch (EOFException e) {
         log.warn("Could not get image from url=" + imageURI + " EOF Reason=" + e.getMessage());
      } catch (IOException e) {
         log.warn("Could not get image from url=" + imageURI + " Reason=" + e.getMessage());
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
               // don't care
            }
         }
      }
      // Failed return empty array
      return new byte[0];
   }

   // Timed Image Service Interface only good for requests close to now
   public byte[] getImage(long time) {
      long now = System.currentTimeMillis();
      if (Math.abs(now - time) < NOW_THRESHOLD) {
         return getCurrentImage();
      } else {
         return new byte[0];
      }
   }

   private class MyServiceProvider
         implements ServiceProvider {
      public MyServiceProvider() {
      }

      public Object getService(ServiceBroker sb, Object req, Class<?> cl) {
         return TimedImageFromURLPlugin.this;
      }

      public void releaseService(ServiceBroker arg0, Object arg1, Class<?> arg2, Object arg3) {
      }
   }

}
