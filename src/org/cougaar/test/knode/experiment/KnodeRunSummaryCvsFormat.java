package org.cougaar.test.knode.experiment;

import java.beans.IntrospectionException;
import java.text.DecimalFormat;

import org.cougaar.test.ping.CsvFormat;

public class KnodeRunSummaryCvsFormat extends CsvFormat<KnodeRunSummaryBean> {

	public KnodeRunSummaryCvsFormat() throws IntrospectionException {
		super(KnodeRunSummaryBean.class);
		DecimalFormat format = new DecimalFormat("#0.00");
		DecimalFormat delayFormat = new DecimalFormat("#0.000");
		defineField("pingers", "Pingers", null);
		defineField("sum", "Ping/Sec", format);
		defineField("min", "Min", format);
		defineField("avg", "Avg", format);
		defineField("max", "Max", format);
		defineField("delayMin","DelayMin",delayFormat);
		defineField("delayAvg","DelayAvg",delayFormat);
		defineField("delayMax","DelayAvg",delayFormat);
		defineField("hops", "Hops", null);
		defineField("minSlots", "Min Slots", null);
		defineField("topologyType", "Topology", null);
		defineField("runId", "Run ID", null);
		defineField("suiteId", "Suite", null);
	}

}
