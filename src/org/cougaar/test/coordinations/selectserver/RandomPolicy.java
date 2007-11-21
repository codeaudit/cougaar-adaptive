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
 *
 */
public class RandomPolicy implements SelectionPolicy {

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.RANDOM;
    }

    public MessageAddress select(List<MessageAddress> servers) {
        int size = servers.size();
        if (size >= 1) {
            int index = (int) Math.floor(Math.random() * size);
            return servers.get(index);
        } else {
            return null;
        }
    }

    public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
        //Stateless
    }
}
