package org.cougaar.test.knode.experiment;

import java.net.InetAddress;
import java.util.Map;

import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.qos.qrs.ospf.NeighborMetricFinder;
import org.cougaar.qos.qrs.ospf.RospfDataFeed;
import org.cougaar.qos.qrs.ospf.SiteToNeighborFinder;

public class BlackRospfDataFeed extends RospfDataFeed {
	private final String[] blackSnmpArgs;
	private SiteToNeighborFinder blackSiteNeighborFinder, redSiteToNeighborFinder;
	private Map<SiteAddress, InetAddress> blackSiteToNeighbor, redSiteToNeighbor;
	
	public BlackRospfDataFeed(String transformClassName, long pollPeriod, 
    		String community, String version, InetAddress router,
    		String blackCommunity, String blackVersion, InetAddress blackRouter) {
		super(transformClassName, pollPeriod, community, version, router);
		blackSnmpArgs = makeSnmpArgs(blackCommunity, blackVersion, blackRouter);
	}
	
	protected SiteToNeighborFinder makeSiteToNeighborFinder() {
		blackSiteNeighborFinder = new SiteToNeighborFinder(blackSnmpArgs);
		redSiteToNeighborFinder = super.makeSiteToNeighborFinder(); // makes red finder
		return redSiteToNeighborFinder;
    }

	protected NeighborMetricFinder makeNeighborMetricFinder() {
		return new NeighborMetricFinder(blackSnmpArgs);
	}
	
	protected boolean findMyNeighbors() {
		boolean haveRed = super.findMyNeighbors();
		if (!haveRed) {
			return false;
		}
		redSiteToNeighbor = redSiteToNeighborFinder.getSiteToNeighbor();
		
    	if (blackSiteToNeighbor != null) {
    		return true;
    	}
    	log.info("Finding my black neighbors again");
    	if (blackSiteNeighborFinder.findNeighbors()) {
    		blackSiteToNeighbor = blackSiteNeighborFinder.getSiteToNeighbor();
    		return true;
    	} else {
    		return false;
    	}
    }
	
	private SiteAddress redNeighborToBlackSite(InetAddress redHost) {
		long redLong = SiteAddress.bytesToLongAddress(redHost.getAddress());
		for (SiteAddress blackSite : blackSiteToNeighbor.keySet()) {
			if (blackSite.contains(redLong)) {
				return blackSite;
			}
		}
		log.warn("Can't find black site for " + redHost);
		return null;
	}
	
	protected void publishNeighborToSites(InetAddress walkNeighbor, long linkMetric) {
    	boolean foundOne = false;
        for (Map.Entry<SiteAddress, InetAddress> entry : redSiteToNeighbor.entrySet()) {
            SiteAddress redSite = entry.getKey();
            InetAddress redNeighbor = entry.getValue();
            SiteAddress blackSite = redNeighborToBlackSite(redNeighbor);
            InetAddress blackNeighbor = blackSiteToNeighbor.get(blackSite);
            if (walkNeighbor.equals(blackNeighbor)) {
            	pushData(redSite, linkMetric);
                foundOne = true;
            }
        }
        if (!foundOne) {
			log.info("No site match for next hop " + walkNeighbor);
		}
    }
}
