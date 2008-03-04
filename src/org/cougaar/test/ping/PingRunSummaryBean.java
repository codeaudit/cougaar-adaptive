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

import java.util.Properties;

import org.cougaar.test.knode.experiment.KnodeDiffServSequencerPlugin;

public class PingRunSummaryBean {
	private final Anova summary;
	private final String runId;
	private final String suiteName;
	
	public PingRunSummaryBean(Anova summary, Properties props, String suiteName) {
		this.summary = summary;
		if (props != null) {
			this.runId = props.getProperty(KnodeDiffServSequencerPlugin.PING_RUN_PROPERTY);
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
	
	public double getMin() {
		return summary.min();
	}
	
	public double getMax() {
		return summary.max();
	}
	
	public double getSum() {
		return summary.getSum();
	}
	
	public double getAvg() {
		return summary.average();
	}
	
	public String getRunId() {
		return runId;
	}
	
	public String toString() {
		return "Pingers=" + getPingers() +
		" Pings/second=" + getSum()+
		" Min/Avg/Max=" +getMin()+ "/" 
		+getAvg() + "/" + getMax();
	}
	
}