/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cougaar.core.service.LoggingService;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;

/**
 * Specification of experiment as an ordered sequence of {@link StepDescriptor}.
 * 
 * @param <S> the step type
 * @param <R> the report type
 * 
 */
public class ExperimentDescriptor<S extends ExperimentStep, R extends Report> {
    private final List<StepDescriptor<S, R>> steps;
    private int index;
    
    public ExperimentDescriptor() {
        steps = new ArrayList<StepDescriptor<S, R>>();
    }
    
    public void addStep(S step, long millis, StepBody<S, R> body, String... propertyPairs) {
        Properties props = new Properties();
        for (String propPair : propertyPairs) {
            String[] pair = propPair.split("=");
            if (pair.length == 2) {
                props.setProperty(pair[0], pair[1]);
            } else {
                // TODO log it
                System.err.println("Skipping \"" + propPair+ "\"");
            }
        }
        StepDescriptor<S, R> descriptor = new StepDescriptor<S,R>(step, millis, body, props);
        steps.add(descriptor);
    }
    
    public StepDescriptor<S, R> getCurrentDescriptor() {
        return steps.get(index);
    }
    
    public Properties getCurrentProperties() {
        return getCurrentDescriptor().getProperties();
    }
    
    public long getCurrentDelay() {
        return getCurrentDescriptor().getDeferMillis();
    }
    
    public void runCurrentBody(SocietyCompletionEvent<S, R> event) {
        getCurrentDescriptor().doWork(event);
    }
    
    public S getCurrentStep() {
        return getCurrentDescriptor().getStep();
    }
    
    public S initializeExperiment() {
        index = 0;
        return getCurrentStep();
    }
    
    public S getNextStep() {
        if (++index < steps.size()) {
            return getCurrentStep();
        } else {
            return null;
        }
    }
    
    public void logDescription(LoggingService log) {
        if (log.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer("Experiment description \n\n");
            for (StepDescriptor<S, R> descriptor : steps) {
                boolean hasWork = descriptor.hasWork();
                S step = descriptor.getStep();
                long deferMillis = descriptor.getDeferMillis();
                buf.append(step).append(", ").append(deferMillis);
                if (hasWork) {
                    buf.append(" with work");
                }
                buf.append('\n');
            }
            log.info(buf.toString());
        }
    }
    
}
