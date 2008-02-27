/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;


/**
 * Some common ExperimentStep objects used by many {@link ExperimentDescriptor}s.
 */
public interface ExperimentSteps {
    public static final ExperimentStep SOCIETY_READY = new ExperimentStep("Society Ready");
    public static final ExperimentStep SHUTDOWN = new ExperimentStep("Shutdown");
}