/* 
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.qos.qrs.ospf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.qos.qrs.SitesDB;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * @author jzinky
 *
 */
public class OspfDataFeed extends SimpleQueueingDataFeed implements Constants {
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    private static final String TRANSFORM_ARG = "--transform=";
    
    private final Logger log;
    private final SimpleSnmpRequest request;
    private final long pollPeriodMillis;
    private final Map<String, SiteAddress> peerSites;
    private final OspfMetricTransform transform;
    private SiteAddress mySite;
    
    public OspfDataFeed(String[] feedArgs) {
        log = Logging.getLogger(getClass());
        peerSites = new HashMap<String, SiteAddress>();
        List<String> snmpArgList = new LinkedList<String>();
        long ppm = 2000;
        OspfMetricTransform tf = new UnityTransform();
        for (String arg : feedArgs) {
            if (arg.startsWith(POLL_PERIOD_ARG)) {
                int beginIndex = POLL_PERIOD_ARG.length();
                ppm = Long.parseLong(arg.substring(beginIndex));
            } else if (arg.startsWith(TRANSFORM_ARG)) {
                int beginIndex = TRANSFORM_ARG.length();
                String classname = arg.substring(beginIndex);
                try {
                    Class<?> transformClass = Class.forName(classname);
                    tf = (OspfMetricTransform) transformClass.newInstance();
                } catch (Exception e) {
                    log.error("Couldn't instantiate transform " +arg);
                }
            } else {
                snmpArgList.add(arg);
            }
        }
        pollPeriodMillis = ppm;
        transform = tf;
        String[] snmpArgs = new String[snmpArgList.size()];
        snmpArgList.toArray(snmpArgs);
        request = new SimpleSnmpRequest(snmpArgs);
        
        RSSUtils.schedule(new SiteFinder(), 0);
    }
    
    private String makeKey(SiteAddress destination) {
        return "Site" + KEY_SEPR + "Flow" + KEY_SEPR 
        + mySite + KEY_SEPR 
        + destination + KEY_SEPR 
        + "Capacity_Max";
    }
    
    
    
    private void pushResults(OID oid, OctetString octets) {
        log.info("OID=" +oid+ " octets=" + octets);
        SiteAddress destination = mySite; // TODO get this from the PDU
        int linkMetric = 30;  // TODO: get this from the PDU
        String key = makeKey(destination);
        DataValue value = new DataValue(transform.toMaxCapacity(linkMetric), 2.0);
        newData(key, value, null);
    }
    
    /**
     * Figure out our site, and the sites we talk to
     *
     */
    private final class SiteFinder implements Runnable {
        public void run() {
            // talk snmp to figure out our site
            if (findSite()) {
                // ready to go, start the ospf poller
                RSSUtils.schedule(new Poller(), 0, pollPeriodMillis);
            } else {
                // try again
                RSSUtils.schedule(this, pollPeriodMillis);
            }
        }
        
        private boolean findSite() {
            SitesDB sites = RSS.instance().getSitesDB();
            try {
                InetAddress us = InetAddress.getLocalHost();
                mySite = sites.lookup(us.getHostAddress());
                return true;
            } catch (UnknownHostException e) {
                log.error("Localhost is unknown");
                return false;
            }
        }
    }
    
    /**
     * Send snmp requests for ospf link metric and publish on this data feed as
     * inter-site capacity DataValue.
     * 
     */
    private final class Poller implements Runnable {
        public void run() {
            try {
                PDU reply = request.send();
                @SuppressWarnings("unchecked")
                Vector<VariableBinding> variableBindings = reply.getVariableBindings();
                for (VariableBinding binding : variableBindings) {
                    OID oid = binding.getOid();
                    Variable var = binding.getVariable();
                    if (var instanceof OctetString) {
                        OctetString octets = (OctetString) var;
                        pushResults(oid, octets);
                    } else {
                        log.warn("Expected OctetString for " +oid+ ", found " + var.getClass());
                    }
                }
            } catch (IOException e) {
                log.error("SNMP request failed", e);
            }
        }
    }

}
