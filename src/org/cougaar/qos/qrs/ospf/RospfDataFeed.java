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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class RospfDataFeed extends SimpleQueueingDataFeed implements Constants {
	private static final double CREDIBILITY = SECOND_MEAS_CREDIBILITY;
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    private static final String TRANSFORM_ARG = "--transform=";
   
    protected static final Logger log = Logging.getLogger(RospfDataFeed.class);
    private long pollPeriodMillis;
    private OspfMetricTransform transform;
    private String[] snmpArgs;
    private SiteAddress mySite;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    
    private MySiteFinder mySiteFinder = new MySiteFinder();
	private SiteToNeighborFinder siteNeighborFinder;
	private NeighborMetricFinder neighborMetricFinder;
	
    public RospfDataFeed(String transformClassName, long pollPeriod, 
    		String community, String version, InetAddress router) {
        pollPeriodMillis = pollPeriod;
        this.snmpArgs = makeSnmpArgs(community, version, router);
        try {
            Class<?> transformClass = Class.forName(transformClassName);
            transform = (OspfMetricTransform) transformClass.newInstance();
        } catch (Exception e) {
            log.error("Couldn't instantiate transform " +transformClassName);
        }
    }

	protected void startPolling() {
		siteNeighborFinder = makeSiteToNeighborFinder();
        neighborMetricFinder  = makeNeighborMetricFinder();
        RSSUtils.schedule(new Poller(), 7000); //delay start
	}

    /**
     * This is an older version that uses standard QRS datafeed constructor.
     * Society xml must provide the all the right feed arguments.
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
        
        RSSUtils.schedule(new Poller(), 7000); // delay start
    }
    
    protected String makeKey(SiteAddress destination) {
        return "Site" + KEY_SEPR + "Flow" + KEY_SEPR 
        + mySite + KEY_SEPR 
        + destination + KEY_SEPR 
        + "Capacity_Max";
    }
    
    /**
     * Find the neighbor from the right neigborToMetric table.  Defaults
     * to the given neighbor.  Subclasses can override to map through
     * other router data.
     */
    protected InetAddress findMeasuredNeighbor(InetAddress neighbor) {
    	return neighbor;
    }
    
    protected void publishNeighborToSites(InetAddress walkNeighbor, long linkMetric) {
    	boolean foundOne = false;
        for (Map.Entry<SiteAddress, InetAddress> entry : siteToNeighbor.entrySet()) {
            SiteAddress site = entry.getKey();
            InetAddress neighbor = entry.getValue();
            InetAddress mappedNeighbor = findMeasuredNeighbor(neighbor);
            if (mappedNeighbor != null && walkNeighbor.equals(mappedNeighbor)) {
            	pushData(site, linkMetric);
                foundOne = true;
            }
        }
        if (!foundOne) {
			log.debug("No site match for next hop " + walkNeighbor);
		}
    }

	protected void pushData(SiteAddress site, long linkMetric) {
		String key = makeKey(site);
		DataValue value = new DataValue(transform.toMaxCapacity(linkMetric), CREDIBILITY);
		if (log.isDebugEnabled()){
			log.debug("Pushing feed key " +key+ " with value " + value);
		}
		newData(key, value, null);
	}
    
    protected void publishNeighborMetrics(Map<InetAddress, Long> metrics,
    		Set<InetAddress> deletedNeighbors) {
    	for (InetAddress deletedNeighbor : deletedNeighbors) {
    		publishNeighborToSites(deletedNeighbor, Long.MAX_VALUE); // site unreachable
    	}
    	
        for (Map.Entry<InetAddress, Long> entry : metrics.entrySet()) {
            InetAddress activeNeighbor = entry.getKey();
			Long metric = entry.getValue();
			publishNeighborToSites(activeNeighbor, metric);
        }
    }
    
    protected String[] makeSnmpArgs(String community, String version, InetAddress router) {
    	return new String[] {
    			"-c",
    			community,
    			"-v",
    			version,
    			router.getHostAddress(),
    	};
    }
    
    protected SiteToNeighborFinder makeSiteToNeighborFinder() {
    	return new SiteToNeighborFinder(snmpArgs);
    }
    
    protected NeighborMetricFinder makeNeighborMetricFinder() {
		return new NeighborMetricFinder(snmpArgs);
	}

    
    protected boolean findMySite() {
		if (mySite != null) {
			return true;
		}
		log.info("Finding myself");
		if (mySiteFinder.findMySite()) {
			mySite = mySiteFinder.getMySite();
			return true;
		} else {
			return false;
		} 
	}
    
    protected boolean findMyNeighbors() {
    	if (siteToNeighbor != null) {
    		return true;
    	}
    	log.info("Finding my neighbors");
    	if (siteNeighborFinder.findNeighbors()) {
    		siteToNeighbor = siteNeighborFinder.getSiteToNeighbor();
    		return true;
    	} else {
    		return false;
    	}
    }
    
    protected void collectNeighborMetrics() {
    	if (neighborMetricFinder.updateNeighborMetrics()) {
        	Map<InetAddress, Long> metrics = neighborMetricFinder.getResults();
        	Set<InetAddress> deletedNeighbors = neighborMetricFinder.getDeletedNeighbors();
			publishNeighborMetrics(metrics, deletedNeighbors);
        }
    }

	private class Poller implements Runnable {
    	public void run() {
            // initialization
            if (!findMySite()) {
            	// no luck, try to get my site again later
            	reschedule();
            	return;
            }
            if (!findMyNeighbors()) {
            	// no luck, try to get my Neighbors again later
        		reschedule();
        		return;
            }
            
            // Periodic poll for next hop metrics once we're initialized
            collectNeighborMetrics();
            reschedule();
        }
    	
    	private void reschedule() {
            RSSUtils.schedule(this, pollPeriodMillis);
        }
    }

}
