package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.FileInputStream;

import org.cougaar.core.plugin.TodoPlugin;

public class SlideDisplayPlugin extends TodoPlugin {
	private SlideFrame frame;
	private String title = "SlideClient";
	private String[] args = {"-show-slides", "true"};
	private byte[] image;
	
	public void start() {
		super.start();
		 frame = new SlideFrame(title, args, this);
		 frame.setVisible(true);
		 try {
			 image=readImage(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.update(image, 0);
	}

	
	public void quit() {
	}
	
	 private String[] gifs ={"davisb01.jpg"};
	 
	   private byte[] readImage (int gifNumber) throws Exception {
           long gifLength = 0;
           byte[] pixels = null;

           File gifFile = new File(gifs[gifNumber]);
           gifLength = gifFile.length();
           FileInputStream gifStream = new FileInputStream(gifFile);

           if (gifLength > Integer.MAX_VALUE) {
               gifStream.close();
               throw new RuntimeException("File too large");
           }
           pixels = new byte[(int)gifLength];
           gifStream.read(pixels);
           gifStream.close();
	return pixels;
}
}
