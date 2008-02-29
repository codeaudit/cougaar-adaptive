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
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class OspfDataFeed extends SimpleQueueingDataFeed implements Constants {
    private static final double CREDIBILITY = SECOND_MEAS_CREDIBILITY;
    private static final Logger log = Logging.getLogger(OspfDataFeed.class);
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    private static final String TRANSFORM_ARG = "--transform=";
    private static final OID ROSPF_METRIC_NEIGHBOR_OID = new OID("1.3.6.1.2.1.14.10.1.12");
    private static final OID IP_ROUTE_ENTRY = new OID("1.3.6.1.2.1.4.21.1");
    private static final OID IP_ROUTE_NEXT_HOP = append(IP_ROUTE_ENTRY, 7); 
    private static final OID IP_ROUTE_MASK = append(IP_ROUTE_ENTRY, 11); 
   
    private long pollPeriodMillis;
    @SuppressWarnings("unused")
    private Map<String, SiteAddress> peerSites;
    private OspfMetricTransform transform;
    private SiteAddress mySite;
    private String[] snmpArgs;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    
    public OspfDataFeed(String[] feedArgs) {
        siteToNeighbor = new HashMap<SiteAddress, InetAddress>();
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
        
        RSSUtils.schedule(new SiteFinder(), 0);
    }
    
    private String makeKey(SiteAddress destination) {
        return "Site" + KEY_SEPR + "Flow" + KEY_SEPR 
        + mySite + KEY_SEPR 
        + destination + KEY_SEPR 
        + "Capacity_Max";
    }
    
    private void pushResults(InetAddress dest, long linkMetric) {
    	boolean foundOne = false;
        for (Map.Entry<SiteAddress, InetAddress> entry : siteToNeighbor.entrySet()) {
            SiteAddress site = entry.getKey();
            InetAddress neighbor = entry.getValue();
            if (dest.equals(neighbor)) {
                String key = makeKey(site);
                DataValue value = new DataValue(transform.toMaxCapacity(linkMetric), CREDIBILITY);
                log.info("Pushing feed key " +key+ " with value " + value);
                newData(key, value, null);
                foundOne = true;
            }
        }
        if (!foundOne) {
			log.info("No site match for next hop " + dest);
		}
    }

    private static OID append(OID base, int suffix) {
        OID extension = (OID) base.clone();
        extension.append(suffix);
        return extension;
    }
    
    private class SynchronousRouteListener extends SynchronousListener {
        private final Map<InetAddress, InetAddress> map;
        private final OID prefix;
        
        SynchronousRouteListener(OID prefix) {
            map = new HashMap<InetAddress, InetAddress>();
            this.prefix = prefix;
        }
        
        public void walkEvent(VariableBinding[] bindings) {
            for (VariableBinding binding : bindings) {
                OID oid = binding.getOid();
                int offset = prefix.size();
                if (!oid.startsWith(prefix) || oid.size() != offset+4) {
                    log.error("Weird response " +oid);
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
                    log.error("Can't parse address", e);
                    continue;
                }
                Variable var = binding.getVariable();
                if (var instanceof IpAddress) {
                    IpAddress addr = (IpAddress) var;
                    InetAddress value = addr.getInetAddress();
                    map.put(neighbor, value);
                } else {
                    log.shout("var is " + var.getClass());
                }
                    
            }
        }
        
        public Map<InetAddress, InetAddress> getMap() {
            return map;
        }
    }
    
    /**
     * Figure out our site, and the sites we talk to
     *
     */
    private final class SiteFinder implements Runnable {
        boolean foundNeighbors;
        
        public void run() {
            // talk snmp to figure out our site
            if (mySite == null && !findSite()) {
                reschedule();
            } else if (!foundNeighbors && !findNeighbors()) {
                reschedule();
            } else {
                // ready to go, start the ospf poller
                RSSUtils.schedule(new NeighborPoller(), 0, pollPeriodMillis);
            }
        }

        private void reschedule() {
            RSSUtils.schedule(this, pollPeriodMillis);
        }
        
        private boolean findNeighbors() {
            SynchronousRouteListener masks =
                new SynchronousRouteListener(IP_ROUTE_MASK);
            SimpleSnmpRequest request = new SimpleSnmpRequest(snmpArgs, IP_ROUTE_MASK);
            boolean maskStatus = masks.synchronousWalk(request);
            Map<InetAddress, InetAddress> maskMap = masks.getMap();
            
            SynchronousRouteListener nextHops =
                new SynchronousRouteListener(IP_ROUTE_NEXT_HOP);
            request = new SimpleSnmpRequest(snmpArgs, IP_ROUTE_NEXT_HOP);
            boolean nextStatus = nextHops.synchronousWalk(request);
            Map<InetAddress, InetAddress> nextMap = nextHops.getMap();
            
            for (Map.Entry<InetAddress, InetAddress> entry : maskMap.entrySet()) {
                InetAddress dest = entry.getKey();
                InetAddress mask = entry.getValue();
                byte[] destBytes = dest.getAddress();
                byte[] maskBytes = mask.getAddress();
                long maskLong = SiteAddress.bytesToLongAddress(maskBytes);
                long destLong = SiteAddress.bytesToLongAddress(destBytes);
                SiteAddress siteAddr = new SiteAddress(destLong, maskLong);
                InetAddress nextHopNeighbor = nextMap.get(dest);
                if (log.isInfoEnabled()) {
					log.info("Site " + siteAddr + " -> " + " next hop "
							+ nextHopNeighbor);
				}
				siteToNeighbor.put(siteAddr, nextHopNeighbor);
            }
            
            return maskStatus && nextStatus;
        }
        
        private boolean findSite() {
            SitesDB sites = RSS.instance().getSitesDB();
            try {
                InetAddress us = InetAddress.getLocalHost();
                mySite = sites.lookup(us.getHostAddress());
                if (log.isInfoEnabled()) {
					log.info("We are " + us + " and our site is " + mySite);
				}
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
                if (oid.startsWith(ROSPF_METRIC_NEIGHBOR_OID)) {
                    int offset = ROSPF_METRIC_NEIGHBOR_OID.size();
                    if (oid.size() == offset+5) {
                        byte[] bytes = oid.toByteArray();
                        byte[] addressBytes = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            addressBytes[i] = bytes[offset + i];
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
                    } else {
                        log.warn(oid.toString() + " is too short");
                    }
                } else {
                    log.warn(oid.toString() +" does not start with " +
                             ROSPF_METRIC_NEIGHBOR_OID.toString());
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
                request.asynchronousWalk(body);
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

}
