package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SiteAddress;

/**
 * Figure out our site, and the sites we talk to
 *
 */
class SiteFinder implements Runnable {
	private final RospfDataFeed dataFeed;
	private final long pollPeriodMillis;
	private final String[] snmpArgs;
    private boolean foundNeighbors;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    
    public SiteFinder(RospfDataFeed dataFeed, long pollPeriodMillis, String[] snmpArgs) {
    	siteToNeighbor = new HashMap<SiteAddress, InetAddress>();
		this.dataFeed = dataFeed;
		this.pollPeriodMillis = pollPeriodMillis;
		this.snmpArgs = snmpArgs;
	}
    
    public void run() {
        // talk snmp to figure out our site
        if (!dataFeed.findMySite()) {
            reschedule();
        } else if (!foundNeighbors && !findNeighbors()) {
            reschedule();
        } else {
            // ready to go, start the ospf poller
            RSSUtils.schedule(new NeighborPoller(dataFeed, siteToNeighbor, snmpArgs), 0, pollPeriodMillis);
        }
    }

    private void reschedule() {
        RSSUtils.schedule(this, pollPeriodMillis);
    }
    
    private boolean findNeighbors() {
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