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
 * Created : Aug 9, 2007
 * Workfile: NodeSequencerPlugin.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

abstract public class NodeAggregatorPlugin<S extends Step, R extends Report, C extends Context>
    extends
        TodoPlugin implements SequencerService<S, R, C> {

    private final Map<String, Worker<S, C>> workers;
    private final Map<String, R> workerReports;
    private boolean failedDuringStep = false;
    private Alarm workerTimoutAlarm;
    private S currentStep;
    protected String nodeId;
    
    @Cougaar.ObtainService
    public NodeControlService ncs;
    
    // Todo: How to handle services we provide with annotations
    private ServiceProvider provider;

    @Cougaar.Arg(name = "workerCount", required = true)
    public int workerCount;
    

    // Timeout failure report
    abstract protected R makeWorkerTimoutFailureReport(S step, String reason);

    public NodeAggregatorPlugin() {
        workers = new HashMap<String, Worker<S, C>>();
        workerReports = new HashMap<String, R>();
    }

    protected NodeControlService getNodeControlService() {
        return ncs;
    }

    @Override
   public void load() {
        super.load();
        provider = new MyServiceProvider();
        ServiceBroker rootsb = ncs.getRootServiceBroker();
        // Todo: Root services can not be looked up via reflection
        rootsb.addService(SequencerService.class, provider);
        NodeIdentificationService nis =
                rootsb.getService(this, NodeIdentificationService.class, null);
        nodeId = nis.getMessageAddress().getAddress();
    }

    @Override
   public void unload() {
        if (provider != null) {
            ServiceBroker rootsb = ncs.getRootServiceBroker();
            rootsb.revokeService(SequencerService.class, provider);
        }
        super.unload();
    }
    
     @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeCompletionEvent(WorkerCompletionEvent<S, R> event) {
        R report = event.getReport();
        S step = event.getStep();
        String workerId = event.getWorkerId();
        boolean successful = report.isSuccessful();
        String reason = report.getReason();
        
        if (currentStep != step) {
            // Wrong Step
            log.warn("Worker " + workerId + " reported during wrong step. Ignoring Report:" +
                     " currentStep=" +currentStep+ " reportedStep=" + step +
                     " success=" +successful+ " reason=" + reason );
            return;
        }
        if (workerReports.containsKey(workerId)) {
            // Duplicate Report
            log.warn("Worker " + workerId + " reported multiple times. Ignoring Report:" +
                     " currentStep=" +currentStep+ " reportedStep=" + step +
                     " success=" +successful+ " reason=" + reason );
            return;
        }
        // Timely Report
        workerReports.put(workerId, report);
        if (!successful ) {
            // A Failed Report
            // Publish Node Completion of Step was failure
            failedDuringStep = true;
            Set<R> reports = new HashSet<R>(workerReports.values());
            NodeCompletionEvent<S, R> completionEvent =
                    new NodeCompletionEvent<S, R>(uids, nodeId, step, reports, false);
            blackboard.publishAdd(completionEvent);
            currentStep=null;
            return;
        }
        if (workerReports.size() == workers.size() && !failedDuringStep) {
            // All workers have finished and have reported success
            // Cancel workerTimeout 
            if (workerTimoutAlarm != null) {
                workerTimoutAlarm.cancel();
                workerTimoutAlarm = null;
            }
            // Publish Node Completion of Step was successful
            Set<R> reports = new HashSet<R>(workerReports.values());
            NodeCompletionEvent<S, R> completionEvent =
                    new NodeCompletionEvent<S, R>(uids, nodeId, step, reports, true);
            blackboard.publishAdd(completionEvent);
            currentStep=null;
            return;
        }
    }

    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeNodeRequest(NodeRequest<S, C> request) {
        S step = request.getStep();
        C context = request.getContext();
        int workerTimeout=context.getWorkerTimeout();
        // Initialize State to process step
        currentStep=step;
        workerReports.clear();
        failedDuringStep=false;
        workerTimoutAlarm = executeLater(workerTimeout, new ReportTimeout(step));
        // service callback on all workers;
        for (Worker<S, C> worker : workers.values()) {
            worker.perform(step, context);
        }
    }

    //Todo: need failed Registration Timeout
    @Cougaar.Execute(on = Subscribe.ModType.ADD)
    public void executeRegisterWorker(WorkerRegistrationEvent<S, C> event) {
        String name = event.getWorkerId();
        Worker<S, C> callback = event.getWorker();
        if (workers.size() == workerCount) {
            log.warn("Skipping extra worker " + name);
            return;
        } else if (workers.containsKey(name)) {
            log.warn("Skipping duplicate worker " + name);
            return;
        }
        workers.put(name, callback);
        if ( workers.size() == workerCount) {
            NodeRegistrationEvent evt = new NodeRegistrationEvent(uids, nodeId, workerCount, true);
            publishAddLater(evt);
        }
    }
    
    private final class ReportTimeout implements Runnable {
        private final S step;
        
        public ReportTimeout(S step) {
            this.step = step;
        }
        
        public void run() {
            // Publish Node Completion of Step was failure due to Worker Timeout
            String reason = "Node timed out waiting for workers to complete Step " +step;
            R report = makeWorkerTimoutFailureReport(step, reason);
            log.warn(reason);
            workerReports.put(nodeId, report);
            Set<R> reports = new HashSet<R>(workerReports.values());
             NodeCompletionEvent<S, R> completionEvent =
                    new NodeCompletionEvent<S, R>(uids, nodeId, step, reports, false);
            blackboard.publishAdd(completionEvent);
            currentStep=null;
        }
    }
  

    // Sequencer Service interface
    public void done(String name, S step, R report) {
        WorkerCompletionEvent<S, R> event =
                new WorkerCompletionEvent<S, R>(uids, name, step, report);
        publishAddLater(event);
    }

    // Sequencer Service interface
    public void registerWorker(String name, Worker<S, C> callback) {
        WorkerRegistrationEvent<S, C> event =
                new WorkerRegistrationEvent<S, C>(uids, name, callback);
        publishAddLater(event);
    }
    

    // TODO support service provider with Annotations
    private class MyServiceProvider implements ServiceProvider {
        public MyServiceProvider() {
        }

        public Object getService(ServiceBroker sb, Object req, Class<?> cl) {
            return NodeAggregatorPlugin.this;
        }

        public void releaseService(ServiceBroker arg0, Object arg1, Class<?> arg2, Object arg3) {
        }
    }
}
