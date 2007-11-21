/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.metrics.StandardVariableEvaluator;
import org.cougaar.core.service.LoggingService;

/**
 *
 */
public class FirstPolicy implements SelectionPolicy {

    private StandardVariableEvaluator variableEvaluator;
    private MetricsService metricsService;

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.FIRST_UP;
    }

    public MessageAddress select(List<MessageAddress> servers) {
        int size = servers.size();
        if (size >= 1) {
            return servers.get(0);
        } else {
            return null;
        }
    }

    public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
        // get metrics service
        try {
        variableEvaluator= new StandardVariableEvaluator(sb);
        metricsService = sb.getService(this, MetricsService.class, null);
        } catch (Exception e) {
          log.error("First Up Policy Unable to get MetricsService");
        }
    }
}
