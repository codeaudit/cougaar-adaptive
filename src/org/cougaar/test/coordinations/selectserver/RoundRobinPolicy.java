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
public class RoundRobinPolicy implements SelectionPolicy {
    
    private int lastIndex;

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.ROUND_ROBIN;
    }

     public MessageAddress select(List<MessageAddress> servers) {
         int size = servers.size();
         if (size > 1) {
             lastIndex = (lastIndex + 1) % size;
             return servers.get(lastIndex);
         } else if (size == 1){
             lastIndex=0;
             return servers.get(lastIndex);
         } else {
             return null;
         }
    }
     
   public void setup(ServiceBroker sb) {
       lastIndex=0;
    }

}
