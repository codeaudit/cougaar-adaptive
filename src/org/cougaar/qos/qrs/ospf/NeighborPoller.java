package org.cougaar.qos.qrs.ospf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cougaar.qos.qrs.SiteAddress;

/**
 * Send snmp requests for ospf link metric and publish on this data feed as
 * inter-site capacity DataValue.
 * 
 */
class NeighborPoller implements Runnable {
    private SimpleSnmpRequest request;
    private Set<InetAddress> lastNeighbors;
    private final RospfDataFeed dataFeed;
    private final Map<SiteAddress, InetAddress> siteToNeighbor;
    public NeighborPoller(RospfDataFeed dataFeed, 
    		Map<SiteAddress, InetAddress> siteToNeighbor,
    		String[] snmpArgs) {
    	this.dataFeed =dataFeed;
    	this.siteToNeighbor = siteToNeighbor;
    	lastNeighbors = new HashSet<InetAddress>();
        try {
            request = new SimpleSnmpRequest(snmpArgs, RospfDataFeed.ROSPF_METRIC_NEIGHBOR_OID);
        } catch (RuntimeException e1) {
            RospfDataFeed.log.error(e1.getMessage(), e1);
            return;
        }
    }
    
    public Set<InetAddress> updateNeighbors(Set<InetAddress> currentNeighbors) {
    	Set<InetAddress> deletedNeighbors = new HashSet<InetAddress>(lastNeighbors);
    	deletedNeighbors.removeAll(currentNeighbors);
    	lastNeighbors = currentNeighbors;
    	return deletedNeighbors;
    }
    
    public void run() {
        try {
            NeighborMetricListener body = new NeighborMetricListener(this, dataFeed, siteToNeighbor);
            request.asynchronousWalk(body);
        } catch (IOException e) {
            RospfDataFeed.log.error("", e);
        }
    }
}