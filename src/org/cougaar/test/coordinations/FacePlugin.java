/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations;

import org.cougaar.core.plugin.TodoPlugin;

/**
 * A TodoPluging for a particular coordination face exposed
 * to the outside.
 */
public abstract class FacePlugin<F extends Face<?>> extends TodoPlugin {
    private final F face;
    
    public FacePlugin(F face) {
        this.face = face;
    }
    
    public F getFace() {
        return face;
    }
    
    public void load() {
        super.load();
        if (log.isInfoEnabled()) {
            CoordinationEventType[] subscribesTo = face.consumes();
            for (CoordinationEventType type : subscribesTo) {
                log.info("subscribes to " + type);
            }
        }
    }
}
