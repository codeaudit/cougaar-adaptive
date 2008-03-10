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

import java.net.InetAddress;
import java.net.UnknownHostException;
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

public class RospfDataFeed extends SimpleQueueingDataFeed implements Constants {
	static final Logger log = Logging.getLogger(RospfDataFeed.class);
	static final OID IP_ROUTE_ENTRY = new OID("1.3.6.1.2.1.4.21.1");
	static final OID ROSPF_METRIC_NEIGHBOR_OID = new OID("1.3.6.1.2.1.14.10.1.12");
	static final OID IP_ROUTE_NEXT_HOP = append(IP_ROUTE_ENTRY, 7); 
	static final OID IP_ROUTE_MASK = append(IP_ROUTE_ENTRY, 11); 
	
    private static final double CREDIBILITY = SECOND_MEAS_CREDIBILITY;
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    private static final String TRANSFORM_ARG = "--transform=";
   
    private long pollPeriodMillis;
    private OspfMetricTransform transform;
    private String[] snmpArgs;
    private SiteAddress mySite;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    private NeighborMetricFinder neighborPoller;
    
    public RospfDataFeed(String transformClassName, long pollPeriod, String[] snmpArgs) {
        pollPeriodMillis = pollPeriod;
        this.snmpArgs = snmpArgs;
        neighborPoller = new NeighborMetricFinder(snmpArgs);
        try {
            Class<?> transformClass = Class.forName(transformClassName);
            transform = (OspfMetricTransform) transformClass.newInstance();
        } catch (Exception e) {
            log.error("Couldn't instantiate transform " +transformClassName);
        }
        RSSUtils.schedule(new Poller(), 0);
    }
    
    /**
     * This is the standard QRS datafeed constructor
     * 
     * @param feedArgs
     */
    public RospfDataFeed(String[] feedArgs) {
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
        
        RSSUtils.schedule(new Poller(), 0);
    }
    
    private String makeKey(SiteAddress destination) {
        return "Site" + KEY_SEPR + "Flow" + KEY_SEPR 
        + mySite + KEY_SEPR 
        + destination + KEY_SEPR 
        + "Capacity_Max";
    }
    
    boolean findMySite() {
    	if (mySite != null) {
    		return true;
    	}
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
    
    void publishNeighborToSites(InetAddress walkNeighbor, long linkMetric) {
    	boolean foundOne = false;
        for (Map.Entry<SiteAddress, InetAddress> entry : siteToNeighbor.entrySet()) {
            SiteAddress site = entry.getKey();
            InetAddress neighbor = entry.getValue();
            if (walkNeighbor.equals(neighbor)) {
                String key = makeKey(site);
                DataValue value = new DataValue(transform.toMaxCapacity(linkMetric), CREDIBILITY);
                log.info("Pushing feed key " +key+ " with value " + value);
                newData(key, value, null);
                foundOne = true;
            }
        }
        if (!foundOne) {
			log.info("No site match for next hop " + walkNeighbor);
		}
    }
    
    private static OID append(OID base, int suffix) {
        OID extension = (OID) base.clone();
        extension.append(suffix);
        return extension;
    }
    
    private void publishNeighborMetrics() {
    	for (InetAddress deletedNeighbor : neighborPoller.getDeletedNeighbors()) {
    		publishNeighborToSites(deletedNeighbor, Long.MAX_VALUE);
    	}
    	
        for (Map.Entry<InetAddress, Long> entry : neighborPoller.getResults().entrySet()) {
            InetAddress activeNeighbor = entry.getKey();
			Long metric = entry.getValue();
			publishNeighborToSites(activeNeighbor, metric);
        }
    }
    
    private class Poller implements Runnable {
    	private final SiteToNeighborFinder sf = new SiteToNeighborFinder(snmpArgs);
    	
    	public void run() {
            // initialization
            if (mySite == null) {
            	log.info("Finding myself again");
            	if (!findMySite()) {
            		reschedule();
            		return;
            	}
            } 
            if (siteToNeighbor == null) {
            	log.info("Finding my neighbors again");
            	if (sf.findNeighbors()) {
            		siteToNeighbor = sf.getSiteToNeighbor();
            	} else {
            		reschedule();
            		return;
            	}
            }
            
            // Periodic poll once we're initialized
            if (neighborPoller.updateNeighborMetrics()) {
            	publishNeighborMetrics();
            }
            reschedule();
        }
    	
    	private void reschedule() {
            RSSUtils.schedule(this, pollPeriodMillis);
        }
    }

}
