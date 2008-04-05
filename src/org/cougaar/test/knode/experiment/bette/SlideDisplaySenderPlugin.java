package org.cougaar.test.knode.experiment.bette;

import org.cougaar.test.ping.PingBBSenderPlugin;

public class SlideDisplaySenderPlugin extends PingBBSenderPlugin {
	private SlideFrame frame;
	private String title = "SlideClient";
	private String[] args = { "-show-slides", "true" };
	private byte gifCount = 0;

	protected byte[] initialPayload() {
		// Setup Swing frame
		frame = new SlideFrame(title, args, this);
		frame.setVisible(true);
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
