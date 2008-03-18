package org.cougaar.test.knode.experiment;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;

public abstract class AbstractKnodExpSequencerPlugin 
	extends AbstractExperimentSequencerPlugin<Report> 
	implements KnodeSteps {
	
	private final int SLOW_DOWN = 25;
	
	// 5 hop line from 162->167
	protected void addRestartKnodeSteps() {
		String from = "192.168.162.100";
		String to = "192.168.167.100";
		String path = "IpFlow("+from+"," +to+ "):CapacityMax()";
		addStep(KNODE_SET_METRIC, 0, null, METRIC_PATH_PROPERTY +"="+ path);
        addStep(SOCIETY_READY, 0, null);
        //Basic 5 Hop Line
        addAddLinkSteps("162", "163", SLOW_DOWN);
        addAddLinkSteps("163", "164", SLOW_DOWN);
        addAddLinkSteps("164", "165", SLOW_DOWN);
        addAddLinkSteps("165", "166", SLOW_DOWN);
        addAddLinkSteps("166", "167", SLOW_DOWN);
        //Remove extra TEE Links
        addDeleteLinkSteps("164","162", SLOW_DOWN);
        addDeleteLinkSteps("165","162", SLOW_DOWN);
        addDeleteLinkSteps("166","162", SLOW_DOWN);
        addDeleteLinkSteps("167","162", SLOW_DOWN);
        //Remove extra STAR
        addDeleteLinkSteps("167","163", SLOW_DOWN);
        addDeleteLinkSteps("167","164", SLOW_DOWN);
        addDeleteLinkSteps("167","165", SLOW_DOWN);
        //Remove extra Hairy 1 Line
        addDeleteLinkSteps("163","140", SLOW_DOWN);
        addDeleteLinkSteps("164","143", SLOW_DOWN);
        addDeleteLinkSteps("165","146", SLOW_DOWN);
        addDeleteLinkSteps("166","149", SLOW_DOWN);
        addDeleteLinkSteps("167","152", SLOW_DOWN);
        //Remove extra Hairy 2 Line
        addDeleteLinkSteps("163","141", SLOW_DOWN);
        addDeleteLinkSteps("164","144", SLOW_DOWN);
        addDeleteLinkSteps("165","147", SLOW_DOWN);
        addDeleteLinkSteps("166","150", SLOW_DOWN);
        addDeleteLinkSteps("167","153", SLOW_DOWN);
        //Remove extra Hairy 3 Line
        addDeleteLinkSteps("163","142", SLOW_DOWN);
        addDeleteLinkSteps("164","145", SLOW_DOWN);
        addDeleteLinkSteps("165","148", SLOW_DOWN);
        addDeleteLinkSteps("166","151", SLOW_DOWN);
        addDeleteLinkSteps("167","154", SLOW_DOWN);
        // Wait for topology to settle
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"=30.0");
	}
	
	protected void addMoveLinkSteps(String from, String to, String desiredCapacity) {
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 " + to);
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 162 " +from);
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"="+desiredCapacity);
 	}
	
	protected void addDeleteLinkSteps(String from, String to) {
        addDeleteLinkSteps(from, to,0);
   	}

	protected void addDeleteLinkSteps(String from, String to, int wait) {
        addStep(KNODE_DEL_LINK, wait, null, LINK_PROPERTY+"= "+ from +" "+ to);
   	}

	protected void addAddLinkSteps(String from, String to,int wait) {
        addStep(KNODE_ADD_LINK, wait, null, LINK_PROPERTY+"= "+ from +" "+ to);
   	}
	
	protected void addAddLinkSteps(String from, String to) {
        addAddLinkSteps(from, to, 0);
   	}

}
