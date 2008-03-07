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
package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

class NeighborMetricListener extends SynchronousListener {
	// Neighbor IP to # metric
    private final Map<InetAddress, Long> results = new HashMap<InetAddress, Long>();
    
    public NeighborMetricListener() {
	}
    
    public Map<InetAddress, Long> getResults() {
		return results;
	}

	public void walkEvent(VariableBinding[] bindings) {
        for (VariableBinding binding : bindings) {
            OID oid = binding.getOid();
            if (oid.startsWith(RospfDataFeed.ROSPF_METRIC_NEIGHBOR_OID)) {
                int offset = RospfDataFeed.ROSPF_METRIC_NEIGHBOR_OID.size();
                if (oid.size() == offset+5) {
                    byte[] bytes = oid.toByteArray();
                    byte[] addressBytes = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        addressBytes[i] = bytes[offset + i];
                    }
                    try {
                        InetAddress neighborAddress = InetAddress.getByAddress(addressBytes);
                        Variable var = binding.getVariable();
                        long metric = var.toLong();
                        results.put(neighborAddress, metric);
                        if (RospfDataFeed.log.isInfoEnabled()) {
                            RospfDataFeed.log.info(binding.toString());
                        }
                    } catch (UnknownHostException e) {
                        RospfDataFeed.log.error(e.getMessage(), e);
                    }
                } else {
                    RospfDataFeed.log.warn(oid.toString() + " is too short");
                }
            } else {
                RospfDataFeed.log.warn(oid.toString() +" does not start with " +
                         RospfDataFeed.ROSPF_METRIC_NEIGHBOR_OID.toString());
            }
        }
    }
}