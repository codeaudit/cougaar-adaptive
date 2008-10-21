/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.qos.coordinations.FacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipOnewayClientFacePlugin
        extends FacePlugin<OneWay.Client> {
    public static final String ANY_CLIP = "AnyClip";
    
    private Map<Long,ClipHolder> clips = new HashMap<Long,ClipHolder>();
    private Map<Long,Set<ImageHolder>> unclaimedImages = new HashMap<Long,Set<ImageHolder>>();

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
        clips.put(clip.getClipId(),clip);
        blackboard.publishAdd(clip);
        if (addUnclaimedImagesToClip(clip)) {
            blackboard.publishChange(clip);
        }
    }
    
    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyImageRelay")
    public void executeNewImageToRecieve(SimpleRelay relay) {
        Object object = relay.getQuery();
        ImageHolder image = (ImageHolder) object;
        ClipHolder clip  = addImageToClips(image);
        if (clip != null ) {
            blackboard.publishChange(clip);
            blackboard.publishAdd(image);
        } else {
            blackboard.publishAdd(image);
            log.warn("Received out of orderImage");
        }
    }
    
    private ClipHolder addImageToClips(ImageHolder image) {
        if (image == null) {
            return null;
        } else {
            Long clipId = image.getCreatorIncarnation();
            ClipHolder clip = clips.get(clipId);
            if (clip == null) {
                Set<ImageHolder> imageList = unclaimedImages.get(clipId);
                if (imageList == null) {
                    imageList = new HashSet<ImageHolder>();
                    unclaimedImages.put(clipId,imageList);
                }
                imageList.add(image);
                return null;
            } else {
                clip.addImage(image);
                return clip;
            }
        }
    }
    
    private boolean addUnclaimedImagesToClip(ClipHolder clip) {
        Long clipId = clip.getClipId();
        Set<ImageHolder> imagesToAdd = unclaimedImages.get(clipId);
        if (imagesToAdd != null) {
            for ( ImageHolder image : imagesToAdd) {
                clip.addImage(image);
            }
            return true;
        } else {
            return false;
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
