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
 * Policy for choosing among multiple servers.
 * The policy may be stateful or use external services
 */
public interface SelectionPolicy {
    /**
     *  Identifier for this policy    
     */
    public SelectionPolicyName getPolicy();
    
    /**
     * Setup Policy state
     */
    public void setup(ServiceBroker sb);
    
   /**
    * Select a server from a list of servers based on the policy
    */
    public MessageAddress select (List<MessageAddress> servers);
}
