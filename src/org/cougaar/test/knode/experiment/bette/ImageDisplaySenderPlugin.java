package org.cougaar.test.knode.experiment.bette;

import org.cougaar.test.ping.PingBBSenderPlugin;
import org.cougaar.util.annotations.Cougaar;


public class ImageDisplaySenderPlugin extends PingBBSenderPlugin  implements Quitable{
	
    @Cougaar.Arg(name = "displayImages", defaultValue = "true", 
    		description = "Images should be displayed on GUI")
    public boolean  isDisplayGifs;
    
    @Cougaar.Arg(name = "realTime", defaultValue = "false", 
    		description = "When false, image sequence will start from begining (time=0.0)")
    public boolean  isRealTime;
    
    @Cougaar.Arg(name = "title", defaultValue = "Slide Client", 
    		description = "text for title on slide viewer frame")
    public String  title;
   


	private ImageFrame frame;
	private long startTime;
	private long sentTime;
	

	protected byte[] initialPayload() {
		// Setup Swing frame
		String[] args = new String[2];
		args[0] = "-show-slides";
		args[1] = Boolean.toString(isDisplayGifs);
		frame = new ImageFrame(title, args, this);
		frame.setVisible(true);
		// initalize Payload array
		startTime = isRealTime ? 0 : System.currentTimeMillis();
		sentTime=0;
		byte [] queryPayload = TimeBytesConverter.timeToBytes(sentTime);
		return queryPayload;
	}

	protected byte[] nextPayload(byte[] replyPayload) {
		// Display an image
		frame.update(replyPayload, sentTime);
		sentTime= System.currentTimeMillis()- startTime;
		byte [] queryPayload = TimeBytesConverter.timeToBytes(sentTime);
		return queryPayload;
	}

	public void quit() {
	}

	
}
