package org.cougaar.test.knode.experiment;

import java.beans.IntrospectionException;
import java.text.DecimalFormat;

import org.cougaar.core.qos.stats.CsvFormat;

public class KnodeRunSummaryCvsFormat extends CsvFormat<KnodeRunSummaryBean> {

	public KnodeRunSummaryCvsFormat() throws IntrospectionException {
		super(KnodeRunSummaryBean.class);
		DecimalFormat format = new DecimalFormat("#0.00");
		DecimalFormat delayFormat = new DecimalFormat("#0.0");
		defineField("pingers", "Pingers", null);
		defineField("thrpPings", "Ping/Sec", format);
		defineField("thrpBits","Bits/Sec",format);
		//defineField("minThrpPerPinger", "MinPingerThrp", format);
		//defineField("avgThrpPerPinger", "AvgPingerThrp", format);
		//defineField("maxThrpPerPinger", "MaxPingerThrp", format);
		defineField("pingCount","Pings",format);
		defineField("minDelay","MinDelay",delayFormat);
		defineField("avgDelay","AvgDelay",delayFormat);
		defineField("maxDelay","MaxDelay",delayFormat);
		defineField("pingSize","PingSize",delayFormat);
		defineField("hops", "Hops", null);
		defineField("minSlots", "Min Slots", null);
		defineField("topologyType", "Topology", null);
		defineField("runId", "Run ID", null);
		defineField("suiteId", "Suite", null);
	}

}
