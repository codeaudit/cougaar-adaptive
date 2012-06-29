/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment;

import org.cougaar.test.sequencer.experiment.ExperimentStep;
import org.cougaar.test.sequencer.experiment.ExperimentSteps;

public interface KnodeSteps
      extends ExperimentSteps {
   public static final String LINK_PROPERTY = "link";
   public static final String METRIC_PATH_PROPERTY = "knode_validity_metric_path";
   public static final String METRIC_VALUE_PROPERTY = "knode_validity_metric_value";
   public static final String KNODE_HOPS_PROPERTY = "knode_hops";
   public static final String KNODE_MIN_SLOTS_PROPERTY = "knode_min_slots";
   public static final String KNODE_TOPOLOGY_TYPE_PROPERTY = "knode_topology_type";
   public static final ExperimentStep KNODE_ADD_LINK = new ExperimentStep("KNODE_ADD_LINK");
   public static final ExperimentStep KNODE_DEL_LINK = new ExperimentStep("KNODE_DEL_LINK");
   public static final ExperimentStep KNODE_SET_METRIC = new ExperimentStep("KNODE_SET_METRIC");
   public static final ExperimentStep KNODE_WAIT_METRIC = new ExperimentStep("KNODE_WAIT_METRIC");
}
