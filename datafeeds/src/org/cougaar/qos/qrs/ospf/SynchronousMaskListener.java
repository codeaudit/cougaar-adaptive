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

import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 *  Accumulates mappings between two IP addresses during an snmp walk. This is used both
 *  for mapping site IP to mask and for mapping site IP to neighbor.
 */
class SynchronousMaskListener extends SynchronousListener {
    private final Map<InetAddress, InetAddress> siteIpToIp;
    private final OID prefix;
    
    SynchronousMaskListener(OID prefix) {
        siteIpToIp = new HashMap<InetAddress, InetAddress>();
        this.prefix = prefix;
    }
    
    public void walkEvent(VariableBinding[] bindings) {
        for (VariableBinding binding : bindings) {
            OID oid = binding.getOid();
            int offset = prefix.size();
            if (!oid.startsWith(prefix) || oid.size() != offset+4) {
                RospfDataFeed.log.error("Weird response " +oid);
                continue;
            }
            byte[] bytes = oid.toByteArray();
            byte[] addressBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                addressBytes[i] = bytes[offset + i];
            }
            InetAddress neighbor;
            try {
                neighbor = InetAddress.getByAddress(addressBytes);
            } catch (UnknownHostException e) {
                RospfDataFeed.log.error("Can't parse address", e);
                continue;
            }
            Variable var = binding.getVariable();
            if (var instanceof IpAddress) {
                IpAddress addr = (IpAddress) var;
                InetAddress value = addr.getInetAddress();
                siteIpToIp.put(neighbor, value);
            } else {
                RospfDataFeed.log.shout("var is " + var.getClass());
            }
                
        }
    }
    
    public Map<InetAddress, InetAddress> getSiteIpToIpMap() {
        return siteIpToIp;
    }
}