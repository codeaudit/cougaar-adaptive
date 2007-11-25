/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.mts.MessageAddress;

/**
 * This policy walks through the servers in order.
 */
public class RoundRobinPolicy extends AbstractSelectionPolicy {

    private int lastIndex = 0;

    public MessageAddress select(List<MessageAddress> servers) {
        int size = servers.size();
        if (size > 1) {
            lastIndex = (lastIndex + 1) % size;
            return servers.get(lastIndex);
        } else if (size == 1) {
            lastIndex = 0;
            return servers.get(lastIndex);
        } else {
            return null;
        }
    }


}
