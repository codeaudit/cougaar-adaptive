package org.cougaar.test.knode.experiment;

import java.net.InetAddress;

import org.cougaar.qos.qrs.ospf.RospfDataFeed;
import org.cougaar.qos.qrs.ospf.RospfDataFeedComponent;
import org.cougaar.util.annotations.Cougaar.Arg;

public class BlackRospfDataFeedComponent extends RospfDataFeedComponent {
	
	@Arg(name="black.router.community", defaultValue="public")
    public String blackCommunity;
    
    @Arg(name="black.router.version", defaultValue="1")
    public String blackVersion;
    
    @Arg(name="black.router.address")
    public InetAddress blackRouter;

    protected RospfDataFeed makeDataFeed() {
    	// TODO: Make a new kind of feed that takes two sets of snmp args
		return new BlackRospfDataFeed(transformClassName, pollPeriod,
				community, version, router,
				blackCommunity, blackVersion, blackRouter);
	}
    
    
}
