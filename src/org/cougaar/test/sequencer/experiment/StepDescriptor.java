/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import java.util.Properties;

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
    private final Properties properties;
    
    public StepDescriptor(S step, long millis, StepBody<S, R> body, Properties properties) {
        this.step = step;
        this.deferMillis = millis;
        this.body = body;
        this.properties = properties;
    }
    
    public S getStep() {
        return step;
    }

    public long getDeferMillis() {
        return deferMillis;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public boolean hasWork() {
        return body  != null;
    }

    public void doWork(SocietyCompletionEvent<S, R> event) {
        if (body != null) {
            body.setEvent(event);
            body.setProps(properties);
            body.run();
        }
    }
    
}
