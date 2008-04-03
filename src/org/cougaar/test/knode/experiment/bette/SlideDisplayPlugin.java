package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.jar.JarConfigFinder;

public class SlideDisplayPlugin extends TodoPlugin {
	private SlideFrame frame;
	private String title = "SlideClient";
	private String[] args = { "-show-slides", "true" };
	private byte[] image;

	public void start() {
		super.start();
		// Setup Swing frame
		frame = new SlideFrame(title, args, this);
		frame.setVisible(true);

		// Display an image
		int i = 0;
		while (true) {
			try {
				image = readImageFromResource( i % gifs.length);
				log.shout("showing image " + i );
				frame.update(image, i);
				Thread.sleep(200);
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void quit() {
	}

	private String[] gifs = { "davisb00.jpg","davisb01.jpg", "davisb02.jpg","davisb03.jpg","davisb04.jpg",
			"davisb05.jpg","davisb06.jpg","davisb07.jpg","davisb08.jpg","davisb09.jpg","davisb10.jpg","davisb11.jpg",
			"davisb12.jpg","davisb13.jpg"};

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
