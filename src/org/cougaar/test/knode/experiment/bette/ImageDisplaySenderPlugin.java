package org.cougaar.test.knode.experiment.bette;

import org.cougaar.test.ping.PingBBSenderPlugin;
import org.cougaar.util.annotations.Cougaar;


public class ImageDisplaySenderPlugin extends PingBBSenderPlugin {
	
    @Cougaar.Arg(name = "displayImages", defaultValue = "true", 
    		description = "Images should be displayed on GUI")
    public boolean  isDisplayGifs;

	private ImageFrame frame;
	private byte gifCount = 0;
	
	

	protected byte[] initialPayload() {
		// Setup Swing frame
		String title = "SlideClient";
		String[] args = new String[2];
		args[0] = "-show-slides";
		args[1] = Boolean.toString(isDisplayGifs);
		frame = new ImageFrame(title, args, this);
		frame.setVisible(true);
		// initalize Payload array
		byte [] queryPayload = new byte[1];
		queryPayload[0] = gifCount;
		return queryPayload;
	}

	protected byte[] nextPayload(byte[] replyPayload) {
		// Display an image
		frame.update(replyPayload, gifCount);
		gifCount= (byte) ((gifCount +1) % 128);
		byte [] queryPayload = new byte[1];
		queryPayload[0] = gifCount;
		return queryPayload;
	}

	public void quit() {
	}

	
}
