/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;

/**
 *
 */
public class FirstPolicy implements SelectionPolicy {

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.FIRST;
    }

    public MessageAddress select(List<MessageAddress> servers) {
        int size = servers.size();
        if (size >= 1) {
            return servers.get(0);
        } else {
            return null;
        }
    }

    public void setup(ServiceBroker sb) {
        //Stateless
    }
}
