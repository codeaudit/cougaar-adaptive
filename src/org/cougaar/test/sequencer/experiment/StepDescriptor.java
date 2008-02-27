/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;

/**
 * A step in an {@link ExperimentDescriptor}.
 * 
 * The {@link #body} is an arbitrary block of code that will run in the
 * sequencer after all the workers have completed successfully.  This
 * code runs in an blackboard execute transaction.
 * 
 * If the {@link #deferMillis} time is positive, the next step is delayed for
 * that many milliseconds.
 * 
 * @param <S> the step type
 * 
 * @param <R> the report type
 */
public class StepDescriptor<S extends ExperimentStep, R extends Report> {
    private final S step;
    private final long deferMillis;
    private final StepBody<S, R> body;
    
    public StepDescriptor(S step, long millis, StepBody<S, R> body) {
        this.step = step;
        this.deferMillis = millis;
        this.body = body;
    }
    
    public S getStep() {
        return step;
    }

    public long getDeferMillis() {
        return deferMillis;
    }
    
    public boolean hasWork() {
        return body  != null;
    }

    public void doWork(SocietyCompletionEvent<S, R> event) {
        if (body != null) {
            body.setEvent(event);
            body.run();
        }
    }
    
}
