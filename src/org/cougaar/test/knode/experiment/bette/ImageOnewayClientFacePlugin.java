/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.qos.coordinations.oneway.OnewayClientFacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay.EventType;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.util.UniqueObject;

public class ImageOnewayClientFacePlugin extends OnewayClientFacePlugin {
	public boolean match(EventType type, UniqueObject object) {
		if (type == EventType.RECEIVE && object instanceof SimpleRelay) {
			SimpleRelay relay = (SimpleRelay) object;
			Object image = relay.getQuery();
			return image instanceof ImageHolder;
		}
		return false;
	}
}
