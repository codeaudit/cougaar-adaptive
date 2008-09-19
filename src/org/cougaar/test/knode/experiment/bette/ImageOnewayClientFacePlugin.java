/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.qos.coordinations.oneway.OnewayClientFacePlugin;
import org.cougaar.core.qos.coordinations.oneway.OneWay.EventType;
import org.cougaar.core.util.UniqueObject;

public class ImageOnewayClientFacePlugin extends OnewayClientFacePlugin {
	public boolean match(EventType type, UniqueObject object) {
		if (type == EventType.RECEIVE && object instanceof ImageHolder) {
		// TODO any other test on image?
		return true;
		}
		return false;
	}
}
