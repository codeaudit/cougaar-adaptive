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
 * Policy for choosing among multiple servers.
 * The policy may be stateful or use external services
 */
public interface SelectionPolicy {
    /**
     *  Identifier for this policy    
     */
    public SelectionPolicyName getPolicy();
    

    public void setup(ServiceBroker sb, 
                      LoggingService log, 
                      List<MessageAddress> servers);
    
   /**
    * Select a server from a list of servers based on the policy
    */
    public MessageAddress select (List<MessageAddress> servers);
}
