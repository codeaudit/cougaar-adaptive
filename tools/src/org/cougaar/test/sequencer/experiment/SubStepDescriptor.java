package org.cougaar.test.sequencer.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;

public abstract class SubStepDescriptor<S extends ExperimentStep, R extends Report>
      implements Descriptor<S, R> {
   protected final List<Descriptor<S, R>> steps;
   protected int index;

   public SubStepDescriptor() {
      steps = new ArrayList<Descriptor<S, R>>();
   }

   public Descriptor<S, R> getCurrentStepDescriptor() {
      return steps.get(index);
   }

   public Properties getProperties() {
      return getCurrentStepDescriptor().getProperties();
   }

   public long getDeferMillis() {
      return getCurrentStepDescriptor().getDeferMillis();
   }

   public boolean hasWork() {
      return getCurrentStepDescriptor().hasWork();
   }

   public void doWork(SocietyCompletionEvent<S, R> event) {
      getCurrentStepDescriptor().doWork(event);
   }

   public S getStep() {
      return getCurrentStepDescriptor().getStep();
   }

   public void addStep(S step, long millis, StepBody<S, R> body, String... propertyPairs) {
      Properties props = new Properties();
      for (String propPair : propertyPairs) {
         String[] pair = propPair.split("=");
         if (pair.length == 2) {
            props.setProperty(pair[0], pair[1]);
         } else {
            // TODO log it
            System.err.println("Skipping \"" + propPair + "\"");
         }
      }
      StepDescriptor<S, R> descriptor = new StepDescriptor<S, R>(step, millis, body, props);
      steps.add(descriptor);
   }

   public void addLoop(LoopDescriptor<S, R> loopDescriptor) {
      steps.add(loopDescriptor);
   }
}
