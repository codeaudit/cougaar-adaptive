/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.qos.coordinations.FacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipOnewayClientFacePlugin
        extends FacePlugin<OneWay.Client> {
    public static final String ANY_CLIP = "AnyClip";
    private ClipHolder  latestClip;

    @Cougaar.Arg(name = "clipName", defaultValue = ANY_CLIP, description = "Name of the Clip. If set to "
            + ANY_CLIP + " then all clips will be recieved")
    public String clipName;

    public ClipOnewayClientFacePlugin() {
        super(new OneWay.Client());
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyClipRelay")
    public void executeNewClipToRecieve(SimpleRelay relay) {
        Object object = relay.getQuery();
        ClipHolder clip = (ClipHolder) object;
        latestClip = clip;
        blackboard.publishAdd(clip);
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyImageRelay")
    public void executeNewImageToRecieve(SimpleRelay relay) {
        Object object = relay.getQuery();
        ImageHolder image = (ImageHolder) object;
        if ( !(latestClip == null) && (latestClip.getClipId()==image.getCreatorIncarnation())) {
        	latestClip.addImage(image.getTimeStamp() - latestClip.getStartTime(), image);
        	 blackboard.publishChange(latestClip);
        	 blackboard.publishAdd(image);
        } else {
        	log.warn("Received out of orderImage");
        }
    }
    
    

    public boolean isMyClipRelay(SimpleRelay relay) {
        // unpack content from relay
        Object relayContents = relay.getQuery();
        // test if my clipholder
        if (relayContents instanceof ClipHolder ) {
            ClipHolder clip = (ClipHolder) relayContents;
            return clip.getClipName().equals(clipName) || clipName.equals(ANY_CLIP);
        }
        return false;
    }

    public boolean isMyImageRelay(SimpleRelay relay) {
        // unpack content from relay
        Object relayContents = relay.getQuery();
        // test if my imageholder
        if (relayContents instanceof ImageHolder ) {
            ImageHolder image = (ImageHolder) relayContents;
            return image.getStreamName().equals(clipName) || clipName.equals(ANY_CLIP);
        }
        return false;
    }

}
