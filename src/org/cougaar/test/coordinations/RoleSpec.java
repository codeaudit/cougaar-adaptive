/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations;

/**
 * Metadata describing blackboard in and out types for a given role
 */
public interface RoleSpec<T extends CoordinationEventType> {
    public T[] getPublishes();
    public T[] getSubscribes();
}
