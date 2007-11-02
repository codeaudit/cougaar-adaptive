/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations;

import org.cougaar.core.plugin.TodoPlugin;

/**
 * A TodoPluging for a particular coordination role
 */
public abstract class RolePlugin<R extends RoleSpec<?>> extends TodoPlugin {
    private final R role;
    
    public RolePlugin(R role) {
        this.role = role;
    }
    
    public R getRole() {
        return role;
    }
    
    public void load() {
        super.load();
        if (log.isInfoEnabled()) {
            CoordinationEventType[] subscribesTo = role.getSubscribes();
            for (CoordinationEventType type : subscribesTo) {
                log.info("subscribes to " + type);
            }
        }
    }
}
