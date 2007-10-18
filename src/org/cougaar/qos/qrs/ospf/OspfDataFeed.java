/* 
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.qos.qrs.SitesDB;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class OspfDataFeed extends SimpleQueueingDataFeed implements Constants {
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    private static final String TRANSFORM_ARG = "--transform=";
    private static final OID ROSPF_METRIC_NEIGHBOR_OID = new OID("1.3.6.1.2.1.14.10.1.12");
    private static final Logger log = Logging.getLogger(OspfDataFeed.class);
    
   
    private long pollPeriodMillis;
    @SuppressWarnings("unused")
    private Map<String, SiteAddress> peerSites;
    private OspfMetricTransform transform;
    private SiteAddress mySite;
    private String[] snmpArgs;
    
    public OspfDataFeed(String[] feedArgs) {
        peerSites = new HashMap<String, SiteAddress>();
        List<String> snmpArgList = new LinkedList<String>();
        pollPeriodMillis = 2000;
        transform = new UnityTransform();
        for (String arg : feedArgs) {
            if (arg.startsWith(POLL_PERIOD_ARG)) {
                int beginIndex = POLL_PERIOD_ARG.length();
                pollPeriodMillis = Long.parseLong(arg.substring(beginIndex));
            } else if (arg.startsWith(TRANSFORM_ARG)) {
                int beginIndex = TRANSFORM_ARG.length();
                String classname = arg.substring(beginIndex);
                try {
                    Class<?> transformClass = Class.forName(classname);
                    transform = (OspfMetricTransform) transformClass.newInstance();
                } catch (Exception e) {
                    log.error("Couldn't instantiate transform " +arg);
                }
            } else {
                snmpArgList.add(arg);
            }
        }
        snmpArgs = new String[snmpArgList.size()];
        snmpArgList.toArray(snmpArgs);
        
        String[] testOIDs = {
                "1.3.6.1.2.1.4.21.1.1",
                "1.3.6.1.2.1.4.21.1.7",
                "1.3.6.1.2.1.4.21.1.11",
        };
        
        // Example:
        for (String oidString : testOIDs) {
            SimpleSnmpRequest request = new SimpleSnmpRequest(snmpArgs, new OID(oidString));
            SynchronousLoggingListener listener = new SynchronousLoggingListener();
            log.shout("Starting " + oidString);
            listener.send(request);
            log.shout("Ending " + oidString);
        }
        
        RSSUtils.schedule(new SiteFinder(), 0);
    }
    
    private String makeKey(SiteAddress destination) {
        return "Site" + KEY_SEPR + "Flow" + KEY_SEPR 
        + mySite + KEY_SEPR 
        + destination + KEY_SEPR 
        + "Capacity_Max";
    }
    
    private void pushResults(InetAddress dest, long linkMetric) {
        // XXX: where is the net mask?
        SiteAddress destination = SiteAddress.getSiteAddress(dest.getHostAddress());
        String key = makeKey(destination);
        DataValue value = new DataValue(transform.toMaxCapacity(linkMetric), 2.0);
        newData(key, value, null);
    }
    
    private class SynchronousLoggingListener extends SynchronousListener {
        public void walkEvent(VariableBinding[] bindings) {
            for (VariableBinding binding : bindings) {
                log.shout(binding.toString());
            }
        }
    }
    
    /**
     * Figure out our site, and the sites we talk to
     *
     */
    private final class SiteFinder implements Runnable {
        public void run() {
            // talk snmp to figure out our site
            if (findSite()) {
                // ready to go, start the ospf poller
                RSSUtils.schedule(new NeighborPoller(), 0, pollPeriodMillis);
            } else {
                // try again
                RSSUtils.schedule(this, pollPeriodMillis);
            }
        }
        
        private boolean findSite() {
            SitesDB sites = RSS.instance().getSitesDB();
            try {
                InetAddress us = InetAddress.getLocalHost();
                mySite = sites.lookup(us.getHostAddress());
                return true;
            } catch (UnknownHostException e) {
                log.error("Localhost is unknown");
                return false;
            }
        }
    }
    
    private final class NeighborMetricListener implements WalkListener {
        private final Map<InetAddress, Long> results = new HashMap<InetAddress, Long>();
        
        public void walkEvent(VariableBinding[] bindings) {
            for (VariableBinding binding : bindings) {
                OID oid = binding.getOid();
                int offset = ROSPF_METRIC_NEIGHBOR_OID.size();
                byte[] addressBytes = new byte[4];
                for (int i=0; i<4; i++) {
                    addressBytes[i] = (byte) oid.get(offset+i);
                }
                try {
                    InetAddress address = InetAddress.getByAddress(addressBytes);
                    Variable var = binding.getVariable();
                    long metric = var.toLong();
                    results.put(address, metric);
                    if (log.isInfoEnabled()) {
                        log.info(binding.toString());
                    }
                } catch (UnknownHostException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        public void walkCompletion(boolean success) {
            for (Map.Entry<InetAddress, Long> entry : results.entrySet()) {
                pushResults(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Send snmp requests for ospf link metric and publish on this data feed as
     * inter-site capacity DataValue.
     * 
     */
    private final class NeighborPoller implements Runnable {
        private SimpleSnmpRequest request;
        
        public NeighborPoller() {
            try {
                request = new SimpleSnmpRequest(snmpArgs, ROSPF_METRIC_NEIGHBOR_OID);
            } catch (RuntimeException e1) {
                log.error(e1.getMessage(), e1);
                return;
            }
        }
        
        public void run() {
            try {
                NeighborMetricListener body = new NeighborMetricListener();
                request.send(body);
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

}
