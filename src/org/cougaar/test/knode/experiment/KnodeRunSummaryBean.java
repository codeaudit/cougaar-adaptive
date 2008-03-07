package org.cougaar.test.knode.experiment;

import java.util.Properties;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.PingRunSummaryBean;

public class KnodeRunSummaryBean extends PingRunSummaryBean implements KnodeSteps {
	private String hops;
	private String minSlots;
	private String topologyType;

	public KnodeRunSummaryBean(Anova summary, Properties props, String suiteName) {
		super(summary, props, suiteName);
		if (props != null) {
			this.hops = props.getProperty(KNODE_HOPS_PROPERTY);
			this.minSlots  = props.getProperty(KNODE_MIN_SLOTS_PROPERTY);
			this.topologyType=props.getProperty(KNODE_TOPOLOGY_TYPE_PROPERTY);
		} else {
			hops = null;
			minSlots=null;
			topologyType=null;
		}
	}

	public String getHops() {
		return hops;
	}

	public String getMinSlots() {
		return minSlots;
	}

	public String getTopologyType() {
		return topologyType;
	}

}
