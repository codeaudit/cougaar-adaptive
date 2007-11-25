/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.service.LoggingService;

/**
 * Select the first server that is up in the list.
 */
public class FirstUpPolicy implements SelectionPolicy, Constants {
    private MetricsService metricsService;
    private LoggingService log;
    private final Map<MessageAddress, Callback> serverMetrics =
            new HashMap<MessageAddress, Callback>();

    public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
        // get metrics service
        this.log = log;
        try {
            metricsService = sb.getService(this, MetricsService.class, null);
        } catch (Exception e) {
            log.error("First Up Policy Unable to get MetricsService");
        }
        NodeIdentificationService nis = sb.getService(this, NodeIdentificationService.class, null);
        MessageAddress nodeAddr = nis.getMessageAddress();

        for (MessageAddress server : servers) {
            String path =
                    "Node(" + nodeAddr + ")" + PATH_SEPR + "Destination(" + server.getAddress()
                            + ")" + PATH_SEPR + "CapacityMax";
            log.info(path);
            serverMetrics.put(server, new Callback(path));
        }

    }

    public MessageAddress select(List<MessageAddress> servers) {
        for (MessageAddress server : servers) {
            Callback callback = serverMetrics.get(server);
            if (callback == null) {
                log.info("new server added, so no metric" + server);
                continue;
            }
            if (callback.getMetric().getCredibility() > 0.2) {
                return server;
            }
        }
        return null;
    }
    
    private class Callback implements Observer {
        String path;
        Metric metric;
        Object subscription_uid;

        Callback(String path) {
            this.path = path;
            MetricNotificationQualifier qualifier = null;
            subscription_uid = metricsService.subscribeToValue(path, this, qualifier);
        }

        Metric getMetric() {
            return metric;
        }

        public void update(Observable ignore, Object value) {
            if (value instanceof Metric) {
                this.metric = (Metric) value;
                log.info(path + "=" + value);
            } else {
                log.warn("got null or bogus value for path" + path);
            }
        }

        void unsubscribe() {
            metricsService.unsubscribeToValue(subscription_uid);
        }
    }

}
