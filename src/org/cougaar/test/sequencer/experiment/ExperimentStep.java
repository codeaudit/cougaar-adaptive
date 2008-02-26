/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import org.cougaar.test.sequencer.Step;


/**
 *
 */
public class ExperimentStep implements Step {
    private final String name;
    
    public ExperimentStep(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
}
