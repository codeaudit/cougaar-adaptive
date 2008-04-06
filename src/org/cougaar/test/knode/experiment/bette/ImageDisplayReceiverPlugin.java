package org.cougaar.test.knode.experiment.bette;

import org.cougaar.test.ping.PingBBReceiverPlugin;

public class ImageDisplayReceiverPlugin extends PingBBReceiverPlugin {
	
	//TODO use Annotated Service lookup
	TimedImageService imageService;
	
	public void start() {
		super.start();
		imageService = 	getServiceBroker().getService(this, TimedImageService.class, null);
	}
	
	// Return image as payload.
	// use query payload as time 	
	protected byte[] nextPayload(byte[] queryPayload) {
		int imageNumber = 0;
		if (queryPayload != null && queryPayload.length >= 1) {
			// TODO convert whole byte array into index
			imageNumber=queryPayload[0] ;
		}
		log.shout("looking for Image #" + imageNumber);
		//TODO add null image
		byte[] image = imageService.getImage(imageNumber);
		return image;
	}
}
