/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.LoggingService;

/**
 * Base class for {@link SelectionPolicy} classes that need no setup.
 */
abstract class AbstractSelectionPolicy implements SelectionPolicy {
    public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
        // stateless by default, so setup is a no-op
    }
}
