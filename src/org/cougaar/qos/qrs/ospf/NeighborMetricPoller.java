package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Send snmp requests for ospf link metric and publish on this data feed as
 * inter-site capacity DataValue.
 * 
 */
class NeighborMetricPoller implements Runnable {
    private SimpleSnmpRequest request;
    private Set<InetAddress> lastNeighbors;
    private final RospfDataFeed dataFeed;
    
    public NeighborMetricPoller(RospfDataFeed dataFeed, String[] snmpArgs) {
    	this.dataFeed = dataFeed;
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
    	NeighborMetricListener body = new NeighborMetricListener();
    	body.synchronousWalk(request);
    	Map<InetAddress, Long> results = body.getResults();
    	
    	Set<InetAddress> deletedNeighbors = updateNeighbors(results.keySet());
    	for (InetAddress deletedNeighbor : deletedNeighbors) {
    		dataFeed.publishNeighborToSites(deletedNeighbor, Long.MAX_VALUE);
    	}
    	
        for (Map.Entry<InetAddress, Long> entry : results.entrySet()) {
            InetAddress activeNeighbor = entry.getKey();
			Long metric = entry.getValue();
			dataFeed.publishNeighborToSites(activeNeighbor, metric);
        }
    }
}