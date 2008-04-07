package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class TimedImageFromResourcePlugin extends TimedImageBasePlugin {
	
	private final String[] betteImageNames = { "davisb00.jpg","davisb01.jpg", "davisb02.jpg","davisb03.jpg","davisb04.jpg",
			"davisb05.jpg","davisb06.jpg","davisb07.jpg","davisb08.jpg","davisb09.jpg","davisb10.jpg","davisb11.jpg",
			"davisb12.jpg","davisb13.jpg"};

    
    // TODO Stuck with explicit list for now
    protected String[] listImages(String dir) {
    	return betteImageNames;
    }
	
	protected byte[] readImage(int imageNumber)  {
		byte[] pixels = null;
		String[] imageNames=getImageNames();
		String imageName=imageNames[imageNumber];
		String fullName = imageDirectory + File.separator + imageName;
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
	
	// TODO Does not work
	// getResources Enumeration returns empty
	@SuppressWarnings("unused")
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

}
