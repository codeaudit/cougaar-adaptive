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
 * 
 * A body of code attached to a step that will execute in the sequencer after
 * all the workers for that step have completed successfully. This code runs in
 * an blackboard execute transaction.
 * 
 * If the body needs access to {@link SocietyCompletionEvent} that signaled the
 * completion of the step, it's accessible via {@link #getEvent()}.
 * 
 * @param <S> the step type
 * @param <R> the report type
 */
abstract public class StepBody<S extends ExperimentStep, R extends Report>
      implements Runnable {
   private SocietyCompletionEvent<S, R> event;
   private Properties props;

   public Properties getProps() {
      return props;
   }

   public void setProps(Properties props) {
      this.props = props;
   }

   public SocietyCompletionEvent<S, R> getEvent() {
      return event;
   }

   void setEvent(SocietyCompletionEvent<S, R> event) {
      this.event = event;
   }

}