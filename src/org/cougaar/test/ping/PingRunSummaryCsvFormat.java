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

import java.beans.IntrospectionException;
import java.text.DecimalFormat;

import org.cougaar.core.qos.stats.CsvFormat;

/**
 *
 */
public class PingRunSummaryCsvFormat extends CsvFormat<PingRunSummaryBean> {
	public PingRunSummaryCsvFormat() throws IntrospectionException {
		super(PingRunSummaryBean.class);
		DecimalFormat format = new DecimalFormat("#0.00");
		defineField("pingers", "Pingers", null);
		defineField("thrpPings", "Ping/Sec", format);
		defineField("thrpBits","Bits/Sec",format);
		defineField("minThrpPerPinger", "MinPingerThrp", format);
		defineField("avgThrpPerPinger", "AvgPingerThrp", format);
		defineField("maxThrpPerPinger", "MaxPingerThrp", format);
		defineField("runId", "Run ID", null);
		defineField("suiteId", "Suite", null);
	}

}
