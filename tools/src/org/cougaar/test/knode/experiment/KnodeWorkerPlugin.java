/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Aug 14, 2007
 * Workfile: PingWorkerPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-03-04 18:04:26 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.knode.experiment;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.test.sequencer.Context;
import org.cougaar.test.sequencer.ReportBase;
import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentWorkerPlugin;
import org.cougaar.util.annotations.Cougaar;

public class KnodeWorkerPlugin
      extends ExperimentWorkerPlugin
      implements KnodeSteps {
   private String reason = "no reason";
   private PrintStream stream;

   @Cougaar.ObtainService
   public MetricsService metricsService;

   @Cougaar.Arg(required = true)
   public InetAddress knodeControllerHost;

   @Cougaar.Arg(defaultValue = "1175")
   public short knodeControllerPort;

   private Callback validityCallback;

   @Override
   protected void doStep(ExperimentStep step, Context context) {
      if (KNODE_ADD_LINK.equals(step)) {
         // send KNODE message to socket,
         String link = context.getParameter(LINK_PROPERTY);
         if (link != null) {
            String text = "add link " + link;
            log.info("Sending command \"" + text + "\"");
            stream.println(text);
            // No feedback from KNODE
            stepCompeleted(step, new ReportBase(workerId, true, reason));
         } else {
            stepCompeleted(step, new ReportBase(workerId, false, "no link"));
         }
      } else if (KNODE_DEL_LINK.equals(step)) {
         String link = context.getParameter(LINK_PROPERTY);
         if (link != null) {
            String text = "del link " + link;
            log.info("Sending command \"" + text + "\"");
            stream.println(text);
            // No feedback from KNODE
            stepCompeleted(step, new ReportBase(workerId, true, reason));
         } else {
            stepCompeleted(step, new ReportBase(workerId, false, "no link"));
         }
      } else if (KNODE_SET_METRIC.equals(step)) {
         String path = context.getParameter(METRIC_PATH_PROPERTY);
         if (path != null) {
            if (validityCallback != null) {
               validityCallback.unsubscribe();
            }
            validityCallback = new Callback(path);
            stepCompeleted(step, new ReportBase(workerId, true, reason));
         } else {
            stepCompeleted(step, new ReportBase(workerId, false, "no path"));
         }
      } else if (KNODE_WAIT_METRIC.equals(step)) {
         if (validityCallback != null) {
            String desiredValueString = context.getParameter(METRIC_VALUE_PROPERTY);
            if (desiredValueString != null) {
               try {
                  double desiredValue = Double.parseDouble(desiredValueString);
                  validityCallback.waitFor(step, desiredValue);
               } catch (NumberFormatException e) {
                  stepCompeleted(step, new ReportBase(workerId, false, "bad desired knode value:" + desiredValueString));
               }
            } else {
               stepCompeleted(step, new ReportBase(workerId, false, "No desired knode value"));
            }
         } else {
            stepCompeleted(step, new ReportBase(workerId, false, "Did not set knode validity path before wait"));
         }
      } else if (SOCIETY_READY.equals(step)) {
         try {
            Socket skt = new Socket(knodeControllerHost, knodeControllerPort);
            stream = new PrintStream(skt.getOutputStream());
            log.info("Opened connection to " + skt);
            stepCompeleted(step, new ReportBase(workerId, true, reason));
         } catch (IOException e) {
            String msg = "Failed to connect to KNode Controller: " + e.getMessage();
            log.error(msg);
            stepCompeleted(step, new ReportBase(workerId, false, msg));
         }
      } else if (SHUTDOWN.equals(step)) {
         if (stream != null) {
            stream.close();
         }
         stepCompeleted(step, new ReportBase(workerId, true, reason));
      } else {
         stepCompeleted(step, new ReportBase(workerId, true, reason));
      }
   }

   private class Callback
         implements Observer {
      private static final double epsilon = 0.001;
      private final String path;
      private final Object subscription_uid;
      private Metric metric;
      private double desiredValue;
      private ExperimentStep step;

      Callback(String path) {
         this.path = path;
         MetricNotificationQualifier qualifier = null;
         subscription_uid = metricsService.subscribeToValue(path, this, qualifier);
      }

      public void waitFor(ExperimentStep step, double desiredValue) {
         this.desiredValue = desiredValue;
         this.step = step;
         checkValueInRange();
      }

      private synchronized boolean checkValueInRange() {
         if (metric != null && step != null) {
            double metricValue = metric.doubleValue();
            if (Math.abs(metricValue - desiredValue) < epsilon) {
               ExperimentStep completedStep = step;
               step = null;
               stepCompeleted(completedStep, new ReportBase(workerId, true, reason));
               return true;
            }
         }
         return false;
      }

      public void update(Observable ignore, Object value) {
         if (value instanceof Metric) {
            this.metric = (Metric) value;
            checkValueInRange();
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
