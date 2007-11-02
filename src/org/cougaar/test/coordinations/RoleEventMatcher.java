/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations;

import org.cougaar.core.util.UniqueObject;

/**
 * This api is used to test blackboard objects against a given event type (T)
 * to determine relevance for the given role (R)
 */
public interface RoleEventMatcher<R extends RoleSpec<T>, T extends CoordinationEventType> {
    public boolean match(T type, UniqueObject object);
}
