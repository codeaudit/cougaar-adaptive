package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

public class ImageHolder extends UniqueObjectBase {
	
	public ImageHolder(UIDService uids,
			long timeStamp,
			byte[] image) {
		super(uids.nextUID());
		this.image=image;
		this.timeStamp=timeStamp;
	}

	private byte[] image;
	private long timeStamp;

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

    public int length () {
    	return image.length;
    }
}
