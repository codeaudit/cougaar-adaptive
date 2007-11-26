/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.List;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Metric;

/**
 * Select the first server in the list that is up.
 */
public class FirstUpPolicy extends MetricBasedPolicy {
    public MessageAddress select(List<MessageAddress> servers) {
        for (MessageAddress server : servers) {
            Metric metric = getMetric(server);
            if (metric != null && metric.getCredibility() > 0.2) {
                return server;
            }
        }
        return null;
    }
}
