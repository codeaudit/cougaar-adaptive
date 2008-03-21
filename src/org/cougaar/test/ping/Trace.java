package org.cougaar.test.ping;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class Trace implements Statistic<Trace> {
	
	private final Logger log = Logging.getLogger(getClass().getName());
	private final String name;
	
	public Trace(String name) {
		this.name = name;
	}
	 
	public Trace delta(Trace s) {
		// TODO Auto-generated method stub
		return null;
	}

	public void newValue(long value) {
		log.shout("new value" + value);

	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public Trace clone() throws CloneNotSupportedException {
        return (Trace) super.clone();
    }
}
