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
 * Select the server with the highest credible metric value. If several servers
 * have the same max (credible) value, prefer the one with highest credibility.
 * If several of these have the same max credibility, prefer the earlier ones in
 * the list.
 */
public class ClosestPolicy extends MetricBasedPolicy {
    public MessageAddress select(List<MessageAddress> servers) {
        double bestMetric = Double.MIN_VALUE;
        double bestCredibility = Double.MIN_VALUE;
        MessageAddress bestServer = null;
        for (MessageAddress server : servers) {
            Metric metric = getMetric(server);
            if (metric == null) {
                // No value for this server yet
                continue;
            }
            double credibility = metric.getCredibility();
            if (credibility < SYS_DEFAULT_CREDIBILITY) {
                // Value is present but not sufficiently credible
                continue;
            }
            double value = metric.doubleValue();
            if (value > bestMetric || value == bestMetric && credibility > bestCredibility) {
                bestMetric = value;
                bestCredibility = credibility;
                bestServer = server;
            }
        }
        return bestServer;
    }
}
