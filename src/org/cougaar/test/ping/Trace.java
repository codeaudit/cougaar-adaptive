package org.cougaar.test.ping;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class Trace implements Statistic<Trace> {
	
	private transient final Logger log = Logging.getLogger(getClass().getName());
	private final String name;
	
	public Trace(String name) {
		reset();
		this.name = name;
	}
	
	public void reset() {
		// TODO Auto-generated method stub

	}

	public void newValue(long value) {
		log.shout("new value" +name + " " + value);

	}

	public Trace delta(Trace s) {
		// TODO Auto-generated method stub
		return null;
	}

	public void accumulate(Trace additionalStatistic) {
		// TODO Auto-generated method stub
		
	}

	public Trace clone() throws CloneNotSupportedException {
        return (Trace) super.clone();
    }

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
