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
public class FirstUpPolicy implements SelectionPolicy {
    

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.FIRST_UP;
    }

     public MessageAddress select(List<MessageAddress> servers) {
       return null;
    }
     
   public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
    }

}
