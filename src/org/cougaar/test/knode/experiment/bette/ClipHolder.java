package org.cougaar.test.knode.experiment.bette;

import java.io.IOException;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

/*
 * Blackboard object holds a Clip of images and meta data about when, where, and how the clip was
 * taken
 */
public class ClipHolder
        extends UniqueObjectBase {

    private long startTime;
    private long endTime;
    private String clipName;
    private long clipId;
    private String note;
    private boolean done = false;
    transient private boolean send ;
    transient private ImageLoop imageLoop ;
    // TODO add location
 
    public ClipHolder(UIDService uids,
                       long startTime,
                       String clipName,
                       long clipId){
        super(uids.nextUID());
        this.startTime = startTime;
        this.clipId = clipId;
        this.clipName = clipName;
        this.imageLoop = new ImageLoop(0);
    }
    
    // initialize transients
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        send=false;
        if (done) {
        imageLoop = new ImageLoop(endTime - startTime);
        } else {
            imageLoop = new ImageLoop(0);
        }
   }

    public void addImage(long time, ImageHolder image) {
        imageLoop.add(time, image);
    }

    public int getImageCount() {
        return imageLoop.getImageCount();
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public long getDuration() {
        return imageLoop.getEndTime();
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime; // absolute
        imageLoop.setEndTime(endTime - startTime); //relative time
        done=true;
    }

    public long getClipId() {
        return clipId;
    }

    public String getClipName() {
        return clipName;
    }

    public long getStartTime() {
        return startTime;
    }

    public ImageLoop getImageLoop() {
        return imageLoop;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public boolean isDone() {
        return done;
    }

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}
}

