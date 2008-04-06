package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.plugin.AnnotatedSubscriptionsPlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.annotations.Cougaar;

public class TimedImagePlugin extends AnnotatedSubscriptionsPlugin implements TimedImageService {
	
	@Cougaar.Arg(name = "cacheImages", defaultValue = "true", 
    		description = "Should Images be cached in memory after first read from disk ")
    public boolean  isCacheImages;
	
	private static final String IMAGE_FILE_EXTENSION = ".jpg";
    private static final String CONFIG_IMAGES_DIR = "images";
    private static final String FILE_IMAGE_DIR="/Volumes/Data/BBN/Projects/cougaar/HEAD/tools/run/images";
    private static final String RESOURCE_IMAGE_PACKAGE = "/org/cougaar/test/knode/experiment/bette/images";

		
	private final String[] betteImageNames = { "davisb00.jpg","davisb01.jpg", "davisb02.jpg","davisb03.jpg","davisb04.jpg",
			"davisb05.jpg","davisb06.jpg","davisb07.jpg","davisb08.jpg","davisb09.jpg","davisb10.jpg","davisb11.jpg",
			"davisb12.jpg","davisb13.jpg"};
	
	private String[] imageNames;
	private byte[][] imageCache;
    
    private ServiceProvider provider;

    public void load() {
        super.load();
        // Find the Image Files
        //imageNames=betteImageNames;
		// imageNames= listImageFilesInDirectory(imageDirectory);
		 imageNames= listImagesFromConfig(CONFIG_IMAGES_DIR);
		//imageNames= listImagesFromResourceDir(RESOURCE_IMAGE_PACKAGE);
		log.shout("Found images " + imageNames.length);
		// byte[] imagePayload = readImageFromResource(0);
		// log.shout("Image length" +imagePayload.length);
		for(String imageName : imageNames) {
			log.shout(imageName);
		}
		if (isCacheImages) {
			imageCache= new byte [imageNames.length][];	
		}
        
        // Advertise the Timed Image service
        provider = new MyServiceProvider();
        getServiceBroker().addService(TimedImageService.class, provider);
    }

    public void unload() {
        if (provider != null) {
        	getServiceBroker().revokeService(TimedImageService.class, provider);
        }
        super.unload();
    }
    
    // Timed Image Service Interface
	public byte[] getImage(long time) {
		int imageNumber= (int) time % imageNames.length;
		log.shout("Requested Image #" + imageNumber + " at " + time);
		if (isCacheImages && imageCache[imageNumber] != null) {
			return imageCache[imageNumber];
		} else {
			byte[] replyPayload = null;
			try {
				// TODO make Configurable image finder
				//replyPayload = readImageFromResource(imageNumber);
				//replyPayload = readImageFile(imageNumber);
				replyPayload = readImageFromConfig(imageNumber);
				if (isCacheImages) {
					imageCache[imageNumber] = replyPayload;
				}
			} catch (Exception e) {
				log.error("Could not get image #" + imageNumber + "message="+ e.getMessage());
			}
			return replyPayload;
		}
	}
	
	private class ImageFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
            return name.endsWith(IMAGE_FILE_EXTENSION);
        }
	}

	private String[] listImageFilesInDirectory(String dirName) {
		File dir = new File(dirName);   
        String[] fileList = dir.list(new ImageFilter());
        Arrays.sort(fileList);
		return fileList;
	}

	private byte[] readImageFile(int imageNumber) throws Exception {
		long imageLength = 0;
		byte[] pixels = null;
		File imageFile = new File(FILE_IMAGE_DIR +File.separator + imageNames[imageNumber]);
		imageLength = imageFile.length();
		log.shout("Image Number=" + imageNumber+ "Image Length="+imageLength);
		FileInputStream imageStream = new FileInputStream(imageFile);

		if (imageLength > Integer.MAX_VALUE) {
			imageStream.close();
			throw new RuntimeException("File too large");
		}
		pixels = new byte[(int) imageLength];
		imageStream.read(pixels);
		imageStream.close();
		return pixels;
	}
	
	private String[] listImagesFromConfig(String dirName) {
		ConfigFinder finder = ConfigFinder.getInstance();
		File dir = finder.locateFile(dirName);
		String[] fileList = dir.list(new ImageFilter());
        Arrays.sort(fileList);
		return fileList;
	}
	
	
	private byte[] readImageFromConfig(int imageNumber) throws Exception {
		byte[] pixels = null;
		String imageName=imageNames[imageNumber];
		ConfigFinder finder = ConfigFinder.getInstance();
		InputStream imageStream = finder.open(CONFIG_IMAGES_DIR + File.separator + imageName);
		pixels = new byte[imageStream.available()];
		imageStream.read(pixels);
		imageStream.close();
		return pixels;
	}
	
	// TODO Does not work
	// getResources Enumeration returns empty
	private String[]  listImagesFromResourceDir(String path)  {
		Enumeration<URL> imageUrls;
		try {
			imageUrls = getClass().getClassLoader().getResources(path);
			log.shout(imageUrls.toString());
			while (imageUrls.hasMoreElements()) {
				URL imageUrl= imageUrls.nextElement();
				log.shout(imageUrl.toString());
			}
		} catch (IOException e) {
			log.error("bad image resource" + e.getMessage());
		}
		return new String[0];
	}
	
	private byte[] readImageFromResource(int imageNumber)  {
		byte[] pixels = null;
		String imageName=imageNames[imageNumber];
		String fullName = RESOURCE_IMAGE_PACKAGE + File.separator + imageName;
		try {
			InputStream imageStream=getClass().getResourceAsStream(fullName);
			if (imageStream !=null) {
				pixels = new byte[imageStream.available()];
				imageStream.read(pixels);
				imageStream.close();
			} else {
				log.error("bad resource image #" + imageNumber + " " + fullName);
			}
		} catch (IOException e) {
			log.error("bad resource image #" + imageNumber + " " + fullName +" message=" +e.getMessage());
		}
		return pixels;
	}

    private class MyServiceProvider implements ServiceProvider {
        public MyServiceProvider() {
        }

        public Object getService(ServiceBroker sb, Object req, Class<?> cl) {
            return TimedImagePlugin.this;
        }

        public void releaseService(ServiceBroker arg0, Object arg1, Class<?> arg2, Object arg3) {
        }
    }
    
}
