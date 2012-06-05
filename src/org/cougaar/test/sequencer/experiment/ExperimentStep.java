/*
 *
 * Copyright 2008 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.sequencer.experiment;

import org.cougaar.test.sequencer.Step;

/**
 * A named step in an experiment.
 */
public class ExperimentStep
      implements Step {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final String name;

   public ExperimentStep(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   @Override
   public String toString() {
      return name;
   }

   @Override
   public boolean equals(Object x) {
      return x instanceof ExperimentStep && ((ExperimentStep) x).name.equals(name);
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }
}
