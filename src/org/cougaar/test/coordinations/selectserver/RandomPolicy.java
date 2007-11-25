/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.mts.MessageAddress;

/**
 * This policy selects a server at random.
 */
public class RandomPolicy extends AbstractSelectionPolicy {
    public MessageAddress select(List<MessageAddress> servers) {
        int size = servers.size();
        if (size >= 1) {
            int index = (int) Math.floor(Math.random() * size);
            return servers.get(index);
        } else {
            return null;
        }
    }
}
