/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class ExperimentDescriptor<S extends ExperimentStep, R extends Report> 
extends SubStepDescriptor<S,R>{
    
    public ExperimentDescriptor() {
        super();
    }
    
    public S initializeExperiment() {
        index = 0;
        return getStep();
    }
  
    public S getNextStep() {
		S subStep = getCurrentStepDescriptor().getNextStep();
		if (subStep != null) {
			return subStep;
		}
		if (++index < steps.size()) {
			return getStep();
		}
		return null;
	}
    
    public void logDescription(LoggingService log, StringBuffer buf, int indent) {
    	for (Descriptor<S, R> descriptor : steps) {
    		descriptor.logDescription(log,buf,0);
    	}
    }
    
    public void logDescription(LoggingService log) {
    	if (log.isInfoEnabled()) {
    		StringBuffer buf = new StringBuffer("Experiment description \n\n");
    		logDescription(log, buf, 0);
    		log.info(buf.toString());
    	}
    }
    
}
