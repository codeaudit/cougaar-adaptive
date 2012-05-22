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
 * both in time and in accuracy. But the Black router is not be available to a
 * real fielded application, because of the HAIPE security boundary.
 * <p>
 * This datafeed only works in the KNODE. It assumes a special access port was
 * created to get around the HAIPE and access the Black router. Also, the HAIPE
 * address translation is hard coded in the map
 * {@link HaipeRedToBlackAddressTranslator}
 * <p>
 * Extra steps are need to map from the Red-side neighbor to the black WNW
 * neighbor
 * <ol>
 * <li>The red neighbor is converted into the HAIPE Black-side neighbor
 * <li>The HAIPE black-side neighbor is converted to the Black site
 * <li>The black site converted to the black-WNW neighbor.
 * </ol>
 * 
 */
public class BlackRospfDataFeed
      extends RospfDataFeed {
   private final String[] blackSnmpArgs;
   private SiteToNeighborFinder blackSiteToNeighborFinder, redSiteToNeighborFinder;
   private Map<SiteAddress, InetAddress> blackSiteToWnwNeighbor;

   public BlackRospfDataFeed(String transformClassName, long pollPeriod, String community, String version, InetAddress router,
                             String blackCommunity, String blackVersion, InetAddress blackRouter) {
      super(transformClassName, pollPeriod, community, version, router);
      blackSnmpArgs = makeSnmpArgs(blackCommunity, blackVersion, blackRouter);
   }

   @Override
   protected SiteToNeighborFinder makeSiteToNeighborFinder() {
      // Use the Black-side router's ROSPF MIB
      blackSiteToNeighborFinder = new SiteToNeighborFinder(blackSnmpArgs);
      redSiteToNeighborFinder = super.makeSiteToNeighborFinder(); // makes red
                                                                  // finder
      return redSiteToNeighborFinder;
   }

   // Create mapping from Black Neighbor to its link metric in the WNW.
   // This uses the Black-side router's OSPF-MIB
   @Override
   protected NeighborMetricFinder makeNeighborMetricFinder() {
      return new NeighborMetricFinder(blackSnmpArgs);
   }

   // Create two mappings: from Red Site to Red Neighbor; and from BlackSite to
   // Black WNW Neighbor
   @Override
   protected boolean findMyNeighbors() {
      boolean haveRed = super.findMyNeighbors();
      if (!haveRed) {
         return false;
      }
      if (blackSiteToWnwNeighbor != null) {
         return true;
      }
      log.info("Finding my black neighbors");
      if (blackSiteToNeighborFinder.findNeighbors()) {
         blackSiteToWnwNeighbor = blackSiteToNeighborFinder.getSiteToNeighbor();
         return true;
      } else {
         return false;
      }
   }

   // Convert the HAIPE Black-side Neighbor to its Black Site
   private SiteAddress HaipeBlackSideNeighborToBlackSite(InetAddress redHost) {
      long redLong = SiteAddress.bytesToLongAddress(redHost.getAddress());
      for (SiteAddress blackSite : blackSiteToWnwNeighbor.keySet()) {
         if (blackSite.contains(redLong)) {
            return blackSite;
         }
      }
      log.warn("Can't find black site for " + redHost);
      return null;
   }

   // Three step mapping:
   // Red Neighbor -> HAIPE Black-side Neighbor -> Black Site -> Black WNW
   // Neighbor
   @Override
   protected InetAddress findMeasuredNeighbor(InetAddress redNeighbor) {
      InetAddress haipeBlackSideNeighbor = HaipeRedToBlackAddressTranslator.get(redNeighbor);
      if (haipeBlackSideNeighbor != null) {
         SiteAddress blackSite = HaipeBlackSideNeighborToBlackSite(haipeBlackSideNeighbor);
         return blackSiteToWnwNeighbor.get(blackSite);
      }
      return null;
   }
}
