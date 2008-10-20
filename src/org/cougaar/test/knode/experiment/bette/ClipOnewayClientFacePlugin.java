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

    @Cougaar.Arg(name = "clipName", defaultValue = ANY_CLIP, description = "Name of the Clip. If not set to "
            + ANY_CLIP + " then all clips will be sent")
    public String clipName;

    public ClipOnewayClientFacePlugin() {
        super(new OneWay.Client());
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD, when = "isMyClipRelay")
    public void executeNewObjectToSend(SimpleRelay relay) {
        Object object = relay.getQuery();
        blackboard.publishAdd(object);
        blackboard.publishRemove(relay);
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

}
