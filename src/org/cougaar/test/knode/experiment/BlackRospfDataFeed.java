package org.cougaar.test.knode.experiment;

import java.net.InetAddress;
import java.util.Map;

import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.qos.qrs.ospf.NeighborMetricFinder;
import org.cougaar.qos.qrs.ospf.RospfDataFeed;
import org.cougaar.qos.qrs.ospf.SiteToNeighborFinder;

/**
 * Use the black router to get the link metrics instead of the red R-OSPF MIB. 
 * The Black router metrics track the real network topology closer than the Red,
 * Both in time and in accuracy. But the Black router should not be available to 
 * a real fielded application, because of the HAIPE security boundary.
 * 
 * <p>
 * This dataFeed assumes a special access port was created to get around the HAIPE and 
 * access the Black router. Also, the HAIPE address translation is hard coded in the map 
 * {@link HaipeRedToBlackAddressTranslator}
 * <p>
 * Extra steps are need to map from the Red-side neighbor to the black neighbor
 * <ol>
 * <li> The red neighbor is converted into the HAIPE neighbor
 * <li> The HAIPE neighbor is converted to the Black site
 * <li> The black site converted to the black neighbor. 
 * </ol>
 * 
 */
public class BlackRospfDataFeed extends RospfDataFeed {
	private final String[] blackSnmpArgs;
	private SiteToNeighborFinder blackSiteNeighborFinder, redSiteToNeighborFinder;
	private Map<SiteAddress, InetAddress> blackSiteToNeighbor;
	
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
    	if (blackSiteToNeighbor != null) {
    		return true;
    	}
    	log.info("Finding my black neighbors");
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
	
	protected InetAddress findMeasuredNeighbor(SiteAddress redSite, InetAddress redNeighbor) {
		InetAddress haipeNeighbor = HaipeRedToBlackAddressTranslator.get(redNeighbor);
        if (haipeNeighbor  != null) {
			SiteAddress blackSite = redNeighborToBlackSite(haipeNeighbor);
			return blackSiteToNeighbor.get(blackSite);
        }
        return null;
	}
}
