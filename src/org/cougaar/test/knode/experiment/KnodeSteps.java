/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment;

import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentSteps;

public interface KnodeSteps extends ExperimentSteps {
    public static final String LINK_PROPERTY = "link";
    public static final ExperimentStep KNODE_ADD_LINK = new ExperimentStep("KNODE_ADD_LINK");
    public static final ExperimentStep KNODE_DEL_LINK = new ExperimentStep("KNODE_DEL_LINK");
 }
