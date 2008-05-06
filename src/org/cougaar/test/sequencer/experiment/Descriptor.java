package org.cougaar.test.sequencer.experiment;

import java.util.Properties;

import org.cougaar.core.service.LoggingService;
import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.SocietyCompletionEvent;

public interface Descriptor<S extends ExperimentStep, R extends Report> {

	public S getStep();

	public long getDeferMillis();

	public Properties getProperties();

	public boolean hasWork();

	public void doWork(SocietyCompletionEvent<S, R> event);
	
	public S getNextStep();
	
	public void logDescription(LoggingService log, StringBuffer buf, int indent);
	
}
