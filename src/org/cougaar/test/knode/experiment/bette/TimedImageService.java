package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.component.ServiceBroker;

/*
 * Service which supplies images for a specific time. 
 * 
 * Plugins implementing this service should make the service available, before start time
 * and expect calls at setup subscription time. 
 * 
 * Other plugins in the agent can access the image service 
 * via the service broker and not the blackboard. 
 * 
 * NB: The {@link ServiceBroker} api doesn't support generic
 * services yet, so for now a given broker (agent) can only have one
 * TimedImageService registered.
 */

public interface TimedImageService {
		
	/*
	 * Supply an image for a specific time
	 * 
	 * @param time  Time of the image in same units as System.getTimeMillis()
	 */
	public byte[] getImage(long time);
	
}
