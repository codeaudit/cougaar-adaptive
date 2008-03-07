package org.cougaar.qos.qrs.ospf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Send snmp requests for ospf link metric and publish on this data feed as
 * inter-site capacity DataValue.
 * 
 */
class NeighborPoller implements Runnable {
    private SimpleSnmpRequest request;
    private Set<InetAddress> lastNeighbors;
    private final RospfDataFeed dataFeed;
    
    public NeighborPoller(RospfDataFeed dataFeed, String[] snmpArgs) {
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
        try {
            NeighborMetricListener body = new NeighborMetricListener(this, dataFeed);
            request.asynchronousWalk(body);
        } catch (IOException e) {
            RospfDataFeed.log.error("", e);
        }
    }
}