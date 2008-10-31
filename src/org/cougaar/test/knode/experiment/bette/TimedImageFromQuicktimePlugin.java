package org.cougaar.test.knode.experiment.bette;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class TimedImageFromQuicktimePlugin
        extends AnnotatedSubscriptionsPlugin
        implements TimedImageService {
    private static final long NOW_THRESHOLD = 1000;
    private static final long SAME_THRESHOLD = 99; // max sample frame rate of 10fps
    private static final int CAMERA_X = 640; // max Camera X
    private static final int CAMERA_Y = 480; // max Camera Y
    //private static final int CAMERA_X = 1280; // max Camera X
    //private static final int CAMERA_Y = 1024; // max Camera Y

    private ServiceProvider provider;
    private byte[] lastImage; 
    private long lastImageTime;
	private QuicktimeVideoCapture qtVideoCamera = null;

    public void load() {
        super.load();
        // Advertise the Timed Image service
        provider = new MyServiceProvider();
        getServiceBroker().addService(TimedImageService.class, provider);
    }
    
    public void start () {
       
    	try {
			qtVideoCamera = new QuicktimeVideoCapture(CAMERA_X,CAMERA_Y);
		} catch (Exception e) {
			log.error("QuickTime initilization failure" + e.getMessage());
		}
    	
    }

    public void unload() {
        if (provider != null) {
            getServiceBroker().revokeService(TimedImageService.class, provider);
        }
        qtVideoCamera.dispose();
        super.unload();
    }

    public byte[] getCurrentImage() {
        long now=System.currentTimeMillis();
        // simple cache of last image
        if (Math.abs(now - lastImageTime) < SAME_THRESHOLD) {
            return lastImage;
        }
		byte[] imageBytes = null;
		BufferedImage image = null;
		try {
			image = qtVideoCamera.getNextImage();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
			encoder.encode(image);
			imageBytes = os.toByteArray();
			return imageBytes;
		} catch (ImageFormatException e) {
			log.error("Image Format Error" + e.getMessage());
		} catch (IOException e) {
			log.error("Io Error" + e.getMessage());
		} catch (Exception e) {
			log.error("Grab Image Error" + e.getMessage());
		} 
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
            return TimedImageFromQuicktimePlugin.this;
        }

        public void releaseService(ServiceBroker arg0, Object arg1, Class<?> arg2, Object arg3) {
        }
    }

}
