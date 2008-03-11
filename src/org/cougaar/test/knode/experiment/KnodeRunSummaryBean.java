package org.cougaar.test.knode.experiment;

import java.util.Properties;

import org.cougaar.test.ping.Anova;
import org.cougaar.test.ping.PingRunSummaryBean;

public class KnodeRunSummaryBean extends PingRunSummaryBean implements KnodeSteps {
	private String hops;
	private String minSlots;
	private String topologyType;
	private double avgDelay;
	private double maxDelay;
	private double minDelay;

	public KnodeRunSummaryBean(Anova thrpSummary,Anova delaySummary, Properties props, String suiteName) {
		super(thrpSummary, props, suiteName);
		if (props != null) {
			this.hops = props.getProperty(KNODE_HOPS_PROPERTY);
			this.minSlots  = props.getProperty(KNODE_MIN_SLOTS_PROPERTY);
			this.topologyType=props.getProperty(KNODE_TOPOLOGY_TYPE_PROPERTY);
			this.avgDelay=delaySummary.average();
			this.maxDelay=delaySummary.max();
			this.minDelay=delaySummary.min();
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

	public double getAvgDelay() {
		return avgDelay;
	}

	public double getMaxDelay() {
		return maxDelay;
	}

	public double getMinDelay() {
		return minDelay;
	}

}
