package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.snmp4j.smi.OID;

/**
 * Send snmp requests for ospf link metrics
 * 
 */
public class NeighborMetricFinder {
	static final OID ROSPF_METRIC_NEIGHBOR_OID = new OID("1.3.6.1.2.1.14.10.1.12");
    private /* final */ SimpleSnmpRequest request;
    private Set<InetAddress> lastNeighbors;
    private Map<InetAddress, Long> results;
    private Set<InetAddress> deletedNeighbors;
    
    public NeighborMetricFinder(String[] snmpArgs) {
    	lastNeighbors = new HashSet<InetAddress>();
        try {
            request = new SimpleSnmpRequest(snmpArgs, ROSPF_METRIC_NEIGHBOR_OID);
        } catch (RuntimeException e1) {
            RospfDataFeed.log.error(e1.getMessage(), e1);
            return;
        }
    }
    
    public Map<InetAddress, Long> getResults() {
		return results;
	}

	public Set<InetAddress> getDeletedNeighbors() {
		return deletedNeighbors;
	}

	public boolean updateNeighborMetrics() {
    	NeighborMetricListener body = new NeighborMetricListener(ROSPF_METRIC_NEIGHBOR_OID);
    	try {
    		body.synchronousWalk(request);
    	} catch (IllegalStateException e) {
    		RospfDataFeed.log.warn("Exception during neighbor metric update: " + e.getMessage());
    		return false;
    	}
    	results = body.getResults();
		Set<InetAddress> currentNeighbors = results.keySet();
		deletedNeighbors = new HashSet<InetAddress>(lastNeighbors);
		deletedNeighbors.removeAll(currentNeighbors);
		lastNeighbors = currentNeighbors;
		return true;
    }
}