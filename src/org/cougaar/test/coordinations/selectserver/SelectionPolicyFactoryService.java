/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import org.cougaar.core.component.Service;

/**
 *  Simple service to create a {@link SelectionPolicy} on demand.
 */
public interface SelectionPolicyFactoryService extends Service {
    public SelectionPolicy makePolicy();
}
