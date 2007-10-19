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
 * Created : Aug 8, 2007
 * Workfile: Tester.java
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.sequencer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.util.annotations.Cougaar;

abstract public class WorkerPlugin<S extends Step, R extends Report, C extends Context>
        extends TodoPlugin {
    private SequencerService<S, R, C> sequencerService;

    @Cougaar.Arg(name = "workerId", required = true, description = "Society unique id")
    public String workerId;

    abstract protected void doStep(S step, C context);
    abstract protected R makeReport(S step);
    
    protected void stepCompeleted(S step, R report) {
        sequencerService.done(workerId, step, report);
    }
    
    protected void setupSubscriptions() {
        super.setupSubscriptions();
        
        // FIXME: Component infrastructure should support reflective service discovery of root services.
        ServiceBroker sb = getServiceBroker();
        @SuppressWarnings("unchecked") // declare the local var to suppress the warning
        SequencerService<S, R, C> svc = sb.getService(this, SequencerService.class, null);
        if (svc == null) {
            log.error("Sequencer service not available at setupSubscriptions");
            return;
        }
        sequencerService = svc;
        
        // Defer registration until setupSubscription
        // in order to ensure that it happens on an
        // Agent thread.
        sequencerService.registerWorker(workerId, new DefaultWorker());
    }

    private final class DefaultWorker implements Worker<S, C> {
        public void perform(S step, C condition) {
            final WorkRequest<S, C> event = new WorkRequest<S, C>(uids, step, condition);
            executeLater(new Runnable() {
                public void run() {
                    doStep(event.getStep(), event.getContext());
                }
            });
        }
    }
}
