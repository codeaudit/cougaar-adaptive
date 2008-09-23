package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UniqueObjectBase;

/*
 * Blackboard object that holds a image that is part of a stream of images
 */
public class ImageHolder
        extends UniqueObjectBase {

    private byte[] image;
    private long timeStamp;
    private String streamName;
    private long count;
    private long creatorIncarnation; 

    public ImageHolder(UIDService uids,
                       long timeStamp,
                       byte[] image,
                       String streamName,
                       long count,
                       long creatorIncarnation) {
        super(uids.nextUID());
        this.image = image;
        this.timeStamp = timeStamp;
        this.count = count;
        this.streamName = streamName;
        this.creatorIncarnation = creatorIncarnation;
    }
    
    public long getCreatorIncarnation() {
        return creatorIncarnation;
    }
    
    public void setCreatorIncarnation(long creatorIncarnation) {
        this.creatorIncarnation = creatorIncarnation;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

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

    public int length() {
        return image.length;
    }
}
