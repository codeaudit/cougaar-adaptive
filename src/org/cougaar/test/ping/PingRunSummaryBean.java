/* 
 * <copyright>
 *  
 *  Copyright 2002-2008 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.test.ping;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Properties;

import org.cougaar.core.qos.stats.Anova;
import org.cougaar.test.ping.experiment.PingSteps;

public class PingRunSummaryBean {
	private final Anova summary;
	private double pingSize=0.0;
	private final String runId;
	private final String suiteName;
	
	public PingRunSummaryBean(Anova summary, Properties props, String suiteName) {
		this.summary = summary;
		if (props != null) {
			this.runId = props.getProperty(PingSteps.PING_RUN_PROPERTY);
			String pingSizeString=props.getProperty(PingSteps.PING_SIZE_PROPERTY);
			this.pingSize=pingSizeString == null? 0.0 : Double.parseDouble(pingSizeString);
		} else {
			runId = null;
		}
		this.suiteName = suiteName;
	}

	public String getSuiteId() {
		return suiteName;
	}
	
	public int getPingers() {
		return summary.getValueCount();
	}
	
	public double getMinThrpPerPinger() {
		return summary.min();
	}
	
	public double getMaxThrpPerPinger() {
		return summary.max();
	}
	
	public double getThrpPings() {
		return summary.getSum();
	}
	
	public double getThrpBits() {
		return getThrpPings()*pingSize*8.0;
	}

	public double getAvgThrpPerPinger() {
		return summary.average();
	}
	
	public String getRunId() {
		return runId;
	}
	
	public double getPingSize() {
		return pingSize;
	}
		
	@Override
   public String toString() {
		DecimalFormat fmt = new DecimalFormat("#0.00");
		StringWriter writer = new StringWriter();
		writer.append("Pingers=").append(fmt.format(getPingers()));
		writer.append(" Pings/second=").append(fmt.format(getThrpPings()));
		writer.append(" Min/Avg/Max=").append(fmt.format(getMinThrpPerPinger()));
		writer.append("/").append(fmt.format(getAvgThrpPerPinger()));
		writer.append("/").append(fmt.format(getMaxThrpPerPinger()));
		return writer.toString();
	}
	
}