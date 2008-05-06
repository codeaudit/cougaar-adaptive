package org.cougaar.test.sequencer.experiment;

import org.cougaar.core.service.LoggingService;
import org.cougaar.test.sequencer.Report;

public class LoopDescriptor<S extends ExperimentStep, R extends Report>
		extends SubStepDescriptor<S, R> {
	private int loopCount;
	private int maxLoops;

	public LoopDescriptor(int maxLoops) {
		super();
		this.maxLoops = maxLoops;
	}


	public S getNextStep() {
		S subStep = getCurrentStepDescriptor().getNextStep();
		if (subStep != null) {
			// subStep of current step
			return subStep;
		}
		if (++index < steps.size()) {
			// plain next step in a loop sequence
			return getStep();
		}
		if (++loopCount < maxLoops) {
			// last step of a loop sequence
			index = 0;
			return getStep();
		}
		// Done with loops
		return null;
	}
	
	public void logDescription(LoggingService log, StringBuffer buf, int indent) {
		for (int i = 1; i < indent; i++) {
			buf.append(" ");
		}
		buf.append("Loop ").append(maxLoops).append(" times\n");
		for (Descriptor<S, R> descriptor : steps) {
           	descriptor.logDescription(log,buf,indent + 4);
           }
	}
}
