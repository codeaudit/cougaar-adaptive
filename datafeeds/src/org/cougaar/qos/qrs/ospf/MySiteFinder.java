package org.cougaar.qos.qrs.ospf;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.qos.qrs.SitesDB;

public class MySiteFinder {
	private SiteAddress mySite;
	
	public SiteAddress getMySite() {
		return mySite;
	}

	public boolean findMySite() {
    	if (mySite != null) {
    		return true;
    	}
        SitesDB sites = RSS.instance().getSitesDB();
        try {
            InetAddress us = InetAddress.getLocalHost();
            mySite = sites.lookup(us.getHostAddress());
            if (RospfDataFeed.log.isInfoEnabled()) {
            	RospfDataFeed.log.info("We are " + us + " and our site is " + mySite);
			}
			return true;
        } catch (UnknownHostException e) {
        	RospfDataFeed.log.error("Localhost is unknown");
            return false;
        }
    }
}
