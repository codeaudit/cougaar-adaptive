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
import org.cougaar.core.qos.metrics.StandardVariableEvaluator;
import org.cougaar.core.service.LoggingService;

/**
 * Select the first server that is up in the list.
 */
public class FirstUpPolicy implements SelectionPolicy,Constants {
    

    private MetricsService metricsService;
    private LoggingService log;
    private Map<MessageAddress,Callback> serverMetrics = new HashMap<MessageAddress,Callback>();

    public SelectionPolicyName getPolicy() {
        return SelectionPolicyName.FIRST_UP;
    }

     public MessageAddress select(List<MessageAddress> servers) {
       return null;
    }
     
   public void setup(ServiceBroker sb, LoggingService log, List<MessageAddress> servers) {
       // get metrics service
       this.log=log;
       try {
       metricsService = sb.getService(this, MetricsService.class, null);
       } catch (Exception e) {
         log.error("First Up Policy Unable to get MetricsService");
       }
       NodeIdentificationService nis = sb.getService(this, NodeIdentificationService.class, null);
       MessageAddress nodeAddr = nis.getMessageAddress();
  
       for (MessageAddress server : servers) {
           String path="Node("+nodeAddr+")"+PATH_SEPR+"Destination("+ server.getAddress() + ")"+PATH_SEPR+"CapacityMax";
           log.info(path);
           serverMetrics.put(server, new Callback(path,metricsService));
       }
       
   }
   
   private class Callback implements Observer {
        String path;
        Metric current;
        Object subscription_uid;
        MetricsService metricService;
      

        Callback(String path, MetricsService svc) {
            this.path = path;
            this.metricService=svc;
            MetricNotificationQualifier qualifier = null;
            subscription_uid = metricService.subscribeToValue(path, this, qualifier);
        }

        public void update(Observable ignore, Object value) {
            if (value != null) {
                this.current = (Metric) value;
                log.info(path+"="+value);
            } else {
                log.warn("got null value for path"+path);
            }
        }

        void unsubscribe() {
            metricService.unsubscribeToValue(subscription_uid);
        }
    }

}
