package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.cougaar.test.ping.PingBBReceiverPlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.annotations.Cougaar;

public class ImageDisplayReceiverPlugin extends PingBBReceiverPlugin {
	
    @Cougaar.Arg(name = "cacheImages", defaultValue = "true", 
    		description = "Should Images be cashed in memory after first read from disk ")
    public boolean  isCasheGifs;

	
	// TODO get the images from dynamically Directory
	private final String[] gifs = { "davisb00.jpg","davisb01.jpg", "davisb02.jpg","davisb03.jpg","davisb04.jpg",
			"davisb05.jpg","davisb06.jpg","davisb07.jpg","davisb08.jpg","davisb09.jpg","davisb10.jpg","davisb11.jpg",
			"davisb12.jpg","davisb13.jpg"};
	private byte[][] gifCache = new byte [gifs.length][];
	
	// Return gif as payload.
	// use query payload as index into the images	
	protected byte[] nextPayload(byte[] queryPayload) {
		int gifNumber = 0;
		if (queryPayload != null && queryPayload.length >= 1) {
			// TODO convert whole byte array into index
			gifNumber=queryPayload[0] % gifs.length;
		}
		if (isCasheGifs && gifCache[gifNumber] != null) {
			return gifCache[gifNumber];
		} else {
			byte[] replyPayload = null;
			try {
				replyPayload = readImageFromResource(gifNumber);
				if (isCasheGifs) {
					gifCache[gifNumber] = replyPayload;
				}
			} catch (Exception e) {
				log.error("Could not get gif #" + gifNumber);
			}
			return replyPayload;
		}
	}


	private byte[] readImageFile(int gifNumber) throws Exception {
		long gifLength = 0;
		byte[] pixels = null;
		File gifFile = new File(gifs[gifNumber]);
		gifLength = gifFile.length();
		FileInputStream gifStream = new FileInputStream(gifFile);

		if (gifLength > Integer.MAX_VALUE) {
			gifStream.close();
			throw new RuntimeException("File too large");
		}
		pixels = new byte[(int) gifLength];
		gifStream.read(pixels);
		gifStream.close();
		return pixels;
	}
	
	private byte[] readImageFromConfig(int gifNumber) throws Exception {
		byte[] pixels = null;
		String gifName=gifs[gifNumber];
		ConfigFinder finder = ConfigFinder.getInstance();
		InputStream gifStream = finder.open(gifName);
		pixels = new byte[gifStream.available()];
		gifStream.read(pixels);
		gifStream.close();
		return pixels;
	}
	
	private byte[] readImageFromResource(int gifNumber) throws Exception {
		byte[] pixels = null;
		String gifName=gifs[gifNumber];
		InputStream gifStream=getClass().getResourceAsStream("images/" + gifName);
		pixels = new byte[gifStream.available()];
		gifStream.read(pixels);
		gifStream.close();
		return pixels;
	}
	
	// TODO Does not work
	// getResources Enumeration returns empty
	private byte[] readImageFromResourceDir(int gifNumber) throws Exception {
		byte[] pixels = null;
		Enumeration<URL> gifUrls = getClass().getClassLoader().getResources("org.cougaar.test.knode.experiment.bette.images");
		log.shout(gifUrls.toString());
		while (gifUrls.hasMoreElements()) {
			URL gifUrl= gifUrls.nextElement();
			log.shout(gifUrl.toString());
		}
		return pixels;
	}
	
}
