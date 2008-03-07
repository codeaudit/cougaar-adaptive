package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.qos.qrs.SiteAddress;

/**
 * Figure out the sites we talk to
 *
 */
class SiteToNeighborFinder  {
	private final String[] snmpArgs;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    
    public SiteToNeighborFinder(String[] snmpArgs) {
    	siteToNeighbor = new HashMap<SiteAddress, InetAddress>();
		this.snmpArgs = snmpArgs;
	}
    
     public Map<SiteAddress, InetAddress> getSiteToNeighbor() {
		return siteToNeighbor;
	}

	boolean findNeighbors() {
        SynchronousMaskListener masks =
            new SynchronousMaskListener(RospfDataFeed.IP_ROUTE_MASK);
        SimpleSnmpRequest request = new SimpleSnmpRequest(snmpArgs, RospfDataFeed.IP_ROUTE_MASK);
        boolean maskStatus = masks.synchronousWalk(request);
        Map<InetAddress, InetAddress> maskMap = masks.getSiteIpToIpMap();
        
        SynchronousMaskListener nextHops =
            new SynchronousMaskListener(RospfDataFeed.IP_ROUTE_NEXT_HOP);
        request = new SimpleSnmpRequest(snmpArgs, RospfDataFeed.IP_ROUTE_NEXT_HOP);
        boolean nextStatus = nextHops.synchronousWalk(request);
        Map<InetAddress, InetAddress> nextMap = nextHops.getSiteIpToIpMap();
        
        for (Map.Entry<InetAddress, InetAddress> entry : maskMap.entrySet()) {
            InetAddress dest = entry.getKey();
            InetAddress mask = entry.getValue();
            byte[] destBytes = dest.getAddress();
            byte[] maskBytes = mask.getAddress();
            long maskLong = SiteAddress.bytesToLongAddress(maskBytes);
            long destLong = SiteAddress.bytesToLongAddress(destBytes);
            SiteAddress siteAddr = new SiteAddress(destLong, maskLong);
            InetAddress nextHopNeighbor = nextMap.get(dest);
            if (RospfDataFeed.log.isInfoEnabled()) {
				RospfDataFeed.log.info("Site " + siteAddr + " -> " + " next hop "
						+ nextHopNeighbor);
			}
			siteToNeighbor.put(siteAddr, nextHopNeighbor);
        }
        
        return maskStatus && nextStatus;
    }
}