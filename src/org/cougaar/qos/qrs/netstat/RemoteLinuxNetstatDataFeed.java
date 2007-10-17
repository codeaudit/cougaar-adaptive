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
package org.cougaar.qos.qrs.netstat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.qos.qrs.SiteAddress;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 *
 */
public class RemoteLinuxNetstatDataFeed extends SimpleQueueingDataFeed implements Constants {
    private static final String USER_NAME_ARG = "--user=";
    private static final String IDENTITY_FILE_ARG = "--identity-file=";
    private static final String HOST_ARG = "--host=";
    private static final String POLL_PERIOD_ARG = "--poll-period=";
    
    private final Logger log;
    private long pollPeriodMillis = 2000;
    private String user;
    private String host;
    private File identityFile;
    private String[] command;
    
    public RemoteLinuxNetstatDataFeed(String[] args) {
        log = Logging.getLogger(getClass());
        for (String arg : args) {
            if (arg.startsWith(USER_NAME_ARG)) {
                user = arg.substring(USER_NAME_ARG.length());
            } else if (arg.startsWith(IDENTITY_FILE_ARG)) {
                identityFile = new File(arg.substring(IDENTITY_FILE_ARG.length()));
            } else if (arg.startsWith(HOST_ARG)) {
                host = arg.substring(HOST_ARG.length());
            } else if (arg.startsWith(POLL_PERIOD_ARG)) {
                pollPeriodMillis = Long.parseLong(arg.substring(POLL_PERIOD_ARG.length()));
            }
        }
        if (host == null || user == null || identityFile == null) {
            log.error("Missing arguments");
            return;
        }
        if (!identityFile.exists()) {
            log.error("Identity file " +identityFile+ " does not exist");
            return;
        }
        command = new String[] {
              "ssh",
              "-o",
              "BatchMode=yes",
              "-i",
              identityFile.getAbsolutePath(),
              "-l",
              user,
              host,
              "netstat",
              "-ren"
        };
        
        RSSUtils.schedule(new Poller(), 0, pollPeriodMillis);
    }
    
    private void parseLine(String line, Map<SiteAddress, Integer> map) 
            throws UnknownHostException, NumberFormatException {
        String[] fields = line.split("\\s+");
        if (fields.length != 8) {
            log.error("Expected 8 fields, found " +fields.length+ " in " + line);
            return;
        }
        String destination = fields[0];
        String maskString = fields[2];
        int metric = Integer.parseInt(fields[4]);
        byte[] addressBytes = SiteAddress.stringToAddress(destination);
        byte[] maskBytes = SiteAddress.stringToAddress(maskString);
        long maskLong = SiteAddress.bytesToLongAddress(maskBytes);
        int mask = SiteAddress.maskToPrefixLength(maskLong);
        SiteAddress address = new SiteAddress(addressBytes, mask);
        map.put(address, metric);
    }
    
    private Map<SiteAddress, Integer> execCommand() throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        InputStream stdout= p.getInputStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(stdout));
        String line;
        // validate the two header lines
        line = rdr.readLine();
        if (!line.startsWith("Kernel")) {
            log.error("Expected \"Kernel IP routing table\", found \"" +line+ "\"");
            return null;
        }
        line = rdr.readLine();
        if (!line.startsWith("Destination")) {
            log.error("Expected \"Destination ...\", found \"" +line+ "\"");
            return null;
        }
        Map<SiteAddress, Integer> map = new HashMap<SiteAddress, Integer>();
        while ((line=rdr.readLine()) != null) {
            try {
                parseLine(line, map);
            } catch (NumberFormatException e) {
                log.error("Error parsing \"" +line+ "\"", e);
            }
        }
        rdr.close();
        return map;
    }
    
    
    /**
     * Run netstat remotely and publish on this data feed as
     * inter-site capacity DataValue.
     * 
     */
    private final class Poller implements Runnable {
        public void run() {
            try {
                Map<SiteAddress, Integer> map = execCommand();
                if (map == null) {
                    return;
                }
                for (Map.Entry<SiteAddress, Integer> entry : map.entrySet()) {
                    log.info(entry.getKey() + " = " + entry.getValue());
                }
            } catch (IOException e) {
                log.error("Netstat request failed", e);
            }
        }
    }

}
