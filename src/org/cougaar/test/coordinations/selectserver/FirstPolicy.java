/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.mts.MessageAddress;

/**
 * This trivial policy simply selects the first server.  Useful
 * for deterministic regression tests.
 */
public class FirstPolicy extends AbstractSelectionPolicy {
    public MessageAddress select(List<MessageAddress> servers) {
        return servers.isEmpty() ? null : servers.get(0);
    }
}
