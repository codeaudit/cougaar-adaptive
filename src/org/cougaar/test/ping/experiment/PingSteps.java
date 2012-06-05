/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.ping.experiment;

import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentSteps;

public interface PingSteps
      extends ExperimentSteps {
   public static final String PING_RUN_PROPERTY = "runName";
   public static final String PING_SIZE_PROPERTY = "pingPayloadSize";
   public static final String PING_DELAY_PROPERTY = "interPingDelayMillis";
   public static final String PING_STATISTICS_PROPERTY = "statisticsKind";
   public static final ExperimentStep START_TEST = new ExperimentStep("StartTest");
   public static final ExperimentStep START_STEADY_STATE = new ExperimentStep("StartSteadyStateCollection");
   public static final ExperimentStep END_STEADY_STATE = new ExperimentStep("EndSteadyStateCollection");
   public static final ExperimentStep END_TEST = new ExperimentStep("EndTest");
   public static final ExperimentStep SUMMARY_TEST = new ExperimentStep("Summary");

}
