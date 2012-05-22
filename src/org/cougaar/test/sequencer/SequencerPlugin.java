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
 * Created : Aug 13, 2007
 * Workfile: NodeLocalSequencerPlugin.java
 * $Revision: 1.2 $
 * $Date: 2008-02-27 18:06:38 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

abstract public class SequencerPlugin<S extends Step, R extends Report, C extends Context>
      extends TodoPlugin {

   @Cougaar.Arg(name = "nodeCount", required = true)
   public int nodeCount;

   @Cougaar.Arg(name = "defaultWorkerTimeout", defaultValue = "10000", description = "Default time Aggregators should wait for all its Workers to complete a Step")
   public int defaultWorkerTimeout;

   @Cougaar.Arg(name = "defaultNodeTimeout", defaultValue = "60000", description = "Default time Sequencer should wait for all its Nodes to complete a Step")
   public int defaultNodeTimeout;

   private Map<String, NodeRegistrationEvent> registeredNodes = new HashMap<String, NodeRegistrationEvent>();
   private Map<String, Set<R>> completionReports = new HashMap<String, Set<R>>();
   private int numberOfWorkers;
   private boolean failedDuringStep = false;
   private Alarm nodeTimoutAlarm;
   private S processingStep;
   protected boolean failedDuringSequence = false;

   abstract protected S getFirstStep();

   abstract protected S getNextStep(S step);

   abstract protected void sequenceCompleted();

   abstract protected void sequenceFailed(SocietyCompletionEvent<S, R> evt);

   abstract protected C makeContext(S step, boolean hasFailed, int workerTimeout);

   abstract protected Set<R> makeNodeTimoutFailureReport(S step, String reason);

   protected NodeRequest<S, C> makeNodeRequest(S step) {
      C ctx = makeContext(step, failedDuringSequence, defaultWorkerTimeout);
      return new NodeRequest<S, C>(uids, step, ctx);
   }

   @Override
   public void setupSubscriptions() {
      super.setupSubscriptions();
      if (log.isInfoEnabled()) {
         log.info("Sequencer waiting for nodes to register");
      }
      String reason = "Sequencer Failed waiting for Node to register";
      nodeTimoutAlarm = executeLater(defaultNodeTimeout, new NodeTimeout(reason));
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeRegisterNode(NodeRegistrationEvent event) {
      String name = event.getNodeId();
      if (registeredNodes.size() == nodeCount) {
         log.warn("Skipping extra worker " + name);
         return;
      } else if (registeredNodes.containsKey(name)) {
         log.warn("Skipping duplicate worker " + name);
         return;
      }
      registeredNodes.put(name, event);
      numberOfWorkers += event.getNumberWorkers();
      if (log.isInfoEnabled()) {
         log.info("Registered Node " + name);
      }

      if (registeredNodes.size() == nodeCount) {
         SocietyRegistrationEvent societyRegistrationEvent = new SocietyRegistrationEvent(uids, numberOfWorkers, nodeCount, true);
         blackboard.publishAdd(societyRegistrationEvent);
      }
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeNodeCompletionEvent(NodeCompletionEvent<S, R> event) {
      Set<R> reports = event.getReports();
      S step = event.getStep();
      String nodeId = event.getId();
      boolean successfulEvent = event.isSuccessful();
      if (processingStep != step) {
         // Wrong Step
         log.warn("Node " + nodeId + " reported during wrong step. Ignoring Report:" + " currentStep=" + processingStep
               + " reportedStep=" + step + " success=" + successfulEvent);
         return;
      }
      if (completionReports.containsKey(nodeId)) {
         // Duplicate Report
         log.warn("Node " + nodeId + " reported multiple times. Ignoring Report:" + " currentStep=" + processingStep
               + " reportedStep=" + step + " success=" + successfulEvent);
         return;
      }
      completionReports.put(nodeId, reports);
      if (!successfulEvent) {
         // Failed Node Completion
         failedDuringSequence = true;
         failedDuringStep = true;
         SocietyCompletionEvent<S, R> completionEvent = new SocietyCompletionEvent<S, R>(uids, step, completionReports, false);
         completionReports = new HashMap<String, Set<R>>();
         blackboard.publishAdd(completionEvent);
         processingStep = null;
         return;
      }
      // All reports have come in successfully
      if (completionReports.size() == nodeCount && !failedDuringStep) {
         // All workers have finished
         cancelNodeTimout();
         SocietyCompletionEvent<S, R> completionEvent = new SocietyCompletionEvent<S, R>(uids, step, completionReports, true);
         completionReports = new HashMap<String, Set<R>>();
         blackboard.publishAdd(completionEvent);
         processingStep = null;
         return;
      }
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public void executeSocietyCompletion(SocietyCompletionEvent<S, R> event) {
      resumeSocietyCompletion(event);
   }

   @Cougaar.Execute(on = Subscribe.ModType.ADD)
   public final void executeSocietyRegistration(SocietyRegistrationEvent registration) {
      if (registration.isSuccessful()) {
         cancelNodeTimout();
         S firstStep = getFirstStep();
         if (log.isInfoEnabled()) {
            log.info("Starting first step=" + firstStep);
         }
         nodeTimoutAlarm = executeLater(defaultNodeTimeout, new NodeTimeout(firstStep));
         processingStep = firstStep;
         failedDuringStep = false;
         NodeRequest<S, C> req = makeNodeRequest(firstStep);
         blackboard.publishAdd(req);
      } else {
         // Node Failed Registration
         // Todo: need failed registration event

      }
   }

   public void deferSocietyCompletion(final SocietyCompletionEvent<S, R> event, long delay) {
      executeLater(delay, new Runnable() {
         public void run() {
            resumeSocietyCompletion(event);
         }
      });
   }

   public void resumeSocietyCompletion(SocietyCompletionEvent<S, R> event) {
      if (event.isSuccessful()) {
         S step = event.getStep();
         S nextStep = getNextStep(step);
         if (nextStep != null) {
            publishNodeRequestStep(nextStep);
         } else {
            // Everything finished successfully.
            sequenceCompleted();
         }
      } else {
         // Some worker failed.
         sequenceFailed(event);
      }
   }

   // Helper method for subclasses that use Steps implemented as an Enum
   protected S getNextEnumConstant(S step, Class<S> enumClass) {
      if (Enum.class.isAssignableFrom(enumClass)) {
         S[] steps = enumClass.getEnumConstants();
         int ordinal = ((Enum<?>) step).ordinal();
         if (ordinal < steps.length - 1) {
            return steps[++ordinal];
         } else {
            return null;
         }
      } else {
         throw new RuntimeException(enumClass + " is not an Enum");
      }
   }

   private final class NodeTimeout
         implements Runnable {
      private final S step;
      private final String reason;

      public NodeTimeout(S step) {
         this.step = step;
         this.reason = "Sequencer timed out waiting for a Node to complete Step " + step;
      }

      public NodeTimeout(String reason) {
         this.step = null;
         this.reason = reason;
      }

      public void run() {
         // Publish a society Completion Event with a failure report
         Set<R> reports = makeNodeTimoutFailureReport(step, reason);
         log.warn(reason);
         completionReports.put(agentId.getAddress(), reports);
         SocietyCompletionEvent<S, R> completionEvent = new SocietyCompletionEvent<S, R>(uids, step, completionReports, false);
         completionReports = new HashMap<String, Set<R>>();
         blackboard.publishAdd(completionEvent);
         processingStep = null;
      }
   }

   protected void cancelNodeTimout() {
      if (nodeTimoutAlarm != null) {
         nodeTimoutAlarm.cancel();
         nodeTimoutAlarm = null;
      }
   }

   protected void publishNodeRequestStep(S nextStep) {
      // Submit a request to perform the next step.
      processingStep = nextStep;
      nodeTimoutAlarm = executeLater(defaultNodeTimeout, new NodeTimeout(processingStep));
      failedDuringStep = false;
      NodeRequest<S, C> req = makeNodeRequest(processingStep);
      blackboard.publishAdd(req);
   }

}
