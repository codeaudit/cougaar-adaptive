package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.qos.qrs.SiteAddress;
import org.snmp4j.smi.OID;

/**
 * Figure out the sites we talk to
 *
 */
public class SiteToNeighborFinder  {
	static final OID IP_ROUTE_ENTRY = new OID("1.3.6.1.2.1.4.21.1");
	static final OID IP_ROUTE_NEXT_HOP = append(IP_ROUTE_ENTRY, 7); 
	static final OID IP_ROUTE_MASK = append(IP_ROUTE_ENTRY, 11); 
	private final String[] snmpArgs;
    private Map<SiteAddress, InetAddress> siteToNeighbor;
    
    public SiteToNeighborFinder(String[] snmpArgs) {
    	siteToNeighbor = new HashMap<SiteAddress, InetAddress>();
		this.snmpArgs = snmpArgs;
	}
    
    public Map<SiteAddress, InetAddress> getSiteToNeighbor() {
		return siteToNeighbor;
	}

	public boolean findNeighbors() {
        SynchronousMaskListener masks =
            new SynchronousMaskListener(IP_ROUTE_MASK);
        SimpleSnmpRequest request = new SimpleSnmpRequest(snmpArgs, IP_ROUTE_MASK);
        if (RospfDataFeed.log.isDebugEnabled()) {
        	RospfDataFeed.log.debug("Sending SNMP Poll");
        }
        boolean maskStatus = masks.synchronousWalk(request);
        if (RospfDataFeed.log.isDebugEnabled()) {
        	RospfDataFeed.log.debug("Done SNMP Poll");
        }
         Map<InetAddress, InetAddress> maskMap = masks.getSiteIpToIpMap();  
        SynchronousMaskListener nextHops =
            new SynchronousMaskListener(IP_ROUTE_NEXT_HOP);
        request = new SimpleSnmpRequest(snmpArgs, IP_ROUTE_NEXT_HOP);
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
	
	 private static OID append(OID base, int suffix) {
	        OID extension = (OID) base.clone();
	        extension.append(suffix);
	        return extension;
	    }
}