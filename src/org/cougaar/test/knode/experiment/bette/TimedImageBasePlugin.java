package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.FilenameFilter;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.util.annotations.Cougaar;

public class TimedImageBasePlugin extends AnnotatedSubscriptionsPlugin implements TimedImageService {
	
	@Cougaar.Arg(name = "cacheImages", defaultValue = "true", 
    		description = "Should Images be cached in memory after first read from disk ")
    public boolean  isCacheImages;
	
	@Cougaar.Arg(name = "imageDirectory", defaultValue = "/org/cougaar/test/knode/experiment/bette/images/big", 
    		description = "Directory where images are stored. Use the appropriate format based on where the images are stored")
    public String  imageDirectory;
	
	@Cougaar.Arg(name = "imageFileExtension", defaultValue = ".jpg", 
    		description = "File extension of image files")
    public String  imageFileExtension;
	
	@Cougaar.Arg(name = "imageFrameRate", defaultValue = "5.0", 
    		description = "Frames per Second")
    public double frameRate;
	
	@Cougaar.Arg(name = "loopBackwards", defaultValue = "false", 
    		description = "Run the image sequence backwards, instead of jumping back to the beginning")
    public boolean isLoopBackwards;


	
	private String[] imageNames;
	private byte[][] imageCache;
    private ServiceProvider provider;

    public void load() {
        super.load();
        // Find the Image Files
        imageNames=listImages(imageDirectory);
        if (isCacheImages && imageNames !=null) {
			imageCache= new byte [imageNames.length][];	
		}
        // Advertise the Timed Image service
        provider = new MyServiceProvider();
        getServiceBroker().addService(TimedImageService.class, provider);
    }
    
    public void start() {
    	super.start();
    	if (imageNames == null || imageNames.length == 0) {
    		log.warn("No images found in " + imageDirectory+ " extension " + imageFileExtension);
    		return;
    	}
    	if (log.isInfoEnabled()) {
    		log.info(imageNames.length + " images are available in " + imageDirectory);
    	}
    }

    public void unload() {
        if (provider != null) {
        	getServiceBroker().revokeService(TimedImageService.class, provider);
        }
        super.unload();
    }
    
    // Timed Image Service Interface
    public byte[] getImage(long time) {
        if (imageNames == null || imageNames.length == 0) {
            return new byte[0];
        }
        int numberImages = imageNames.length;
        int imageNumber = (int) ((time * frameRate / 1000) % numberImages);
        if (isLoopBackwards) {
            // loop by running backwards through list until the beginning
            long cycle = (long) ((time * frameRate / 1000) / numberImages);
            if (cycle % 2 == 1) {
                // odd cycle count backwards
                imageNumber = (numberImages - 1) - imageNumber;
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Requested Image #" + imageNumber + " at " + time);
        }
        if (isCacheImages && imageCache[imageNumber] != null) {
            return imageCache[imageNumber];
        } else {
            byte[] replyPayload = null;
            try {
                replyPayload = readImage(imageNumber);
                if (isCacheImages) {
                    imageCache[imageNumber] = replyPayload;
                }
            } catch (Exception e) {
                log.error("Could not get image #" + imageNumber + "message=" + e.getMessage());
            }
            return replyPayload;
        }
    }
	
	// This method should be overridden by childern classes
	protected byte[] readImage(int imageNumber) throws Exception {
		return new byte[0];
	}
	
	// This method should be overridden by childern classes
	protected String[] listImages(String directory) {
		return new String[0];
	}
	
	protected class ImageFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
            return name.endsWith(imageFileExtension);
        }
	}
	
    private class MyServiceProvider implements ServiceProvider {
        public MyServiceProvider() {
        }

        public Object getService(ServiceBroker sb, Object req, Class<?> cl) {
            return TimedImageBasePlugin.this;
        }

        public void releaseService(ServiceBroker arg0, Object arg1, Class<?> arg2, Object arg3) {
        }
    }
    
	public String[] getImageNames() {
		return imageNames;
	}

}
