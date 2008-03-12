package org.cougaar.test.knode.experiment;

import org.cougaar.test.sequencer.Report;
import org.cougaar.test.sequencer.experiment.AbstractExperimentSequencerPlugin;

public abstract class AbstractKnodExpSequencerPlugin 
	extends AbstractExperimentSequencerPlugin<Report> 
	implements KnodeSteps {
	
	// 5 hop line from 162->167
	protected void addRestartKnodeSteps() {
		String from = "192.168.162.100";
		String to = "192.168.167.100";
		String path = "IpFlow("+from+"," +to+ "):CapacityMax()";
		addStep(KNODE_SET_METRIC, 0, null, METRIC_PATH_PROPERTY +"="+ path);
        addStep(SOCIETY_READY, 0, null);
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 163");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 163 164");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 164 165");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 165 166");
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 166 167");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 164 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 165 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 166 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 162");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 163");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 164");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 165");
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 167 163");
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"=30.0");
	}
	
	protected void addMoveLinkSteps(String from, String to, String desiredCapacity) {
        addStep(KNODE_ADD_LINK, 0, null, LINK_PROPERTY+"= 162 " + to);
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= 162 " +from);
        addStep(KNODE_WAIT_METRIC, 0, null, METRIC_VALUE_PROPERTY+"="+desiredCapacity);
 	}
	
	protected void addDeleteLinkSteps(String from, String to) {
        addStep(KNODE_DEL_LINK, 0, null, LINK_PROPERTY+"= "+ from +" "+ to);
   	}

	protected void addAddLinkSteps(String from, String to,int wait) {
        addStep(KNODE_ADD_LINK, wait, null, LINK_PROPERTY+"= "+ from +" "+ to);
   	}
	

}
