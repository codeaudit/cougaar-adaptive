/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.mts.AttributeConstants;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageAddressWithAttributes;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.SimpleMessageAttributes;
import org.cougaar.core.qos.coordinations.FacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class ClipOnewaySenderFacePlugin
        extends FacePlugin<OneWay.Server> {
    public static final String UID_ATTRIBUTE = "UID";
    public static final String ANY_CLIP = "AnyClip";

    @Cougaar.Arg(name = "clientName", required = true)
    public MessageAddress clientAddress;

    @Cougaar.Arg(name = "defaultTimeoutMillis", defaultValue = "30000")
    public int defaultTimeoutMillis;

    @Cougaar.Arg(name = "deleteOnSend", defaultValue = "true")
    public boolean deleteOnSend;

    @Cougaar.Arg(name = "clipName", defaultValue = ANY_CLIP, description = "Name of the Clip. If set to "
            + ANY_CLIP + " then all clips will be sent")
    public String clipName;

    public ClipOnewaySenderFacePlugin() {
        super(new OneWay.Server());
    }

    @Cougaar.Execute(on = Subscribe.ModType.CHANGE, when = "isSendableClip")
    public void executeNewClipToSend(ClipHolder clipToSend) {
        sendObject(clipToSend);
        ImageLoop loop = clipToSend.getImageLoop();
        for (ImageHolder image : loop.schedule.values()) {
        	sendObject(image);
        }
    }

	private void sendObject(Object objectToSend) {
		UID uid = uids.nextUID();
        MessageAddress targetAddress =
                makeMessageAddress(objectToSend, clientAddress, defaultTimeoutMillis, uid);
        // Make relay that will be sent once
        SimpleRelay relay = new SimpleRelaySource(uid, agentId, targetAddress, null);
        relay.setQuery(objectToSend);
        blackboard.publishAdd(relay);
        blackboard.publishRemove(relay);
        // Delete the objectToSend
        if (deleteOnSend) {
            blackboard.publishRemove(objectToSend);
        }
	}

    /*
     * Override this method to use properties of the objectToSend to change the
     * attributes of the target address.
     */
    protected MessageAddress makeMessageAddress(Object objectToSend,
                                                MessageAddress clientAddress,
                                                int defaultTimeoutMillis,
                                                UID uid) {
        // make the target address with attributes
        MessageAttributes attrs = new SimpleMessageAttributes();
        // TODO add priority and other QoS attributes
        attrs.setAttribute(AttributeConstants.MESSAGE_SEND_TIMEOUT_ATTRIBUTE, defaultTimeoutMillis);
        // HACK: Relay Directives are put into different MTS messages, if their
        // target is different
        // We add an unique attribute value to the target address
        // so that the relay is guaranteed to go in its own message
        attrs.setAttribute(UID_ATTRIBUTE, uid);
        return MessageAddressWithAttributes.getMessageAddressWithAttributes(clientAddress, attrs);
     // TODO extract attributes from objectToSend, which is a ClipHolder
    }

 
    public boolean isSendableClip(ClipHolder object) {
            ClipHolder clip = (ClipHolder) object;
            String name = clip.getClipName();
            int length = clip.getImageCount();
            boolean isSend = clip.isSend();
            return isSend && length != 0 && (name.equals(clipName) || clipName.equals(ANY_CLIP));
    }
}
