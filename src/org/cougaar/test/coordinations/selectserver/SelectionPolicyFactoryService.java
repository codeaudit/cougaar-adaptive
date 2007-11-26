/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import org.cougaar.core.component.Service;

/**
 * Simple service api to create a {@link SelectionPolicy} on demand.  We don't
 * use this a true SOA service, since the extent is plugin-specific.
 */
public interface SelectionPolicyFactoryService extends Service {
    public SelectionPolicy makePolicy();
}
